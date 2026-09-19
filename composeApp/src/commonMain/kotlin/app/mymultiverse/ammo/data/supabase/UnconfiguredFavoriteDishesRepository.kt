package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.domain.model.nutrition.FavoriteDish
import app.mymultiverse.ammo.domain.repository.FavoriteDishesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Read-only fallback used when Supabase is not configured (missing anon key) or
 * the user is not signed in. Favorites are empty and every mutation is a no-op
 * failure so the UI can disable the star rather than fake success.
 */
internal class UnconfiguredFavoriteDishesRepository : FavoriteDishesRepository {
    override val favorites: StateFlow<List<FavoriteDish>> = MutableStateFlow(emptyList())
    override val remoteAvailable: StateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun refresh(): Result<Unit> = Result.failure(UnavailableException())
    override suspend fun addFavorite(label: String): Result<Unit> = Result.failure(UnavailableException())
    override suspend fun removeFavorite(normalisedLabel: String): Result<Unit> = Result.failure(UnavailableException())
    override suspend fun replaceFavorite(removeNormalisedLabel: String, newLabel: String): Result<Unit> =
        Result.failure(UnavailableException())

    private class UnavailableException : Exception("favorites_unavailable")
}