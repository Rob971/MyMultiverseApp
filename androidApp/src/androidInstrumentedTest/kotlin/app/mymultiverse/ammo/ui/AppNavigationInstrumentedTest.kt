package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.data.nutrition.GroceryGhostPairingDismissStore
import app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature
import com.russhwolf.settings.MapSettings
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.presentation.navigation.NavigationTestTags
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionHubScreen
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.ammo.presentation.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun nutritionScreenModel(): NutritionScreenModel {
        val repository = InstrumentedNutritionRepository(WeekCalendar.currentWeekKey())
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        return NutritionScreenModel(
            session = InstrumentedNutritionSessionCoordinator(repository),
            householdRepository = InstrumentedHouseholdRepository(),
            collaborationRepository = InstrumentedHouseholdCollaborationRepository(),
            aiAssistant = InstrumentedNutritionAdviceService(),
            ghostPairingDismissStore = GroceryGhostPairingDismissStore(MapSettings()),
            scope = scope,
            newItemId = { "instrumented-nav-item" },
        )
    }

    @Test
    fun nutritionHub_backButton_invokesOnBack() {
        var backPressed = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    NutritionHubScreen(
                        householdName = "Our household",
                        enabledFeatures = setOf(
                            NutritionSharingFeature.Grocery,
                            NutritionSharingFeature.MealPlan,
                            NutritionSharingFeature.AiAdvice,
                        ),
                        onBack = { backPressed = true },
                        onOpenSection = {},
                        screenModel = nutritionScreenModel(),
                    )
                }
            }
        }

        composeRule.onNodeWithTag(NavigationTestTags.BACK_BUTTON).performClick()

        assertTrue(backPressed)
    }
}
