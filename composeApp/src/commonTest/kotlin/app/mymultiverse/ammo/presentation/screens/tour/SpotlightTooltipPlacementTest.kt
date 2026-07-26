package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for [tooltipTopOffsetPx] — the pure placement logic that keeps the product tour
 * tooltip card fully on screen.
 *
 * Regression context: step 2 of the tour ("home hub") used to spotlight the entire home
 * content, which pushed the tooltip below the bottom edge of the screen and made the tour
 * appear broken after tapping Next. The placement must now fall back to centring the card
 * whenever neither the above nor the below position fits.
 */
class SpotlightTooltipPlacementTest {

    private val tooltipHeight = 650f
    private val gap = 40f
    private val containerHeight = 2400f

    @Test
    fun nullSpotlightRect_returnsNull_forCentredModalStep() {
        assertNull(
            tooltipTopOffsetPx(
                spotlightRect = null,
                tooltipHeightPx = tooltipHeight,
                gapPx = gap,
                containerHeightPx = containerHeight,
            ),
        )
    }

    @Test
    fun spotlightNearBottom_placesTooltipAbove() {
        // e.g. a bottom nav tab target
        val rect = Rect(left = 100f, top = 2200f, right = 400f, bottom = 2350f)

        val offset = tooltipTopOffsetPx(
            spotlightRect = rect,
            tooltipHeightPx = tooltipHeight,
            gapPx = gap,
            containerHeightPx = containerHeight,
        )

        assertEquals(rect.top - tooltipHeight - gap, offset)
    }

    @Test
    fun spotlightNearTop_placesTooltipBelow_whenItFits() {
        val rect = Rect(left = 0f, top = 150f, right = 1000f, bottom = 500f)

        val offset = tooltipTopOffsetPx(
            spotlightRect = rect,
            tooltipHeightPx = tooltipHeight,
            gapPx = gap,
            containerHeightPx = containerHeight,
        )

        assertEquals(rect.bottom + gap, offset)
    }

    @Test
    fun fullScreenSpotlight_returnsNull_soCardIsCentredAndVisible() {
        // Regression: a spotlight covering (almost) the whole screen must not push the
        // tooltip off screen below the spotlight.
        val rect = Rect(left = 0f, top = 100f, right = 1080f, bottom = 2300f)

        assertNull(
            tooltipTopOffsetPx(
                spotlightRect = rect,
                tooltipHeightPx = tooltipHeight,
                gapPx = gap,
                containerHeightPx = containerHeight,
            ),
        )
    }

    @Test
    fun spotlightNearTop_withNoRoomBelow_returnsNull() {
        // Short container: neither above (top too close) nor below (exceeds container) fits.
        val rect = Rect(left = 0f, top = 100f, right = 500f, bottom = 400f)

        assertNull(
            tooltipTopOffsetPx(
                spotlightRect = rect,
                tooltipHeightPx = tooltipHeight,
                gapPx = gap,
                containerHeightPx = 1000f,
            ),
        )
    }

    @Test
    fun aboveOffsetAtExactMinTop_fallsThroughToBelow() {
        // aboveTop == minTopPx is not strictly greater, so placement goes below.
        val minTop = TooltipMinTopPx
        val rect = Rect(
            left = 0f,
            top = minTop + tooltipHeight + gap,
            right = 500f,
            bottom = minTop + tooltipHeight + gap + 100f,
        )

        val offset = tooltipTopOffsetPx(
            spotlightRect = rect,
            tooltipHeightPx = tooltipHeight,
            gapPx = gap,
            containerHeightPx = containerHeight,
        )

        assertEquals(rect.bottom + gap, offset)
    }
}
