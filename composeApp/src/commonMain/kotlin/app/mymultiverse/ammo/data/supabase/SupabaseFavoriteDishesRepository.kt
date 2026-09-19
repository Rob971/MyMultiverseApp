package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.data.supabase.dto.AddFavoriteParams
import app.mymultiverse.ammo.data.supabase.dto.FavoriteDishRow
import app.mymultiverse.ammo.data.supabase.dto.RemoveFavoriteParams
import app.mymultiverse.ammo.data.supabase.dto.ReplaceFavoriteParams
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.nutrition.FavoriteDish
import app.mymultiverse.ammo.domain.model.nutrition.favoriteKeyFor
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.domain.repository.FavoriteDishesRepository
import app.mymultiverse.ammo.domain.repository.FavoriteMutationException
import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json

/**
 * Supabase-backed favorites. [favorites] is a read-only cache seeded from device
 * storage and advanced only after a confirmed remote mutation — never optimistically,
 * so a failed write never pretends to succeed.
 *
 * The device cache is kept per account and [favorites] follows the signed-in account, so a
 * second account on a shared device never sees the first one's favorites, even offline.
 */
internal class SupabaseFavoriteDishesRepository(
    private val client: SupabaseClient,
    private val settings: Settings,
    authRepository: AuthRepository,
    scope: CoroutineScope,
) : FavoriteDishesRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /** The account whose favorites [favorites] shows; null when signed out. */
    private var accountId: String? = null

    private val _favorites = MutableStateFlow<List<FavoriteDish>>(emptyList())
    override val favorites: StateFlow<List<FavoriteDish>> = _favorites.asStateFlow()

    private val _remoteAvailable = MutableStateFlow(true)
    override val remoteAvailable: StateFlow<Boolean> = _remoteAvailable.asStateFlow()

    init {
        showAccount(authRepository.authState.value)
        authRepository.authState.onEach(::showAccount).launchIn(scope)
    }

    override suspend fun refresh(): Result<Unit> {
        val owner = accountId ?: return notSignedIn()
        return catch {
            client.auth.awaitInitialization()
            val rows = client.postgrest["user_favorite_dishes"]
                .select(Columns.ALL)
                .decodeList<FavoriteDishRow>()
            commit(owner) { rows.map { FavoriteDish(label = it.label, normalisedLabel = it.normalisedLabel) } }
        }
    }

    override suspend fun addFavorite(label: String): Result<Unit> {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return Result.failure(FavoriteMutationException(FavoriteMutationException.Kind.UNKNOWN))
        val owner = accountId ?: return notSignedIn()
        return catch {
            client.auth.awaitInitialization()
            client.postgrest.rpc("add_favorite", AddFavoriteParams(trimmed))
            commit(owner) { current ->
                current.filterNot { it.normalisedLabel == favoriteKeyFor(trimmed) } +
                    FavoriteDish(label = trimmed, normalisedLabel = favoriteKeyFor(trimmed))
            }
        }
    }

    override suspend fun removeFavorite(normalisedLabel: String): Result<Unit> {
        val owner = accountId ?: return notSignedIn()
        return catch {
            client.auth.awaitInitialization()
            client.postgrest.rpc("remove_favorite", RemoveFavoriteParams(normalisedLabel))
            commit(owner) { current -> current.filterNot { it.normalisedLabel == normalisedLabel } }
        }
    }

    override suspend fun replaceFavorite(removeNormalisedLabel: String, newLabel: String): Result<Unit> {
        val trimmed = newLabel.trim()
        val owner = accountId ?: return notSignedIn()
        return catch {
            client.auth.awaitInitialization()
            client.postgrest.rpc(
                "replace_favorite",
                ReplaceFavoriteParams(removeNormalised = removeNormalisedLabel, newLabel = trimmed),
            )
            commit(owner) { current ->
                current.filterNot { it.normalisedLabel == removeNormalisedLabel } +
                    FavoriteDish(label = trimmed, normalisedLabel = favoriteKeyFor(trimmed))
            }
        }
    }

    /** Shows the signed-in account's cached favorites; Loading keeps what is shown (e.g. offline refresh). */
    private fun showAccount(state: AuthState) {
        val next = when (state) {
            is AuthState.Authenticated -> state.user.id
            AuthState.Unauthenticated -> null
            AuthState.Loading, AuthState.ConfigurationMissing -> return
        }
        if (next == accountId) return
        accountId = next
        _favorites.value = next?.let(::loadCache).orEmpty()
    }

    private fun notSignedIn(): Result<Unit> =
        Result.failure(FavoriteMutationException(FavoriteMutationException.Kind.UNKNOWN))

    /** Runs [block]; on success commits the authoritative list and marks remote available. */
    private suspend fun catch(block: suspend () -> Unit): Result<Unit> = try {
        block()
        _remoteAvailable.value = true
        Result.success(Unit)
    } catch (e: Exception) {
        if (isCapExceeded(e)) {
            _remoteAvailable.value = true
            Result.failure(FavoriteMutationException(FavoriteMutationException.Kind.CAP_EXCEEDED, e))
        } else {
            _remoteAvailable.value = false
            Result.failure(FavoriteMutationException(FavoriteMutationException.Kind.OFFLINE, e))
        }
    }

    /**
     * Applies [update] to [owner]'s list and caches it under [owner]. Skipped when the account
     * changed while the request was in flight: the next [refresh] rebuilds that account's cache.
     */
    private fun commit(owner: String, update: (List<FavoriteDish>) -> List<FavoriteDish>) {
        if (owner != accountId) return
        val list = update(_favorites.value)
        _favorites.value = list
        settings.putString(cacheKey(owner), json.encodeToString(list.map { FavoriteDishRow(it.label, it.normalisedLabel) }))
    }

    private fun loadCache(owner: String): List<FavoriteDish> {
        val raw = settings.getStringOrNull(cacheKey(owner)) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<FavoriteDishRow>>(raw)
                .map { FavoriteDish(label = it.label, normalisedLabel = it.normalisedLabel) }
        }.getOrDefault(emptyList())
    }

    private fun isCapExceeded(e: Throwable): Boolean =
        generateSequence(e) { it.cause }.any { it.message.orEmpty().contains("favorite_cap_exceeded") }

    internal companion object {
        fun cacheKey(accountId: String): String = "favorite_dishes_$accountId"
    }
}