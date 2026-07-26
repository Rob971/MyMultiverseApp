package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [TourSpotlightPlacement] — guards the regression where step 2 after
 * "Next"/"Avanti" used an oversized home-screen target, clearing the entire scrim and
 * pushing the tooltip off-screen.
 */
class TourSpotlightPlacementTest {

    private val phoneWidth = 1080f
    private val phoneHeight = 1920f

    // ─── usableSpotlightRect ─────────────────────────────────────────────────

    @Test
    fun usableSpotlightRect_returnsNull_whenRectIsNull() {
        assertNull(
            TourSpotlightPlacement.usableSpotlightRect(
                rect = null,
                screenWidthPx = phoneWidth,
                screenHeightPx = phoneHeight,
            ),
        )
    }

    @Test
    fun usableSpotlightRect_returnsNull_whenTargetFillsAlmostEntireScreen() {
        // Reproduces the pre-fix TARGET_HOME_HUB on fillMaxSize Welcome content.
        val fullScreen = Rect(0f, 0f, phoneWidth, phoneHeight)
        assertNull(
            TourSpotlightPlacement.usableSpotlightRect(
                rect = fullScreen,
                screenWidthPx = phoneWidth,
                screenHeightPx = phoneHeight,
            ),
        )
    }

    @Test
    fun usableSpotlightRect_returnsNull_whenHeightExceedsMaxFraction() {
        val tooTall = Rect(
            left = 40f,
            top = 200f,
            right = phoneWidth * 0.4f,
            bottom = 200f + phoneHeight * 0.7f,
        )
        assertNull(
            TourSpotlightPlacement.usableSpotlightRect(
                rect = tooTall,
                screenWidthPx = phoneWidth,
                screenHeightPx = phoneHeight,
            ),
        )
    }

    @Test
    fun usableSpotlightRect_returnsNull_whenAreaExceedsMaxFraction() {
        // Wide and moderately tall — height under the cap, area over it.
        val largeArea = Rect(
            left = 0f,
            top = 100f,
            right = phoneWidth,
            bottom = 100f + phoneHeight * 0.5f,
        )
        assertNull(
            TourSpotlightPlacement.usableSpotlightRect(
                rect = largeArea,
                screenWidthPx = phoneWidth,
                screenHeightPx = phoneHeight,
            ),
        )
    }

    @Test
    fun usableSpotlightRect_keepsCompactDailyHubHeader() {
        // Title + Today/This week tabs only (~120dp tall), full width — post-hardening target.
        val hubHeader = Rect(left = 48f, top = 720f, right = 1032f, bottom = 860f)
        val usable = TourSpotlightPlacement.usableSpotlightRect(
            rect = hubHeader,
            screenWidthPx = phoneWidth,
            screenHeightPx = phoneHeight,
        )
        assertEquals(hubHeader, usable)
    }

    @Test
    fun usableSpotlightRect_keepsCompactHeader_onShortLandscapeHeight() {
        val landscapeHeight = 720f
        val hubHeader = Rect(left = 48f, top = 200f, right = 1032f, bottom = 340f)
        val usable = TourSpotlightPlacement.usableSpotlightRect(
            rect = hubHeader,
            screenWidthPx = phoneWidth,
            screenHeightPx = landscapeHeight,
        )
        assertEquals(hubHeader, usable)
    }

    @Test
    fun usableSpotlightRect_rejectsTallCard_onShortLandscapeHeight() {
        val landscapeHeight = 720f
        // Former whole Daily meal plan card (~560px) exceeds 55% of 720.
        val tallCard = Rect(left = 48f, top = 160f, right = 1032f, bottom = 720f)
        assertNull(
            TourSpotlightPlacement.usableSpotlightRect(
                rect = tallCard,
                screenWidthPx = phoneWidth,
                screenHeightPx = landscapeHeight,
            ),
        )
    }

    @Test
    fun usableSpotlightRect_keepsBottomNavTab() {
        val tab = Rect(left = 360f, top = 1750f, right = 540f, bottom = 1900f)
        val usable = TourSpotlightPlacement.usableSpotlightRect(
            rect = tab,
            screenWidthPx = phoneWidth,
            screenHeightPx = phoneHeight,
        )
        assertEquals(tab, usable)
    }

    // ─── tooltipTopOffsetPx ──────────────────────────────────────────────────

    @Test
    fun tooltipTopOffsetPx_returnsNull_whenNoSpotlight_forCenteredModal() {
        assertNull(
            TourSpotlightPlacement.tooltipTopOffsetPx(
                spotlightRect = null,
                tooltipHeightPx = 260f,
                cardGapPx = 16f,
                screenHeightPx = phoneHeight,
            ),
        )
    }

    @Test
    fun tooltipTopOffsetPx_placesCardAbove_whenRoomAboveSpotlight() {
        val lowerSpotlight = Rect(left = 48f, top = 1200f, right = 1032f, bottom = 1500f)
        val offset = TourSpotlightPlacement.tooltipTopOffsetPx(
            spotlightRect = lowerSpotlight,
            tooltipHeightPx = 260f,
            cardGapPx = 16f,
            screenHeightPx = phoneHeight,
        )
        assertNotNull(offset)
        // aboveTop = 1200 - 260 - 16 = 924
        assertEquals(924f, offset)
    }

    @Test
    fun tooltipTopOffsetPx_placesCardBelow_whenSpotlightNearTop() {
        val upperSpotlight = Rect(left = 48f, top = 120f, right = 1032f, bottom = 400f)
        val offset = TourSpotlightPlacement.tooltipTopOffsetPx(
            spotlightRect = upperSpotlight,
            tooltipHeightPx = 260f,
            cardGapPx = 16f,
            screenHeightPx = phoneHeight,
        )
        assertNotNull(offset)
        // belowTop = 400 + 16 = 416
        assertEquals(416f, offset)
    }

    @Test
    fun tooltipTopOffsetPx_clampsOnScreen_whenPreferredBelowWouldOverflow() {
        // Spotlight near the bottom so "below" would push the card past the screen edge.
        val bottomSpotlight = Rect(left = 48f, top = 1700f, right = 200f, bottom = 1850f)
        val tooltipHeight = 400f
        val offset = TourSpotlightPlacement.tooltipTopOffsetPx(
            spotlightRect = bottomSpotlight,
            tooltipHeightPx = tooltipHeight,
            cardGapPx = 16f,
            screenHeightPx = phoneHeight,
        )
        assertNotNull(offset)
        val maxTop = phoneHeight - tooltipHeight
        assertTrue(offset <= maxTop, "offset $offset must be <= maxTop $maxTop")
        assertTrue(offset >= 0f, "offset must stay non-negative")
    }
}
