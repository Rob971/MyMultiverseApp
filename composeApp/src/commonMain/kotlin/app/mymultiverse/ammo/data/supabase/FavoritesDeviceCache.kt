package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.data.supabase.dto.FavoriteDishRow
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.nutrition.FavoriteDish
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Per-account device cache for favorites.
 *
 * [favorites] tracks the signed-in account: on sign-in it is seeded from that account's persisted
 * cache, and on account switch it is swapped for the new account's list. Writes are keyed per
 * account and a write whose owner no longer matches the signed-in account is dropped, so a second
 * account on a shared device never sees the first one's favorites — even offline, or when an
 * in-flight response for the previous account arrives after switching.
 */
internal class FavoritesDeviceCache(
    private val settings: Settings,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _favorites = MutableStateFlow<List<FavoriteDish>>(emptyList())
    val favorites: StateFlow<List<FavoriteDish>> = _favorites.asStateFlow()

    private var accountId: String? = null

    /** The account whose favorites [favorites] currently shows; null when signed out. */
    val currentAccountId: String? get() = accountId

    /** Advances [favorites] to the signed-in account's cached list. Loading keeps what is shown. */
    fun showAccount(state: AuthState) {
        val next = when (state) {
            is AuthState.Authenticated -> state.user.id
            AuthState.Unauthenticated -> null
            AuthState.Loading, AuthState.ConfigurationMissing -> return
        }
        if (next == accountId) return
        accountId = next
        _favorites.value = next?.let(::loadCache).orEmpty()
    }

    /**
     * Applies [update] to [owner]'s list and persists it under [owner]. Dropped when the account
     * changed while the request was in flight: the next refresh rebuilds that account's cache.
     */
    fun commit(owner: String, update: (List<FavoriteDish>) -> List<FavoriteDish>) {
        if (owner != accountId) return
        val list = update(_favorites.value)
        _favorites.value = list
        settings.putString(
            cacheKey(owner),
            json.encodeToString(list.map { FavoriteDishRow(it.label, it.normalisedLabel) }),
        )
    }

    private fun loadCache(owner: String): List<FavoriteDish> {
        val raw = settings.getStringOrNull(cacheKey(owner)) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<FavoriteDishRow>>(raw)
                .map { FavoriteDish(label = it.label, normalisedLabel = it.normalisedLabel) }
        }.getOrDefault(emptyList())
    }

    internal companion object {
        fun cacheKey(accountId: String): String = "favorite_dishes_$accountId"
    }
}