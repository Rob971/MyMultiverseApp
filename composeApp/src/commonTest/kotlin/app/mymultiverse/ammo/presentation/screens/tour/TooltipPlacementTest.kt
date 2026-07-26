package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for [resolveTooltipPlacement] — the pure positioning logic that keeps the product
 * tour tooltip card on-screen regardless of how large the highlighted target is.
 */
class TooltipPlacementTest {

    private val containerHeightPx = 2000f
    private val gapPx = 48f
    private val cardHeightEstimatePx = 680f

    private fun resolve(rect: Rect?): TooltipVerticalPlacement =
        resolveTooltipPlacement(
            spotlightRect = rect,
            containerHeightPx = containerHeightPx,
            gapPx = gapPx,
            cardHeightEstimatePx = cardHeightEstimatePx,
        )

    @Test
    fun nullSpotlight_isCentered() {
        assertIs<TooltipVerticalPlacement.Centered>(resolve(null))
    }

    @Test
    fun fullScreenSpotlight_fallsBackToCentered() {
        // Regression: a near-full-screen target (e.g. the home hub content) must not push the
        // card off-screen — it should centre instead so the tour never appears broken.
        val fullScreen = Rect(left = 0f, top = 40f, right = 1080f, bottom = 1980f)
        assertIs<TooltipVerticalPlacement.Centered>(resolve(fullScreen))
    }

    @Test
    fun spotlightNearTop_placesCardBelow() {
        val rect = Rect(left = 100f, top = 100f, right = 500f, bottom = 300f)
        val placement = resolve(rect)
        assertIs<TooltipVerticalPlacement.BelowSpotlight>(placement)
        assertEquals(300f + gapPx, placement.topPaddingPx)
    }

    @Test
    fun spotlightNearBottom_placesCardAbove() {
        val rect = Rect(left = 100f, top = 1600f, right = 500f, bottom = 1900f)
        val placement = resolve(rect)
        assertIs<TooltipVerticalPlacement.AboveSpotlight>(placement)
        assertEquals(containerHeightPx - 1600f + gapPx, placement.bottomPaddingPx)
    }

    @Test
    fun spotlightMidScreen_prefersBelowWhenBothFit() {
        val rect = Rect(left = 100f, top = 800f, right = 500f, bottom = 1000f)
        val placement = resolve(rect)
        assertIs<TooltipVerticalPlacement.BelowSpotlight>(placement)
        assertEquals(1000f + gapPx, placement.topPaddingPx)
    }
}
