package app.mymultiverse.kmp.ui

import androidx.appcompat.R
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.GroceryInputBarTestTags
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.screens.home.HomeContent
import app.mymultiverse.kmp.presentation.screens.home.HomeTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.GroceryListTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.GroceryShoppingScreen
import app.mymultiverse.kmp.presentation.components.MealPlanTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionAiAdviceScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionAiTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionHubScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionHubTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.kmp.presentation.screens.nutrition.WeeklyMealPlanScreen
import app.mymultiverse.kmp.presentation.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NutritionUxInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<AppCompatActivity>()

    private fun setAppCompatTheme() {
        composeRule.activity.setTheme(R.style.Theme_AppCompat_Light_NoActionBar)
    }

    private fun nutritionScreenModel(
        weekKey: String = WeekCalendar.currentWeekKey(),
        itemId: String = "instrumented-item-1",
        adviceAnswer: String = "Eat more vegetables.",
    ): NutritionScreenModel {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        return NutritionScreenModel(
            repository = InstrumentedNutritionRepository(weekKey),
            aiAssistant = InstrumentedNutritionAdviceService(adviceAnswer),
            scope = scope,
            newItemId = { itemId },
        )
    }

    @Test
    fun grocery_addItem_showsInList() {
        setAppCompatTheme()
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Milk").assertIsDisplayed()
    }

    @Test
    fun grocery_toggleItem_marksItemChecked() {
        setAppCompatTheme()
        val itemId = "toggle-item"
        val screenModel = nutritionScreenModel(itemId = itemId)

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Bread")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("${GroceryListTestTags.CHECKBOX_PREFIX}$itemId")
            .performClick()
        composeRule.waitForIdle()

        assertTrue(screenModel.groceryItems.value.single().isChecked)
    }

    @Test
    fun mealPlan_editLunch_persistsInField() {
        setAppCompatTheme()
        val weekKey = WeekCalendar.currentWeekKey()
        val dayIndex = WeekCalendar.todayIndexInWeek(weekKey) ?: 0
        val screenModel = nutritionScreenModel(weekKey = weekKey)

        composeRule.setContent {
            AppTheme {
                WeeklyMealPlanScreen(onBack = {}, screenModel = screenModel)
            }
        }

        val lunchField = composeRule.onNodeWithTag(MealPlanTestTags.lunchField(dayIndex))
        lunchField.performScrollTo()
        lunchField.performTextInput("Pasta salad")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Pasta salad").assertIsDisplayed()
        assertEquals("Pasta salad", screenModel.mealPlan.value.days[dayIndex].lunch)
    }

    @Test
    fun mealPlan_generateLunchGrocery_appendsAiGroceryItems() {
        setAppCompatTheme()
        val weekKey = WeekCalendar.currentWeekKey()
        val dayIndex = WeekCalendar.todayIndexInWeek(weekKey) ?: 0
        val screenModel = nutritionScreenModel(weekKey = weekKey)

        composeRule.setContent {
            AppTheme {
                WeeklyMealPlanScreen(onBack = {}, screenModel = screenModel)
            }
        }

        val lunchField = composeRule.onNodeWithTag(MealPlanTestTags.lunchField(dayIndex))
        lunchField.performScrollTo()
        lunchField.performTextInput("Pasta primavera")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MealPlanTestTags.groceryButton(dayIndex, MealSlot.Lunch))
            .performClick()
        composeRule.waitForIdle()

        assertEquals(3, screenModel.aiGroceryItems.value.size)
        composeRule.onNodeWithText("Tomatoes").assertIsDisplayed()
        composeRule.onNodeWithText("Basil").assertIsDisplayed()
    }

    @Test
    fun nutritionAi_askQuestion_showsAnswer() {
        setAppCompatTheme()
        val answer = "Add leafy greens to every lunch."
        val screenModel = nutritionScreenModel(adviceAnswer = answer)

        composeRule.setContent {
            AppTheme {
                NutritionAiAdviceScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(NutritionAiTestTags.CRITERIA_FIELD)
            .performTextInput("What veggies should we eat?")
        composeRule.onNodeWithTag(NutritionAiTestTags.GENERATE_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(NutritionAiTestTags.ANSWER_CARD).assertIsDisplayed()
        composeRule.onNodeWithText(answer).assertIsDisplayed()
    }

    @Test
    fun nutritionHub_tapGroceryCard_opensGrocerySection() {
        setAppCompatTheme()
        val screenModel = nutritionScreenModel()
        var opened: NutritionSection? = null

        composeRule.setContent {
            AppTheme {
                NutritionHubScreen(
                    onBack = {},
                    onOpenSection = { opened = it },
                    screenModel = screenModel,
                )
            }
        }

        composeRule.onNodeWithTag(NutritionHubTestTags.GROCERY_CARD).performClick()

        assertEquals(NutritionSection.Grocery, opened)
    }

    @Test
    fun homeContent_tapNutritionCard_invokesCallback() {
        setAppCompatTheme()
        var openedNutrition = false

        composeRule.setContent {
            AppTheme {
                HomeContent(
                    greeting = Greeting("Hello"),
                    isRefreshing = false,
                    onRefreshClick = {},
                    onOpenNutrition = { openedNutrition = true },
                )
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.NUTRITION_CARD)
            .performScrollTo()
            .performClick()

        assertTrue(openedNutrition)
    }
}
