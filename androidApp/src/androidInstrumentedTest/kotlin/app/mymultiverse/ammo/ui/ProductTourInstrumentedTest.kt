package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.data.tour.ProductTourStore
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourScreenModel
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourTestTags
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourUiState
import app.mymultiverse.ammo.presentation.screens.tour.SpotlightTourOverlay
import app.mymultiverse.ammo.presentation.screens.tour.defaultProductTourSteps
import app.mymultiverse.ammo.presentation.theme.AppTheme
import com.russhwolf.settings.MapSettings
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductTourInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nextFromWelcome_keepsTooltipVisibleForFullHeightTarget() {
        val screenModel = ProductTourScreenModel(ProductTourStore(MapSettings()))
        val steps = defaultProductTourSteps().take(2)

        composeRule.setContent {
            AppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            screenModel.registerCoordinate(
                                ProductTourTestTags.TARGET_HOME_HUB,
                                coordinates.boundsInRoot(),
                            )
                        },
                ) {
                    SpotlightTourOverlay(screenModel = screenModel)
                }
            }
        }

        composeRule.runOnIdle {
            screenModel.maybeShowTour(versionKey = "instrumented-tour", steps = steps)
        }
        composeRule.onNodeWithTag(ProductTourTestTags.TOOLTIP_CARD)
            .assertIsDisplayed()
        assertTooltipIsInsideOverlay()

        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_NEXT).performClick()
        composeRule.waitUntil {
            (screenModel.uiState.value as? ProductTourUiState.Active)?.currentIndex == 1
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(ProductTourTestTags.TOOLTIP_CARD)
            .assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_PREVIOUS)
            .assertIsDisplayed()
        assertTooltipIsInsideOverlay()
    }

    private fun assertTooltipIsInsideOverlay() {
        val overlayBounds = composeRule.onNodeWithTag(ProductTourTestTags.OVERLAY)
            .fetchSemanticsNode()
            .boundsInRoot
        val tooltipBounds = composeRule.onNodeWithTag(ProductTourTestTags.TOOLTIP_CARD)
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(
            "Tooltip top ${tooltipBounds.top} must be within overlay top ${overlayBounds.top}",
            tooltipBounds.top >= overlayBounds.top,
        )
        assertTrue(
            "Tooltip bottom ${tooltipBounds.bottom} must be within overlay bottom ${overlayBounds.bottom}",
            tooltipBounds.bottom <= overlayBounds.bottom,
        )
    }
}
