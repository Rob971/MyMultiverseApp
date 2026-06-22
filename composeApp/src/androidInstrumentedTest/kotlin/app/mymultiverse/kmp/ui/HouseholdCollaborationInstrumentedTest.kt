package app.mymultiverse.kmp.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.GroceryInputBarTestTags
import app.mymultiverse.kmp.presentation.components.HouseholdViewerReadOnlyTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.GroceryShoppingScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.kmp.presentation.screens.nutrition.WeeklyMealPlanScreen
import app.mymultiverse.kmp.presentation.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HouseholdCollaborationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun viewerRole_hidesGroceryInputAndShowsReadOnlyBanner() {
        val weekKey = WeekCalendar.currentWeekKey()
        val repository = InstrumentedNutritionRepository(weekKey)
        val screenModel = NutritionScreenModel(
            session = InstrumentedNutritionSessionCoordinator(repository),
            householdRepository = InstrumentedHouseholdRepository(
                role = HouseholdMemberRole.Viewer,
            ),
            aiAssistant = InstrumentedNutritionAdviceService(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(HouseholdViewerReadOnlyTestTags.BANNER).assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodesWithTag(GroceryInputBarTestTags.INPUT_FIELD)
                .fetchSemanticsNodes().isEmpty(),
        )
        assertTrue(
            composeRule.onAllNodesWithTag(GroceryInputBarTestTags.ADD_BUTTON)
                .fetchSemanticsNodes().isEmpty(),
        )
    }

    @Test
    fun viewerRole_showsReadOnlyBannerOnMealPlan() {
        val weekKey = WeekCalendar.currentWeekKey()
        val repository = InstrumentedNutritionRepository(weekKey)
        val screenModel = NutritionScreenModel(
            session = InstrumentedNutritionSessionCoordinator(repository),
            householdRepository = InstrumentedHouseholdRepository(
                role = HouseholdMemberRole.Viewer,
            ),
            aiAssistant = InstrumentedNutritionAdviceService(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )

        composeRule.setContent {
            AppTheme {
                WeeklyMealPlanScreen(
                    onBack = {},
                    onOpenSection = { _, _ -> },
                    screenModel = screenModel,
                )
            }
        }

        composeRule.onNodeWithTag(HouseholdViewerReadOnlyTestTags.BANNER).assertIsDisplayed()
    }
}
