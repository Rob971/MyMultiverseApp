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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.dsl.module

/**
 * Smoke coverage for the product-tour regression where Next/Avanti on step 1 left the
 * overlay looking gone (oversized spotlight + off-screen tooltip).
 *
 * Drives [SpotlightTourOverlay] with a compact hub target — not the full app shell —
 * so the assertion is about tour state after Next, not auth/home bootstrap.
 */
@RunWith(AndroidJUnit4::class)
class ProductTourInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nextFromWelcome_keepsOverlayAndAdvancesToHubStep() {
        val store = ProductTourStore(MapSettings())
        val screenModel = ProductTourScreenModel(store = store)
        val steps = ProductTourCatalog.defaultSteps()

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
                        // Compact stand-in for the daily hub header spotlight target.
                        Box(
                            modifier = Modifier
                                .size(width = 280.dp, height = 96.dp)
                                .productTourTarget(ProductTourTestTags.TARGET_HOME_HUB)
                                .testTag(ProductTourTestTags.TARGET_HOME_HUB),
                        )
                        SpotlightTourOverlay(screenModel = screenModel)
                    }
                }
            }
        }

        composeRule.runOnIdle {
            screenModel.maybeShowTour(versionKey = "instrumented-tour", steps = steps)
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

        // Regression guard: Next must not hide the tour (the original "Avanti" failure).
        val afterNext = screenModel.uiState.value
        assertTrue(afterNext is ProductTourUiState.Active)
        assertEquals(1, (afterNext as ProductTourUiState.Active).currentIndex)
        composeRule.onNodeWithTag(ProductTourTestTags.OVERLAY).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.TOOLTIP_CARD).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.BUTTON_PREVIOUS).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductTourTestTags.TARGET_HOME_HUB).assertIsDisplayed()
    }
}
