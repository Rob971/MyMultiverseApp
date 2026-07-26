package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals

class SpotlightTourPlacementTest {

    @Test
    fun tooltipPosition_placesCardAboveSpotlight_whenSpaceIsAvailable() {
        val top = calculateTooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 700f, right = 400f, bottom = 780f),
            viewportHeightPx = 1_000f,
            tooltipHeightPx = 260f,
            cardGapPx = 16f,
            edgePaddingPx = 20f,
        )

        assertEquals(expected = 424f, actual = top)
    }

    @Test
    fun tooltipPosition_placesCardBelowSpotlight_whenTargetIsNearTop() {
        val top = calculateTooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 40f, right = 400f, bottom = 100f),
            viewportHeightPx = 1_000f,
            tooltipHeightPx = 260f,
            cardGapPx = 16f,
            edgePaddingPx = 20f,
        )

        assertEquals(expected = 116f, actual = top)
    }

    @Test
    fun tooltipPosition_centresCard_whenContentSpotlightLeavesNoRoom() {
        val top = calculateTooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 100f, right = 400f, bottom = 900f),
            viewportHeightPx = 1_000f,
            tooltipHeightPx = 260f,
            cardGapPx = 16f,
            edgePaddingPx = 20f,
        )

        assertEquals(expected = 370f, actual = top)
    }
}
