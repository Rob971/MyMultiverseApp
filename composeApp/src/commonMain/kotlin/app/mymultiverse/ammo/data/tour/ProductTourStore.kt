package app.mymultiverse.ammo.data.tour

import com.russhwolf.settings.Settings

/**
 * Persists whether the user has completed or skipped the product tour for a given version key.
 *
 * Version-keyed so the tour shows once per major feature release (e.g. "1.5.3").
 */
class ProductTourStore(private val settings: Settings) {

    fun hasSeenTour(versionKey: String): Boolean =
        settings.getBoolean(seenKey(versionKey), defaultValue = false)

    fun markTourSeen(versionKey: String) {
        settings.putBoolean(seenKey(versionKey), true)
    }

    /** Exposed for testing — clears the seen flag for the given version. */
    internal fun clearTourSeen(versionKey: String) {
        settings.remove(seenKey(versionKey))
    }

    private fun seenKey(versionKey: String): String = "product_tour_seen_$versionKey"
}
