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
import app.mymultiverse.ammo.presentation.screens.tour.SpotlightTourOverlay
import app.mymultiverse.ammo.presentation.screens.tour.productTourSteps
import app.mymultiverse.ammo.presentation.theme.AppTheme
import com.russhwolf.settings.MapSettings
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductTourInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun next_fromWelcome_keepsTooltipVisibleForFullScreenTarget() {
        val targetTag = ProductTourTestTags.TARGET_HOME_HUB
        val screenModel = ProductTourScreenModel(ProductTourStore(MapSettings()))
        val steps = productTourSteps().take(2)

        composeRule.setContent {
            AppTheme {
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coordinates ->
                                screenModel.registerCoordinate(
                                    stepTag = targetTag,
                                    rect = coordinates.boundsInRoot(),
                                )
                            },
                    )
                    SpotlightTourOverlay(screenModel = screenModel)
                }
            }
        }

        composeRule.runOnIdle {
            screenModel.maybeShowTour(versionKey = "instrumented-test", steps = steps)
        }
        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_NEXT)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals(1, screenModel.uiState.value.let { state ->
                (state as app.mymultiverse.ammo.presentation.screens.tour.ProductTourUiState.Active)
                    .currentIndex
            })
        }
        composeRule.onNodeWithTag(ProductTourTestTags.TOOLTIP_CARD).assertIsDisplayed()
    }
}
