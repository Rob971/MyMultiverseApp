package app.mymultiverse.ammo.domain.repository

import app.mymultiverse.ammo.domain.model.nutrition.FavoriteDish
import kotlinx.coroutines.flow.StateFlow

/**
 * Personal favorite dishes (F2). Favorites are visible offline via [favorites],
 * which is a read-only cache that only advances after a confirmed remote mutation —
 * never optimistically.
 */
interface FavoriteDishesRepository {
    /** Last-known favorites, readable offline. */
    val favorites: StateFlow<List<FavoriteDish>>

    /** False after a network failure; mutations should surface this to the UI. */
    val remoteAvailable: StateFlow<Boolean>

    /** Pulls the latest favorites from the server into the cache. */
    suspend fun refresh(): Result<Unit>

    /** Adds [label] as a favorite. Fails without mutating when offline. */
    suspend fun addFavorite(label: String): Result<Unit>

    /** Removes the favorite keyed by [normalisedLabel]. */
    suspend fun removeFavorite(normalisedLabel: String): Result<Unit>

    /** Atomically replaces the favorite [removeNormalisedLabel] with [newLabel]. */
    suspend fun replaceFavorite(removeNormalisedLabel: String, newLabel: String): Result<Unit>
}