package app.mymultiverse.kmp.ui

import androidx.appcompat.R
import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.GroceryInputBarTestTags
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
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
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionAiState
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
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun setAppCompatTheme() {
        composeRule.activity.setTheme(R.style.Theme_AppCompat_Light_NoActionBar)
    }

    private fun nutritionScreenModel(
        weekKey: String = WeekCalendar.currentWeekKey(),
        itemId: String = "instrumented-item-1",
        adviceAnswer: String = "Eat more vegetables.",
    ): NutritionScreenModel {
        val repository = InstrumentedNutritionRepository(weekKey)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        return NutritionScreenModel(
            session = InstrumentedNutritionSessionCoordinator(repository),
            aiAssistant = InstrumentedNutritionAdviceService(adviceAnswer),
            scope = scope,
            newItemId = { itemId },
        )
    }

    private fun waitForCondition(
        timeoutMillis: Long = 10_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            composeRule.waitForIdle()
            if (condition()) return
            Thread.sleep(50)
        }
        composeRule.waitForIdle()
        check(condition()) { "Condition not met within ${timeoutMillis}ms" }
    }

    private fun waitForGroceryCount(screenModel: NutritionScreenModel, count: Int) {
        waitForCondition { screenModel.groceryItems.value.size == count }
    }

    private fun waitForMealPlanLunch(
        screenModel: NutritionScreenModel,
        dayIndex: Int,
        lunch: String,
    ) {
        waitForCondition {
            screenModel.mealPlan.value.days[dayIndex].lunch == lunch
        }
    }

    private fun waitForAiGroceryCount(screenModel: NutritionScreenModel, count: Int) {
        waitForCondition { screenModel.aiGroceryItems.value.size == count }
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
        waitForGroceryCount(screenModel, 1)

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
        waitForGroceryCount(screenModel, 1)

        composeRule.onNodeWithTag("${GroceryListTestTags.CHECKBOX_PREFIX}$itemId")
            .performClick()
        waitForCondition {
            screenModel.groceryItems.value.singleOrNull()?.isChecked == true
        }

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
        waitForMealPlanLunch(screenModel, dayIndex, "Pasta salad")

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
        waitForMealPlanLunch(screenModel, dayIndex, "Pasta primavera")

        composeRule.onNodeWithTag(MealPlanTestTags.groceryButton(dayIndex, MealSlot.Lunch))
            .performScrollTo()
            .performClick()
        waitForAiGroceryCount(screenModel, 3)

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
            .performScrollTo()
            .performTextInput("What veggies should we eat?")
        composeRule.onNodeWithTag(NutritionAiTestTags.GENERATE_BUTTON)
            .performScrollTo()
            .performClick()
        waitForCondition { screenModel.aiState.value is NutritionAiState.Advice }

        composeRule.onNodeWithTag(NutritionAiTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(NutritionAiTestTags.ANSWER_CARD))
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
                    spaceName = "Test space",
                    enabledFeatures = setOf(
                        NutritionSharingFeature.Grocery,
                        NutritionSharingFeature.MealPlan,
                        NutritionSharingFeature.AiAdvice,
                    ),
                    onBack = {},
                    onOpenSection = { opened = it },
                    screenModel = screenModel,
                )
            }
        }

        composeRule.onNodeWithTag(NutritionHubTestTags.GROCERY_CARD)
            .performScrollTo()
            .performClick()

        assertEquals(NutritionSection.Grocery, opened)
    }

    @Test
    fun homeContent_tapNutritionCard_invokesCallback() {
        setAppCompatTheme()
        var openedNutrition = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeContent(
                        greeting = Greeting("Hello"),
                        isRefreshing = false,
                        onRefreshClick = {},
                        onOpenNutrition = { openedNutrition = true },
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.NUTRITION_CARD)
            .performScrollTo()
            .performClick()

        assertTrue(openedNutrition)
    }
}
