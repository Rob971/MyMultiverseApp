package app.mymultiverse.kmp.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
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
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.GroceryInputBarTestTags
import app.mymultiverse.kmp.presentation.components.MealPlanTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.GroceryListTestTags
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.screens.home.HomeContent
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

        composeRule.onNodeWithText("Tomatoes").assertIsDisplayed()
        composeRule.onNodeWithText("Basil").assertIsDisplayed()
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

        composeRule.onNodeWithTag(NutritionAiTestTags.CLEAR_AI_GROCERY_BUTTON)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForState(screenModel.aiGroceryItems) { it.isEmpty() }
    }

    @Test
    fun nutritionHub_showsAllThreeCategoryCards() {
        val screenModel = nutritionScreenModel()

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
                    onOpenSection = {},
                    screenModel = screenModel,
                )
            }
        }

        composeRule.onNodeWithTag(NutritionHubTestTags.GROCERY_CARD).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(NutritionHubTestTags.MEAL_PLAN_CARD).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(NutritionHubTestTags.AI_CARD).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun nutritionHub_tapGroceryCard_opensGrocerySection() {
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
    fun nutritionHub_tapMealPlanCard_opensMealPlanSection() {
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

        composeRule.onNodeWithTag(NutritionHubTestTags.MEAL_PLAN_CARD)
            .performScrollTo()
            .performClick()

        assertEquals(NutritionSection.MealPlan, opened)
    }

    @Test
    fun nutritionHub_tapAiCard_opensAiAdviceSection() {
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

        composeRule.onNodeWithTag(NutritionHubTestTags.AI_CARD)
            .performScrollTo()
            .performClick()

        assertEquals(NutritionSection.AiAdvice, opened)
    }

    @Test
    fun homeContent_tapHouseholdCard_invokesCallback() {
        var openedHousehold = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeContent(
                        greeting = Greeting("Hello"),
                        userDisplayName = "Test User",
                        householdName = "Our household",
                        isRefreshing = false,
                        pendingInvites = emptyList(),
                        onRefreshClick = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = { openedHousehold = true },
                        onSignOut = {},
                        onAcceptInvite = {},
                        onDeclineInvite = {},
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
                    HomeContent(
                        greeting = Greeting("Hello"),
                        userDisplayName = "Test User",
                        householdName = null,
                        isRefreshing = false,
                        pendingInvites = emptyList(),
                        onRefreshClick = {},
                        onOpenNutrition = { openedNutrition = true },
                        onOpenHouseholdMembers = {},
                        onSignOut = {},
                        onAcceptInvite = {},
                        onDeclineInvite = {},
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
                    HomeContent(
                        greeting = Greeting("ready"),
                        userDisplayName = "Roberto",
                        householdName = null,
                        isRefreshing = false,
                        pendingInvites = emptyList(),
                        onRefreshClick = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        onSignOut = {},
                        onAcceptInvite = {},
                        onDeclineInvite = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.GREETING_LINE).assertIsDisplayed()
        composeRule.onNodeWithText("Hello, Roberto").assertIsDisplayed()
    }

    @Test
    fun homeContent_withEmailDerivedDisplayName_showsPersonalizedGreeting() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeContent(
                        greeting = Greeting("ready"),
                        userDisplayName = "maria",
                        householdName = null,
                        isRefreshing = false,
                        pendingInvites = emptyList(),
                        onRefreshClick = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        onSignOut = {},
                        onAcceptInvite = {},
                        onDeclineInvite = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("Hello, maria").assertIsDisplayed()
    }

    @Test
    fun homeContent_withoutDisplayName_showsGenericGreeting() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeContent(
                        greeting = Greeting("ready"),
                        userDisplayName = null,
                        householdName = null,
                        isRefreshing = false,
                        pendingInvites = emptyList(),
                        onRefreshClick = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        onSignOut = {},
                        onAcceptInvite = {},
                        onDeclineInvite = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.GREETING_LINE).assertIsDisplayed()
        composeRule.onNodeWithText("The beating heart of our family.").assertIsDisplayed()
    }

    @Test
    fun homeContent_withoutGreeting_showsLoadingBannerLine() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeContent(
                        greeting = null,
                        userDisplayName = "Roberto",
                        householdName = null,
                        isRefreshing = false,
                        pendingInvites = emptyList(),
                        onRefreshClick = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        onSignOut = {},
                        onAcceptInvite = {},
                        onDeclineInvite = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.LOADING_INDICATOR).assertIsDisplayed()
        composeRule.onNodeWithText("Loading your family space...").assertIsDisplayed()
    }

    @Test
    fun homeContent_withoutGreeting_showsLoadingIndicator() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeContent(
                        greeting = null,
                        userDisplayName = null,
                        householdName = null,
                        isRefreshing = false,
                        pendingInvites = emptyList(),
                        onRefreshClick = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        onSignOut = {},
                        onAcceptInvite = {},
                        onDeclineInvite = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    @Test
    fun homeContent_withGreetingWhileRefreshing_hidesLoadingIndicator() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeContent(
                        greeting = Greeting("Hello"),
                        userDisplayName = "Test User",
                        householdName = null,
                        isRefreshing = true,
                        pendingInvites = emptyList(),
                        onRefreshClick = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        onSignOut = {},
                        onAcceptInvite = {},
                        onDeclineInvite = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.LOADING_INDICATOR).assertDoesNotExist()
        composeRule.onNodeWithTag(HomeTestTags.NUTRITION_CARD)
            .performScrollTo()
            .assertIsDisplayed()
    }
}
