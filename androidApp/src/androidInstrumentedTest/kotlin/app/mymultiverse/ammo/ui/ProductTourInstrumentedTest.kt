package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.data.tour.ProductTourStore
import app.mymultiverse.ammo.presentation.screens.home.HomeWelcomeContent
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourScreenModel
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourStep
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourTestTags
import app.mymultiverse.ammo.presentation.screens.tour.SpotlightTourOverlay
import app.mymultiverse.ammo.presentation.theme.AppTheme
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.tour_step_home_body
import ammo.composeapp.generated.resources.tour_step_home_title
import ammo.composeapp.generated.resources.tour_step_welcome_body
import ammo.composeapp.generated.resources.tour_step_welcome_title
import com.russhwolf.settings.MapSettings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class ProductTourInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tappingNextFromWelcome_keepsTourVisibleOnHomeHubStep() {
        val model = ProductTourScreenModel(
            store = ProductTourStore(MapSettings()),
        )
        val steps = listOf(
            ProductTourStep(
                id = "welcome",
                title = Res.string.tour_step_welcome_title,
                description = Res.string.tour_step_welcome_body,
            ),
            ProductTourStep(
                id = "home_hub",
                title = Res.string.tour_step_home_title,
                description = Res.string.tour_step_home_body,
                targetTag = ProductTourTestTags.TARGET_HOME_HUB,
            ),
        )

        composeRule.setContent {
            KoinApplication(application = { modules(module { single { model } }) }) {
                AppTheme {
                    LaunchedEffect(Unit) {
                        model.maybeShowTour(versionKey = "tour-regression", steps = steps)
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        HomeWelcomeContent(
                            greeting = null,
                            userDisplayName = "Roberto",
                            nutritionSummary = null,
                            isRefreshing = false,
                            onRefresh = {},
                            onOpenMealPlan = {},
                            onOpenGrocery = {},
                            onOpenHouseholdMembers = {},
                            modifier = Modifier.fillMaxSize(),
                        )
                        SpotlightTourOverlay(screenModel = model)
                    }
                }
            }
        }

        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_NEXT).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_NEXT).performClick()
        composeRule.onNodeWithTag(ProductTourTestTags.TOOLTIP_CARD).assertIsDisplayed()
    }
}
