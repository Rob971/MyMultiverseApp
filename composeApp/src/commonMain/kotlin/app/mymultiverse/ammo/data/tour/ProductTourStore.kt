package app.mymultiverse.ammo.data.tour

import com.russhwolf.settings.Settings

/**
 * Persists whether the user has completed or skipped the product tour for a given key.
 *
 * Prefer [app.mymultiverse.ammo.presentation.screens.tour.ProductTourCatalog.TOUR_ID]
 * (`spotlight_v1`) so a new user sees the walkthrough once across alpha/beta/prod builds.
 * Bump that ID only when intentionally re-introducing the tour.
 */
class ProductTourStore(private val settings: Settings) {

    fun hasSeenTour(tourKey: String): Boolean =
        settings.getBoolean(seenKey(tourKey), defaultValue = false)

    fun markTourSeen(tourKey: String) {
        settings.putBoolean(seenKey(tourKey), true)
    }

    /** Exposed for testing — clears the seen flag for the given tour key. */
    internal fun clearTourSeen(tourKey: String) {
        settings.remove(seenKey(tourKey))
    }

    private fun seenKey(tourKey: String): String = "product_tour_seen_$tourKey"
}
