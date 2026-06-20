package app.mymultiverse.kmp.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.AiGrocerySuggestionChipsTestTags
import app.mymultiverse.kmp.presentation.components.GroceryInputBarTestTags
import app.mymultiverse.kmp.presentation.components.MealPlanTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.GroceryListTestTags
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.screens.home.HomeWelcomeContent
import app.mymultiverse.kmp.presentation.screens.home.HomeTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.GroceryShoppingScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionAiAdviceScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionAiState
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionAiTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionHubScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionHubTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.kmp.presentation.screens.nutrition.WeeklyMealPlanScreen
import app.mymultiverse.kmp.presentation.theme.AppTheme
import app.mymultiverse.kmp.ui.InstrumentedComposeTest.waitFor
import app.mymultiverse.kmp.ui.InstrumentedComposeTest.waitForState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose + AppCompat integration smoke tests for nutrition flows.
 *
 * Business rules (toggle grocery, meal edits, duplicate labels, etc.) live in
 * [app.mymultiverse.kmp.presentation.screens.nutrition.NutritionScreenModelTest].
 */
@RunWith(AndroidJUnit4::class)
class NutritionUxInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun nutritionScreenModel(
        weekKey: String = WeekCalendar.currentWeekKey(),
        itemId: String = "instrumented-item-1",
        adviceAnswer: String = "Eat more vegetables.",
        initialAiGrocery: List<GroceryItem> = emptyList(),
    ): NutritionScreenModel {
        val repository = InstrumentedNutritionRepository(weekKey)
        repository.aiGrocery.value = initialAiGrocery
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        return NutritionScreenModel(
            session = InstrumentedNutritionSessionCoordinator(repository),
            householdRepository = InstrumentedHouseholdRepository(),
            aiAssistant = InstrumentedNutritionAdviceService(adviceAnswer),
            scope = scope,
            newItemId = { itemId },
        )
    }

    @Test
    fun grocery_addItem_showsInList() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        composeRule.onNodeWithText("Milk").assertIsDisplayed()
    }

    @Test
    fun grocery_stickyInputBar_isDisplayed() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.STICKY_BAR).assertIsDisplayed()
        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD).assertIsDisplayed()
    }

    @Test
    fun grocery_rowTap_togglesChecked() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        composeRule.onNodeWithText("Milk")
            .performScrollTo()
            .performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.single().isChecked }
    }

    @Test
    fun grocery_adoptAiChip_addsItemAndRemovesChip() {
        val screenModel = nutritionScreenModel(
            itemId = "grocery-new",
            initialAiGrocery = listOf(GroceryItem("ai-salt", "Salt")),
        )

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(AiGrocerySuggestionChipsTestTags.chip("ai-salt"))
            .performScrollTo()
            .performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.any { item -> item.label == "Salt" } }
        composeRule.waitForState(screenModel.aiGroceryItems) { it.isEmpty() }

        composeRule.onNodeWithText("Salt").assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodesWithTag(AiGrocerySuggestionChipsTestTags.chip("ai-salt"))
                .fetchSemanticsNodes()
                .isEmpty(),
        )
    }

    @Test
    fun grocery_clearCheckedAction_removesCompletedItems() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        composeRule.onNodeWithTag("${GroceryListTestTags.CHECKBOX_PREFIX}instrumented-item-1")
            .performScrollTo()
            .performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.single().isChecked }

        composeRule.onNodeWithTag(GroceryListTestTags.CLEAR_CHECKED_ACTION)
            .performScrollTo()
            .performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.isEmpty() }
    }

    @Test
    fun mealPlan_generateLunchGrocery_appendsAiGroceryItems() {
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
        composeRule.waitForState(screenModel.mealPlan) { it.days[dayIndex].lunch == "Pasta primavera" }

        composeRule.onNodeWithTag(MealPlanTestTags.groceryButton(dayIndex, MealSlot.Lunch))
            .performScrollTo()
            .performClick()
        composeRule.waitForState(screenModel.aiGroceryItems) { it.size == 3 }

        composeRule.onNodeWithTag(AiGrocerySuggestionChipsTestTags.ROW)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Tomatoes").assertIsDisplayed()
        composeRule.onNodeWithText("Basil").assertIsDisplayed()
    }

    @Test
    fun mealPlan_adoptAiChip_addsItemToGroceryList() {
        val weekKey = WeekCalendar.currentWeekKey()
        val screenModel = nutritionScreenModel(
            weekKey = weekKey,
            initialAiGrocery = listOf(GroceryItem("ai-basil", "Basil")),
        )

        composeRule.setContent {
            AppTheme {
                WeeklyMealPlanScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(AiGrocerySuggestionChipsTestTags.chip("ai-basil"))
            .performScrollTo()
            .performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.any { item -> item.label == "Basil" } }
        composeRule.waitForState(screenModel.aiGroceryItems) { it.isEmpty() }
    }

    @Test
    fun nutritionAi_askQuestion_showsAnswer() {
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
        composeRule.waitFor { screenModel.aiState.value is NutritionAiState.Advice }

        composeRule.onNodeWithTag(NutritionAiTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(NutritionAiTestTags.ANSWER_CARD))
        composeRule.onNodeWithTag(NutritionAiTestTags.ANSWER_CARD).assertIsDisplayed()
        composeRule.onNodeWithText(answer).assertIsDisplayed()
    }

    @Test
    fun nutritionAi_groceryMode_generatesAndClearsReadOnlyGroceryList() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                NutritionAiAdviceScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(NutritionAiTestTags.MODE_GROCERY)
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag(NutritionAiTestTags.CRITERIA_FIELD)
            .performScrollTo()
            .performTextInput("high protein")
        composeRule.onNodeWithTag(NutritionAiTestTags.GENERATE_BUTTON)
            .performScrollTo()
            .performClick()
        composeRule.waitFor { screenModel.aiState.value is NutritionAiState.GroceryList }
        composeRule.waitForState(screenModel.aiGroceryItems) { it.size == 3 }

        composeRule.onNodeWithTag(NutritionAiTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(NutritionAiTestTags.CLEAR_AI_GROCERY_BUTTON))
        composeRule.onNodeWithTag(NutritionAiTestTags.CLEAR_AI_GROCERY_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForState(screenModel.aiGroceryItems) { it.isEmpty() }
    }

    @Test
    fun nutritionHub_showsGroceryAndMealPlanCards() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                NutritionHubScreen(
                    householdName = "Test household",
                    enabledFeatures = setOf(
                        NutritionSharingFeature.Grocery,
                        NutritionSharingFeature.MealPlan,
                        NutritionSharingFeature.AiAdvice,
                    ),
                    onBack = {},
                    onOpenSection = {},
                    screenModel = screenModel,
                )
            }
        }

        composeRule.onNodeWithTag(NutritionHubTestTags.GROCERY_CARD).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(NutritionHubTestTags.MEAL_PLAN_CARD).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun nutritionHub_tapGroceryCard_opensGrocerySection() {
        val screenModel = nutritionScreenModel()
        var opened: NutritionSection? = null

        composeRule.setContent {
            AppTheme {
                NutritionHubScreen(
                    householdName = "Test household",
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
    fun nutritionHub_tapMealPlanCard_opensMealPlanSection() {
        val screenModel = nutritionScreenModel()
        var opened: NutritionSection? = null

        composeRule.setContent {
            AppTheme {
                NutritionHubScreen(
                    householdName = "Test household",
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

        composeRule.onNodeWithTag(NutritionHubTestTags.MEAL_PLAN_CARD)
            .performScrollTo()
            .performClick()

        assertEquals(NutritionSection.MealPlan, opened)
    }

    @Test
    fun homeContent_tapHouseholdCard_invokesCallback() {
        var openedHousehold = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("Hello"),
                        userDisplayName = "Test User",
                        householdName = "Our household",
                        canRenameHousehold = false,
                        onRenameHousehold = {},
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = { openedHousehold = true },
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.HOUSEHOLD_CARD)
            .performScrollTo()
            .performClick()

        assertTrue(openedHousehold)
    }

    @Test
    fun homeContent_tapNutritionCard_invokesCallback() {
        var openedNutrition = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("Hello"),
                        userDisplayName = "Test User",
                        householdName = null,
                        canRenameHousehold = false,
                        onRenameHousehold = {},
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenNutrition = { openedNutrition = true },
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.NUTRITION_CARD)
            .performScrollTo()
            .performClick()

        assertTrue(openedNutrition)
    }

    @Test
    fun homeContent_withUserDisplayName_showsPersonalizedGreeting() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("ready"),
                        userDisplayName = "Roberto",
                        householdName = null,
                        canRenameHousehold = false,
                        onRenameHousehold = {},
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.GREETING_LINE).assertIsDisplayed()
        composeRule.onNodeWithText("Good morning, Roberto").assertIsDisplayed()
    }

    @Test
    fun homeContent_withEmailDerivedDisplayName_showsPersonalizedGreeting() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("ready"),
                        userDisplayName = "maria",
                        householdName = null,
                        canRenameHousehold = false,
                        onRenameHousehold = {},
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 14,
                    )
                }
            }
        }

        composeRule.onNodeWithText("Good afternoon, maria").assertIsDisplayed()
    }

    @Test
    fun homeContent_withoutDisplayName_showsGenericGreeting() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("ready"),
                        userDisplayName = null,
                        householdName = null,
                        canRenameHousehold = false,
                        onRenameHousehold = {},
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 20,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.GREETING_LINE).assertIsDisplayed()
        composeRule.onNodeWithText("Good evening").assertIsDisplayed()
    }

    @Test
    fun homeContent_withoutGreeting_showsLoadingBannerLine() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = null,
                        userDisplayName = "Roberto",
                        householdName = null,
                        canRenameHousehold = false,
                        onRenameHousehold = {},
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.LOADING_INDICATOR).assertIsDisplayed()
        composeRule.onNodeWithText("Loading inspiration…").assertIsDisplayed()
        composeRule.onNodeWithText("Good morning, Roberto").assertIsDisplayed()
    }

    @Test
    fun homeContent_withoutGreeting_showsInspirationLoadingWhenAnonymous() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = null,
                        userDisplayName = null,
                        householdName = null,
                        canRenameHousehold = false,
                        onRenameHousehold = {},
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.LOADING_INDICATOR).assertIsDisplayed()
        composeRule.onNodeWithText("Loading inspiration…").assertIsDisplayed()
    }

    @Test
    fun homeContent_withGreeting_showsInspirationText() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("Small steps keep the week calm."),
                        userDisplayName = "Roberto",
                        householdName = null,
                        canRenameHousehold = false,
                        onRenameHousehold = {},
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.INSPIRATION_LINE).assertIsDisplayed()
        composeRule.onNodeWithText("Small steps keep the week calm.").assertIsDisplayed()
    }

    @Test
    fun homeContent_withGreetingWhileRefreshing_hidesLoadingIndicator() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("Hello"),
                        userDisplayName = "Test User",
                        householdName = null,
                        canRenameHousehold = false,
                        onRenameHousehold = {},
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        assertTrue(
            composeRule.onAllNodesWithTag(HomeTestTags.LOADING_INDICATOR)
                .fetchSemanticsNodes().isEmpty(),
        )
        composeRule.onNodeWithTag(HomeTestTags.INSPIRATION_LINE).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTestTags.NUTRITION_CARD)
            .performScrollTo()
            .assertIsDisplayed()
    }
}
