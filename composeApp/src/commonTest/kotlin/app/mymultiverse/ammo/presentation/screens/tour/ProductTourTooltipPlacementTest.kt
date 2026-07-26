package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductTourTooltipPlacementTest {

    @Test
    fun noSpotlight_returnsZeroForCenteredTooltip() {
        val top = calculateTourTooltipTopOffsetPx(
            spotlightRect = null,
            tooltipHeightPx = 260f,
            viewportHeightPx = 800f,
            gapPx = 16f,
            edgePaddingPx = 16f,
        )

        assertEquals(0f, top)
    }

    @Test
    fun lowerSpotlight_placesTooltipAboveTarget() {
        val top = calculateTourTooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 600f, right = 100f, bottom = 700f),
            tooltipHeightPx = 200f,
            viewportHeightPx = 800f,
            gapPx = 16f,
            edgePaddingPx = 16f,
        )

        assertEquals(384f, top)
    }

    @Test
    fun upperSpotlight_placesTooltipBelowTarget() {
        val top = calculateTourTooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 50f, right = 100f, bottom = 150f),
            tooltipHeightPx = 200f,
            viewportHeightPx = 800f,
            gapPx = 16f,
            edgePaddingPx = 16f,
        )

        assertEquals(166f, top)
    }

    @Test
    fun fullHeightSpotlight_clampsTooltipInsideViewport() {
        val tooltipHeight = 260f
        val viewportHeight = 800f
        val edgePadding = 16f

        val top = calculateTourTooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 32f, right = 100f, bottom = 768f),
            tooltipHeightPx = tooltipHeight,
            viewportHeightPx = viewportHeight,
            gapPx = 16f,
            edgePaddingPx = edgePadding,
        )

        assertEquals(edgePadding, top)
        assertTrue(top + tooltipHeight <= viewportHeight - edgePadding)
    }

    @Test
    fun tooltipTallerThanViewport_startsAtTopEdge() {
        val top = calculateTourTooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 100f, right = 100f, bottom = 700f),
            tooltipHeightPx = 900f,
            viewportHeightPx = 800f,
            gapPx = 16f,
            edgePaddingPx = 16f,
        )

        assertEquals(0f, top)
    }
}
