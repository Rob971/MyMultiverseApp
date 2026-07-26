package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [TourTooltipPlacement] — the pure layout math that keeps the tour
 * tooltip card on-screen after advancing from the welcome modal (step 1 → 2).
 *
 * Regression: a near-full-screen spotlight target previously computed `belowTop`
 * past the bottom of the viewport, so the Avanti/Next transition left a broken
 * scrim with no visible tooltip or buttons.
 */
class TourTooltipPlacementTest {

    private val tooltipHeight = 260f
    private val cardGap = 16f
    private val screenHeight = 900f
    private val minTop = TourTooltipPlacement.DEFAULT_MIN_TOP_PX

    @Test
    fun placesBelowSpotlight_whenNotEnoughRoomAbove() {
        // Spotlight near the top — card should go below it.
        val spotlight = Rect(left = 20f, top = 100f, right = 380f, bottom = 220f)
        val top = TourTooltipPlacement.tooltipTopPx(
            spotlightRect = spotlight,
            tooltipHeightPx = tooltipHeight,
            cardGapPx = cardGap,
            screenHeightPx = screenHeight,
        )
        assertEquals(spotlight.bottom + cardGap, top)
    }

    @Test
    fun placesAboveSpotlight_whenEnoughRoomAbove() {
        // Spotlight near the bottom (e.g. meal-plan / grocery nav tab).
        val spotlight = Rect(left = 100f, top = 780f, right = 200f, bottom = 860f)
        val top = TourTooltipPlacement.tooltipTopPx(
            spotlightRect = spotlight,
            tooltipHeightPx = tooltipHeight,
            cardGapPx = cardGap,
            screenHeightPx = screenHeight,
        )
        val expectedAbove = spotlight.top - tooltipHeight - cardGap
        assertEquals(expectedAbove, top)
        assertTrue(top > minTop)
    }

    @Test
    fun clampsOnScreen_whenSpotlightIsNearlyFullScreen() {
        // Mirrors the pre-fix bug: TARGET_HOME_HUB was the entire HomeWelcomeContent
        // (fillMaxSize), so belowTop was past the bottom and the card vanished.
        val spotlight = Rect(left = 0f, top = 80f, right = 400f, bottom = 880f)
        val top = TourTooltipPlacement.tooltipTopPx(
            spotlightRect = spotlight,
            tooltipHeightPx = tooltipHeight,
            cardGapPx = cardGap,
            screenHeightPx = screenHeight,
        )
        val maxTop = screenHeight - tooltipHeight
        assertEquals(maxTop, top)
        assertTrue(top + tooltipHeight <= screenHeight)
        assertTrue(top >= minTop)
    }

    @Test
    fun clampsOnScreen_whenSpotlightFillsEntireOverlay() {
        val spotlight = Rect(left = 0f, top = 0f, right = 400f, bottom = screenHeight)
        val top = TourTooltipPlacement.tooltipTopPx(
            spotlightRect = spotlight,
            tooltipHeightPx = tooltipHeight,
            cardGapPx = cardGap,
            screenHeightPx = screenHeight,
        )
        assertEquals(screenHeight - tooltipHeight, top)
    }

    @Test
    fun neverReturnsBelowMinTop() {
        val spotlight = Rect(left = 0f, top = 10f, right = 100f, bottom = 40f)
        val top = TourTooltipPlacement.tooltipTopPx(
            spotlightRect = spotlight,
            tooltipHeightPx = tooltipHeight,
            cardGapPx = cardGap,
            screenHeightPx = screenHeight,
        )
        assertTrue(top >= minTop)
    }
}
