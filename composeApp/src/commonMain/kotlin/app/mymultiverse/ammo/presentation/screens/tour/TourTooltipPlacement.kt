package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect

/**
 * Pure layout math for placing the tour tooltip relative to a spotlight cutout.
 *
 * Kept free of Compose runtime so unit tests can cover edge cases such as
 * near-full-screen targets (which previously pushed the card off-screen).
 */
internal object TourTooltipPlacement {
    const val DEFAULT_MIN_TOP_PX = 80f

    /**
     * Returns the Y offset (from the top of the overlay) at which the tooltip card should start.
     *
     * Prefers placing the card above the spotlight when there is room; otherwise below.
     * Always clamps so the estimated card height stays within the overlay when possible —
     * critical when the spotlight target is large (e.g. nearly fills the screen).
     */
    fun tooltipTopPx(
        spotlightRect: Rect,
        tooltipHeightPx: Float,
        cardGapPx: Float,
        screenHeightPx: Float,
        minTopPx: Float = DEFAULT_MIN_TOP_PX,
    ): Float {
        val aboveTop = spotlightRect.top - tooltipHeightPx - cardGapPx
        val belowTop = spotlightRect.bottom + cardGapPx
        val preferred = if (aboveTop > minTopPx) aboveTop else belowTop

        // Keep the card on-screen: never start above minTopPx, and never so low that
        // the estimated card height is fully clipped at the bottom.
        val maxTop = (screenHeightPx - tooltipHeightPx).coerceAtLeast(minTopPx)
        return preferred.coerceIn(minTopPx, maxTop)
    }
}
