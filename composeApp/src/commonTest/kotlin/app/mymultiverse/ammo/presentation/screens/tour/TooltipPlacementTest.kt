package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals

class TooltipPlacementTest {

    @Test
    fun targetNearTop_placesTooltipBelowTarget() {
        val offset = calculateTooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 40f, right = 300f, bottom = 180f),
            viewportHeightPx = 1_000f,
            tooltipHeightPx = 260f,
            edgePaddingPx = 20f,
            cardGapPx = 16f,
        )

        assertEquals(196f, offset)
    }

    @Test
    fun targetNearBottom_placesTooltipAboveTarget() {
        val offset = calculateTooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 700f, right = 300f, bottom = 820f),
            viewportHeightPx = 1_000f,
            tooltipHeightPx = 260f,
            edgePaddingPx = 20f,
            cardGapPx = 16f,
        )

        assertEquals(424f, offset)
    }

    @Test
    fun fullHeightTarget_clampsTooltipInsideViewport() {
        val offset = calculateTooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 0f, right = 400f, bottom = 1_000f),
            viewportHeightPx = 1_000f,
            tooltipHeightPx = 260f,
            edgePaddingPx = 20f,
            cardGapPx = 16f,
        )

        assertEquals(20f, offset)
    }
}
