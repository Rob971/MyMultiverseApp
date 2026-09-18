package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.data.supabase.dto.AddFavoriteParams
import app.mymultiverse.ammo.data.supabase.dto.FavoriteDishRow
import app.mymultiverse.ammo.data.supabase.dto.RemoveFavoriteParams
import app.mymultiverse.ammo.data.supabase.dto.ReplaceFavoriteParams
import app.mymultiverse.ammo.domain.model.nutrition.FavoriteDish
import app.mymultiverse.ammo.domain.model.nutrition.favoriteKeyFor
import app.mymultiverse.ammo.domain.repository.FavoriteDishesRepository
import app.mymultiverse.ammo.domain.repository.FavoriteMutationException
import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Supabase-backed favorites. [favorites] is a read-only cache seeded from device
 * storage and advanced only after a confirmed remote mutation — never optimistically,
 * so a failed write never pretends to succeed.
 */
internal class SupabaseFavoriteDishesRepository(
    private val client: SupabaseClient,
    private val settings: Settings,
) : FavoriteDishesRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _favorites = MutableStateFlow(loadCache())
    override val favorites: StateFlow<List<FavoriteDish>> = _favorites.asStateFlow()

    private val _remoteAvailable = MutableStateFlow(true)
    override val remoteAvailable: StateFlow<Boolean> = _remoteAvailable.asStateFlow()

    override suspend fun refresh(): Result<Unit> = catch {
        client.auth.awaitInitialization()
        val rows = client.postgrest["user_favorite_dishes"]
            .select(Columns.ALL)
            .decodeList<FavoriteDishRow>()
        commit(rows.map { FavoriteDish(label = it.label, normalisedLabel = it.normalisedLabel) })
    }

    override suspend fun addFavorite(label: String): Result<Unit> {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return Result.failure(FavoriteMutationException(FavoriteMutationException.Kind.UNKNOWN))
        return catch {
            client.auth.awaitInitialization()
            client.postgrest.rpc("add_favorite", AddFavoriteParams(trimmed))
            commit(_favorites.value
                .filterNot { it.normalisedLabel == favoriteKeyFor(trimmed) } +
                FavoriteDish(label = trimmed, normalisedLabel = favoriteKeyFor(trimmed)))
        }
    }

    override suspend fun removeFavorite(normalisedLabel: String): Result<Unit> = catch {
        client.auth.awaitInitialization()
        client.postgrest.rpc("remove_favorite", RemoveFavoriteParams(normalisedLabel))
        commit(_favorites.value.filterNot { it.normalisedLabel == normalisedLabel })
    }

    override suspend fun replaceFavorite(removeNormalisedLabel: String, newLabel: String): Result<Unit> {
        val trimmed = newLabel.trim()
        return catch {
            client.auth.awaitInitialization()
            client.postgrest.rpc(
                "replace_favorite",
                ReplaceFavoriteParams(removeNormalised = removeNormalisedLabel, newLabel = trimmed),
            )
            commit(_favorites.value
                .filterNot { it.normalisedLabel == removeNormalisedLabel } +
                FavoriteDish(label = trimmed, normalisedLabel = favoriteKeyFor(trimmed)))
        }
    }

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

    private fun commit(list: List<FavoriteDish>) {
        _favorites.value = list
        settings.putString(CACHE_KEY, json.encodeToString(list.map { FavoriteDishRow(it.label, it.normalisedLabel) }))
    }

    private fun loadCache(): List<FavoriteDish> {
        val raw = settings.getStringOrNull(CACHE_KEY) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<FavoriteDishRow>>(raw)
                .map { FavoriteDish(label = it.label, normalisedLabel = it.normalisedLabel) }
        }.getOrDefault(emptyList())
    }

    private fun isCapExceeded(e: Throwable): Boolean =
        generateSequence(e) { it.cause }.any { it.message.orEmpty().contains("favorite_cap_exceeded") }

    private companion object {
        const val CACHE_KEY = "favorite_dishes"
    }
}