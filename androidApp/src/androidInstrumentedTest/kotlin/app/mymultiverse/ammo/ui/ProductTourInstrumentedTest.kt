package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.data.tour.ProductTourStore
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourScreenModel
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourStep
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourTestTags
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourUiState
import app.mymultiverse.ammo.presentation.screens.tour.SpotlightTourOverlay
import app.mymultiverse.ammo.presentation.theme.AppTheme
import app.mymultiverse.ammo.ui.InstrumentedComposeTest.waitForState
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.tour_step_home_body
import ammo.composeapp.generated.resources.tour_step_home_title
import ammo.composeapp.generated.resources.tour_step_welcome_body
import ammo.composeapp.generated.resources.tour_step_welcome_title
import com.russhwolf.settings.MapSettings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductTourInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun next_fromWelcome_keepsTooltipVisible_forFullHeightHomeSpotlight() {
        val screenModel = ProductTourScreenModel(ProductTourStore(MapSettings()))
        screenModel.registerCoordinate(
            ProductTourTestTags.TARGET_HOME_HUB,
            Rect(left = 0f, top = 100f, right = 1_080f, bottom = 10_000f),
        )
        screenModel.maybeShowTour(
            versionKey = "instrumented-positioning",
            steps = listOf(
                ProductTourStep(
                    id = "welcome",
                    title = Res.string.tour_step_welcome_title,
                    description = Res.string.tour_step_welcome_body,
                ),
                ProductTourStep(
                    id = "home",
                    title = Res.string.tour_step_home_title,
                    description = Res.string.tour_step_home_body,
                    targetTag = ProductTourTestTags.TARGET_HOME_HUB,
                ),
            ),
        )

        composeRule.setContent {
            AppTheme {
                SpotlightTourOverlay(screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_NEXT)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForState(screenModel.uiState) { state ->
            state is ProductTourUiState.Active && state.currentIndex == 1
        }

        composeRule.onNodeWithTag(ProductTourTestTags.TOOLTIP_CARD).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_PREVIOUS).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_NEXT).assertIsDisplayed()
    }
}
