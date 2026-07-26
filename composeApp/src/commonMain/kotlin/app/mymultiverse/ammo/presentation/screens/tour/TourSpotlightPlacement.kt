package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect

/**
 * Pure layout helpers for spotlight cutouts and tooltip card placement.
 *
 * Kept free of Compose runtime so unit tests can cover edge cases that previously
 * made the tour appear to "disappear" after Next (oversized cutout + off-screen card).
 */
internal object TourSpotlightPlacement {

    /**
     * Spotlight height must stay below this fraction of the screen.
     * Full-width cards (daily hub) are expected; height/area are the regressors.
     */
    const val MaxSpotlightHeightFraction = 0.55f

    /** Spotlight area must stay below this fraction of the screen (guards near-full cutouts). */
    const val MaxSpotlightAreaFraction = 0.45f

    /** Minimum top padding so the card clears the status / app bar area. */
    const val MinTopPx = 80f

    /**
     * Returns [rect] when it is small enough to leave a visible scrim and room for the
     * tooltip; otherwise `null` so the overlay falls back to a centred modal (full scrim).
     *
     * An oversized target (e.g. an entire `fillMaxSize` screen) would Clear almost the
     * whole scrim and push the tooltip off-screen — the bug reported after tapping Next
     * on the welcome step.
     */
    fun usableSpotlightRect(
        rect: Rect?,
        screenWidthPx: Float,
        screenHeightPx: Float,
    ): Rect? {
        if (rect == null) return null
        if (screenWidthPx <= 0f || screenHeightPx <= 0f) return null
        val maxHeight = screenHeightPx * MaxSpotlightHeightFraction
        val maxArea = screenWidthPx * screenHeightPx * MaxSpotlightAreaFraction
        if (rect.height > maxHeight) return null
        if (rect.width * rect.height > maxArea) return null
        return rect
    }

    /**
     * Top padding (px) for a tooltip aligned [TopStart][androidx.compose.ui.Alignment.TopStart].
     *
     * Prefers placing the card above the spotlight when there is room; otherwise below.
     * Result is clamped so the card stays within the screen vertically.
     *
     * Returns `null` when [spotlightRect] is null — callers should centre the card instead.
     */
    fun tooltipTopOffsetPx(
        spotlightRect: Rect?,
        tooltipHeightPx: Float,
        cardGapPx: Float,
        screenHeightPx: Float,
        minTopPx: Float = MinTopPx,
    ): Float? {
        if (spotlightRect == null) return null
        if (screenHeightPx <= 0f || tooltipHeightPx <= 0f) return 0f

        val aboveTop = spotlightRect.top - tooltipHeightPx - cardGapPx
        val belowTop = spotlightRect.bottom + cardGapPx
        val preferred = if (aboveTop > minTopPx) aboveTop else belowTop

        val maxTop = (screenHeightPx - tooltipHeightPx).coerceAtLeast(0f)
        return preferred.coerceIn(minTopPx.coerceAtMost(maxTop), maxTop)
    }
}
