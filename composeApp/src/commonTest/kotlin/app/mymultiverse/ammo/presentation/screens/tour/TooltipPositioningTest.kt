package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TooltipPositioningTest {

    @Test
    fun oversizedTarget_clampsTooltipInsideViewport() {
        val viewport = Rect(left = 0f, top = 0f, right = 456f, bottom = 1024f)
        val tooltipHeight = 260f

        val top = tooltipTopOffsetPx(
            spotlightRect = Rect(left = 0f, top = 124f, right = 456f, bottom = 882f),
            viewportRect = viewport,
            tooltipHeightPx = tooltipHeight,
            cardGapPx = 16f,
            viewportPaddingPx = 20f,
        )

        assertEquals(744f, top)
        assertTrue(top >= 20f)
        assertTrue(top + tooltipHeight <= viewport.bottom - 20f)
    }

    @Test
    fun boundedTargetNearTop_placesTooltipBelowTarget() {
        val top = tooltipTopOffsetPx(
            spotlightRect = Rect(left = 20f, top = 100f, right = 436f, bottom = 220f),
            viewportRect = Rect(left = 0f, top = 0f, right = 456f, bottom = 1024f),
            tooltipHeightPx = 260f,
            cardGapPx = 16f,
            viewportPaddingPx = 20f,
        )

        assertEquals(236f, top)
    }

    @Test
    fun boundedTargetNearBottom_placesTooltipAboveTarget() {
        val top = tooltipTopOffsetPx(
            spotlightRect = Rect(left = 20f, top = 780f, right = 436f, bottom = 900f),
            viewportRect = Rect(left = 0f, top = 0f, right = 456f, bottom = 1024f),
            tooltipHeightPx = 260f,
            cardGapPx = 16f,
            viewportPaddingPx = 20f,
        )

        assertEquals(504f, top)
    }
}
