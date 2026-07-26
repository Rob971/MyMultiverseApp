package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.data.tour.ProductTourStore
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourCatalog
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourScreenModel
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourTestTags
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourUiState
import app.mymultiverse.ammo.presentation.screens.tour.SpotlightTourOverlay
import app.mymultiverse.ammo.presentation.screens.tour.productTourTarget
import app.mymultiverse.ammo.presentation.theme.AppTheme
import app.mymultiverse.ammo.ui.InstrumentedComposeTest.waitFor
import com.russhwolf.settings.MapSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.dsl.module

/**
 * Instrumented contract for the new-user product tour:
 * - All four steps can be completed successfully via Next / Avanti / Done.
 * - Completing marks the tour seen so it does not show again (only once).
 */
@RunWith(AndroidJUnit4::class)
class ProductTourInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nextFromWelcome_keepsOverlayAndAdvancesToHubStep() {
        val store = ProductTourStore(MapSettings())
        val screenModel = ProductTourScreenModel(store = store)
        setTourContent(screenModel)

        composeRule.runOnIdle {
            screenModel.maybeShowTour(
                versionKey = ProductTourCatalog.TOUR_ID,
                steps = ProductTourCatalog.defaultSteps(),
            )
        }

        composeRule.waitFor {
            screenModel.uiState.value is ProductTourUiState.Active &&
                (screenModel.uiState.value as ProductTourUiState.Active).currentIndex == 0
        }
        composeRule.onNodeWithTag(ProductTourTestTags.OVERLAY).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.TOOLTIP_CARD).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_NEXT).assertIsDisplayed()

        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_NEXT).performClick()

        composeRule.waitFor {
            val state = screenModel.uiState.value
            state is ProductTourUiState.Active && state.currentIndex == 1
        }

        val afterNext = screenModel.uiState.value
        assertTrue(afterNext is ProductTourUiState.Active)
        assertEquals(1, (afterNext as ProductTourUiState.Active).currentIndex)
        composeRule.onNodeWithTag(ProductTourTestTags.OVERLAY).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.TOOLTIP_CARD).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_PREVIOUS).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.TARGET_HOME_HUB).assertIsDisplayed()
    }

    @Test
    fun newUser_completesAllFourSteps_onceOnly() {
        val settings = MapSettings()
        val store = ProductTourStore(settings)
        val screenModel = ProductTourScreenModel(store = store)
        setTourContent(screenModel)

        composeRule.runOnIdle {
            screenModel.maybeShowTour(
                versionKey = ProductTourCatalog.TOUR_ID,
                steps = ProductTourCatalog.defaultSteps(),
            )
        }

        // Steps 1–3: Next / Avanti; step 4: Done.
        repeat(ProductTourCatalog.STEP_COUNT) { expectedIndex ->
            composeRule.waitFor {
                val state = screenModel.uiState.value
                state is ProductTourUiState.Active && state.currentIndex == expectedIndex
            }
            composeRule.onNodeWithTag(ProductTourTestTags.OVERLAY).assertIsDisplayed()
            composeRule.onNodeWithTag(ProductTourTestTags.TOOLTIP_CARD).assertIsDisplayed()
            composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_NEXT).assertIsDisplayed()
            composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_NEXT).performClick()
        }

        composeRule.waitFor {
            screenModel.uiState.value is ProductTourUiState.Hidden
        }
        assertTrue(store.hasSeenTour(ProductTourCatalog.TOUR_ID))

        // Only once — same store / tour ID must not activate again.
        composeRule.runOnIdle {
            screenModel.maybeShowTour(
                versionKey = ProductTourCatalog.TOUR_ID,
                steps = ProductTourCatalog.defaultSteps(),
            )
        }
        composeRule.waitFor {
            screenModel.uiState.value is ProductTourUiState.Hidden
        }
        assertFalse(
            screenModel.uiState.value is ProductTourUiState.Active,
        )
    }

    private fun setTourContent(screenModel: ProductTourScreenModel) {
        composeRule.setContent {
            AppTheme {
                KoinApplication(
                    application = {
                        modules(
                            instrumentedKoinModule,
                            module { single { screenModel } },
                        )
                    },
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .size(width = 280.dp, height = 96.dp)
                                .productTourTarget(ProductTourTestTags.TARGET_HOME_HUB)
                                .testTag(ProductTourTestTags.TARGET_HOME_HUB),
                        )
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .productTourTarget(ProductTourTestTags.TARGET_MEAL_PLAN_TAB)
                                .testTag(ProductTourTestTags.TARGET_MEAL_PLAN_TAB),
                        )
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .productTourTarget(ProductTourTestTags.TARGET_GROCERY_TAB)
                                .testTag(ProductTourTestTags.TARGET_GROCERY_TAB),
                        )
                        SpotlightTourOverlay(screenModel = screenModel)
                    }
                }
            }
        }
    }
}
