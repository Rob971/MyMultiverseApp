package app.mymultiverse.kmp.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import android.view.WindowManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.kmp.data.nutrition.GroceryGhostPairingDismissStore
import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.AiGrocerySuggestionChipsTestTags
import app.mymultiverse.kmp.presentation.components.GroceryGhostPairingTestTags
import app.mymultiverse.kmp.presentation.components.GroceryInputBarTestTags
import app.mymultiverse.kmp.presentation.components.GroceryItemRowTestTags
import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode
import app.mymultiverse.kmp.presentation.components.MealPlanEmptyStateTestTags
import app.mymultiverse.kmp.presentation.components.MealPlanTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.GroceryListTestTags
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.screens.home.HomeTestTags
import app.mymultiverse.kmp.presentation.screens.home.HomeWelcomeContent
import app.mymultiverse.kmp.presentation.screens.home.HomeNutritionSummary
import app.mymultiverse.kmp.presentation.components.HomePrimaryActionsTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.AiHelperSheet
import app.mymultiverse.kmp.presentation.screens.nutrition.AiHelperLaunchContext
import app.mymultiverse.kmp.presentation.screens.nutrition.AiHelperSheetTestTags
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
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.unit.dp

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
        plannedLunch: Pair<Int, String>? = null,
    ): NutritionScreenModel {
        val repository = InstrumentedNutritionRepository(weekKey)
        repository.aiGrocery.value = initialAiGrocery
        plannedLunch?.let { (dayIndex, lunch) ->
            repository.mealPlan.value = repository.mealPlan.value.copy(
                days = repository.mealPlan.value.days.toMutableList().apply {
                    this[dayIndex] = this[dayIndex].copy(lunch = lunch)
                },
            )
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        var nextItemId = 0
        return NutritionScreenModel(
            session = InstrumentedNutritionSessionCoordinator(repository),
            householdRepository = InstrumentedHouseholdRepository(),
            collaborationRepository = InstrumentedHouseholdCollaborationRepository(),
            aiAssistant = InstrumentedNutritionAdviceService(adviceAnswer),
            ghostPairingDismissStore = GroceryGhostPairingDismissStore(MapSettings()),
            scope = scope,
            newItemId = {
                if (nextItemId == 0) {
                    nextItemId++
                    itemId
                } else {
                    "$itemId-${nextItemId++}"
                }
            },
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
    fun grocery_ghostPairingBanner_addsSuggestedItems() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Tortillas")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.ghostPairingOffer) { it != null }

        composeRule.onNodeWithTag(GroceryGhostPairingTestTags.ROOT).assertIsDisplayed()
        composeRule.onNodeWithTag(GroceryGhostPairingTestTags.ACTION).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size >= 4 }

        composeRule.onNodeWithText("Salsa").assertIsDisplayed()
        composeRule.onNodeWithText("Cheese").assertIsDisplayed()
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
    fun grocery_editButton_meetsMinimumTouchTarget() {
        val itemId = "instrumented-item-1"
        val screenModel = nutritionScreenModel(itemId = itemId)

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        val editButtonTag = "${GroceryItemRowTestTags.EDIT_BUTTON_PREFIX}$itemId"
        composeRule.onNodeWithTag(editButtonTag)
            .performScrollTo()
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
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
                WeeklyMealPlanScreen(
                    onBack = {},
                    onOpenSection = { _, _ -> },
                    screenModel = screenModel,
                )
            }
        }

        composeRule.onNodeWithTag(MealPlanTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(MealPlanTestTags.lunchField(dayIndex)))
        val lunchField = composeRule.onNodeWithTag(MealPlanTestTags.lunchField(dayIndex))
        lunchField.performClick()
        lunchField.performTextInput("Pasta primavera")
        composeRule.waitForState(screenModel.mealPlan) { it.days[dayIndex].lunch == "Pasta primavera" }

        composeRule.onNodeWithTag(MealPlanTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(MealPlanTestTags.groceryButton(dayIndex, MealSlot.Lunch)))
        composeRule.onNodeWithTag(MealPlanTestTags.groceryButton(dayIndex, MealSlot.Lunch))
            .performClick()
        composeRule.waitForState(screenModel.aiGroceryItems) { it.size == 3 }

        composeRule.onNodeWithTag(MealPlanTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(AiGrocerySuggestionChipsTestTags.ROW))
        composeRule.onNodeWithTag(AiGrocerySuggestionChipsTestTags.ROW)
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
                WeeklyMealPlanScreen(
                    onBack = {},
                    onOpenSection = { _, _ -> },
                    screenModel = screenModel,
                )
            }
        }

        composeRule.onNodeWithTag(MealPlanTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(AiGrocerySuggestionChipsTestTags.chip("ai-basil")))
        composeRule.onNodeWithTag(AiGrocerySuggestionChipsTestTags.chip("ai-basil"))
            .performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.any { item -> item.label == "Basil" } }
        composeRule.waitForState(screenModel.aiGroceryItems) { it.isEmpty() }
    }

    @Test
    fun nutritionAi_idle_showsEmptyStatePrompt() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                NutritionAiAdviceScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(NutritionAiTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(NutritionAiTestTags.IDLE_EMPTY))
        composeRule.onNodeWithTag(NutritionAiTestTags.IDLE_EMPTY).assertIsDisplayed()
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
                    householdName = "Test Family",
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
                    householdName = "Test Family",
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
                    householdName = "Test Family",
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
    fun homeContent_tapFamilyHero_invokesCallback() {
        var openedHousehold = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("Hello"),
                        userDisplayName = "Test User",
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
                        onOpenHouseholdMembers = { openedHousehold = true },
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.FAMILY)
            .performScrollTo()
            .performClick()

        assertTrue(openedHousehold)
    }

    @Test
    fun homeContent_showsWeekContextBanner_whenWeekKeyPresent() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("Hello"),
                        userDisplayName = "Test User",
                        nutritionSummary = HomeNutritionSummary(
                            weekKey = WeekCalendar.currentWeekKey(),
                            groceryProgress = null,
                            plannedMealSlots = 0,
                        ),
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.WEEK_CONTEXT_BANNER)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun homeContent_hidesWeekContextBanner_whenNutritionSummaryMissing() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("Hello"),
                        userDisplayName = "Test User",
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.WEEK_CONTEXT_BANNER)
            .assertDoesNotExist()
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
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = { openedNutrition = true },
                        onOpenGrocery = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.PLAN)
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
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
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
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
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
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
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
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.LOADING_INDICATOR).assertIsDisplayed()
        composeRule.onNodeWithText("Loading your week…").assertIsDisplayed()
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
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.LOADING_INDICATOR).assertIsDisplayed()
        composeRule.onNodeWithText("Loading your week…").assertIsDisplayed()
    }

    @Test
    fun homeContent_withGreeting_showsInspirationText() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("Small steps keep the week calm."),
                        userDisplayName = "Roberto",
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
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
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
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
        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.PLAN)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun aiHelperSheet_whenVisible_showsMealPlanMode() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                AiHelperSheet(
                    visible = true,
                    launchContext = AiHelperLaunchContext(mode = NutritionAiMode.MealPlan),
                    onDismiss = {},
                    onApplied = {},
                    screenModel = screenModel,
                )
            }
        }

        composeRule.onNodeWithTag(AiHelperSheetTestTags.SHEET).assertIsDisplayed()
        composeRule.onNodeWithTag(NutritionAiTestTags.MODE_MEAL_PLAN).assertDoesNotExist()
        composeRule.onNodeWithTag(NutritionAiTestTags.MORE_OPTIONS_TOGGLE).assertIsDisplayed()
    }

    @Test
    fun mealPlan_emptyState_chip_opensHelperSheet() {
        val screenModel = nutritionScreenModel()
        val sheetVisible = mutableStateOf(false)
        val launchContext = mutableStateOf(AiHelperLaunchContext(mode = NutritionAiMode.MealPlan))

        composeRule.setContent {
            AppTheme {
                Box {
                    WeeklyMealPlanScreen(
                        onBack = {},
                        onOpenSection = { _, _ -> },
                        onOpenAiSheet = { context ->
                            launchContext.value = context
                            sheetVisible.value = true
                        },
                        screenModel = screenModel,
                    )
                    AiHelperSheet(
                        visible = sheetVisible.value,
                        launchContext = launchContext.value,
                        onDismiss = { sheetVisible.value = false },
                        onApplied = { sheetVisible.value = false },
                        screenModel = screenModel,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(MealPlanTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(MealPlanEmptyStateTestTags.CHIP_PROTEIN))
        composeRule.onNodeWithTag(MealPlanEmptyStateTestTags.CHIP_PROTEIN).performClick()
        composeRule.onNodeWithTag(AiHelperSheetTestTags.SHEET).assertIsDisplayed()
    }

    @Test
    fun mealPlan_planWithAi_opensHelperSheet() {
        val screenModel = nutritionScreenModel()
        val sheetVisible = mutableStateOf(false)
        val launchContext = mutableStateOf(AiHelperLaunchContext(mode = NutritionAiMode.MealPlan))

        composeRule.setContent {
            AppTheme {
                Box {
                    WeeklyMealPlanScreen(
                        onBack = {},
                        onOpenSection = { _, _ -> },
                        onOpenAiSheet = { context ->
                            launchContext.value = context
                            sheetVisible.value = true
                        },
                        screenModel = screenModel,
                    )
                    AiHelperSheet(
                        visible = sheetVisible.value,
                        launchContext = launchContext.value,
                        onDismiss = { sheetVisible.value = false },
                        onApplied = { sheetVisible.value = false },
                        screenModel = screenModel,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(MealPlanTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(MealPlanEmptyStateTestTags.PLAN_WITH_AI))
        composeRule.onNodeWithTag(MealPlanEmptyStateTestTags.PLAN_WITH_AI).performClick()
        composeRule.onNodeWithTag(AiHelperSheetTestTags.SHEET).assertIsDisplayed()
    }

    @Test
    fun mealPlan_emptySlot_suggestQuickMeal_opensHelperSheet() {
        val weekKey = WeekCalendar.currentWeekKey()
        val dayIndex = WeekCalendar.todayIndexInWeek(weekKey) ?: 0
        val screenModel = nutritionScreenModel(weekKey = weekKey)
        val sheetVisible = mutableStateOf(false)
        val launchContext = mutableStateOf(AiHelperLaunchContext(mode = NutritionAiMode.MealPlan))

        composeRule.setContent {
            AppTheme {
                Box {
                    WeeklyMealPlanScreen(
                        onBack = {},
                        onOpenSection = { _, _ -> },
                        onOpenAiSheet = { context ->
                            launchContext.value = context
                            sheetVisible.value = true
                        },
                        screenModel = screenModel,
                    )
                    AiHelperSheet(
                        visible = sheetVisible.value,
                        launchContext = launchContext.value,
                        onDismiss = { sheetVisible.value = false },
                        onApplied = { sheetVisible.value = false },
                        screenModel = screenModel,
                    )
                }
            }
        }

        if (WeekCalendar.todayIndexInWeek(weekKey) == null) {
            composeRule.onNodeWithTag(MealPlanTestTags.dayHeader(dayIndex))
                .performScrollTo()
                .performClick()
        }
        composeRule.onNodeWithTag(MealPlanTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(MealPlanTestTags.suggestAiButton(dayIndex, MealSlot.Lunch)))
        composeRule.onNodeWithTag(MealPlanTestTags.suggestAiButton(dayIndex, MealSlot.Lunch))
            .performClick()
        composeRule.onNodeWithTag(AiHelperSheetTestTags.SHEET).assertIsDisplayed()
    }

    @Test
    fun grocery_buildFromMealsChip_opensGroceryAiSheet() {
        val weekKey = WeekCalendar.currentWeekKey()
        val dayIndex = WeekCalendar.todayIndexInWeek(weekKey) ?: 0
        val screenModel = nutritionScreenModel(
            weekKey = weekKey,
            plannedLunch = dayIndex to "Pasta primavera",
        )
        val sheetVisible = mutableStateOf(false)
        val launchContext = mutableStateOf(AiHelperLaunchContext(mode = NutritionAiMode.GroceryList))

        composeRule.setContent {
            AppTheme {
                Box {
                    GroceryShoppingScreen(
                        onBack = {},
                        onOpenAiSheet = { context ->
                            launchContext.value = context
                            sheetVisible.value = true
                        },
                        screenModel = screenModel,
                    )
                    AiHelperSheet(
                        visible = sheetVisible.value,
                        launchContext = launchContext.value,
                        onDismiss = { sheetVisible.value = false },
                        onApplied = { sheetVisible.value = false },
                        screenModel = screenModel,
                    )
                }
            }
        }

        composeRule.waitForState(screenModel.mealPlan) { plan ->
            plan.days.any { day -> day.lunch.isNotBlank() || day.dinner.isNotBlank() }
        }
        composeRule.onNodeWithTag(GroceryListTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(GroceryListTestTags.BUILD_FROM_MEALS))
        composeRule.onNodeWithTag(GroceryListTestTags.BUILD_FROM_MEALS).performClick()
        composeRule.onNodeWithTag(AiHelperSheetTestTags.SHEET).assertIsDisplayed()
        composeRule.onNodeWithTag(NutritionAiTestTags.MODE_GROCERY).assertDoesNotExist()
    }

    @Test
    fun grocery_swipeRight_checksItem() {
        val itemId = "instrumented-item-1"
        val screenModel = nutritionScreenModel(itemId = itemId)

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        composeRule.onNodeWithTag("${GroceryItemRowTestTags.ROW_PREFIX}$itemId")
            .performScrollTo()
            .performTouchInput { swipeRight() }
        composeRule.waitForState(screenModel.groceryItems) { it.single().isChecked }
    }

    @Test
    fun groceryScreen_enablesKeepScreenOnFlag() {
        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = nutritionScreenModel())
            }
        }

        val flags = composeRule.activity.window.attributes.flags
        assertTrue(flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0)
    }

    @Test
    fun groceryScreen_keepScreenOnToggle_disablesFlag() {
        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = nutritionScreenModel())
            }
        }

        composeRule.onNodeWithTag(GroceryListTestTags.KEEP_SCREEN_ON_TOGGLE).performClick()
        composeRule.waitForIdle()

        val flags = composeRule.activity.window.attributes.flags
        assertTrue(flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON == 0)
    }

    @Test
    fun homeWelcome_emptyGrocery_showsAddFirstItemCta() {
        var openedGrocery = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("ready"),
                        userDisplayName = "Test User",
                        nutritionSummary = HomeNutritionSummary(
                            weekKey = WeekCalendar.currentWeekKey(),
                            groceryProgress = null,
                            plannedMealSlots = 0,
                        ),
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = { openedGrocery = true },
                        onOpenHouseholdMembers = {},
                        greetingHour = 9,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.GROCERY)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        assertTrue(openedGrocery)
    }
}
