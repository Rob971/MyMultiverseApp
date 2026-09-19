package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.doubleClick

import android.view.WindowManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.data.nutrition.GroceryGhostPairingDismissStore
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.domain.model.Greeting
import app.mymultiverse.ammo.domain.model.nutrition.FavoriteDish
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.MealSlot
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.presentation.components.GroceryGhostPairingTestTags
import app.mymultiverse.ammo.presentation.components.GroceryInputBarTestTags
import app.mymultiverse.ammo.presentation.components.GroceryItemRowTestTags
import app.mymultiverse.ammo.presentation.components.JourneyEmptyStateTestTags
import app.mymultiverse.ammo.domain.nutrition.NutritionAiMode
import app.mymultiverse.ammo.presentation.components.MealPlanEmptyStateTestTags
import app.mymultiverse.ammo.presentation.components.MealPlanTestTags
import app.mymultiverse.ammo.presentation.screens.nutrition.GroceryListTestTags
import app.mymultiverse.ammo.presentation.navigation.NutritionSection
import app.mymultiverse.ammo.presentation.screens.home.HomeTestTags
import app.mymultiverse.ammo.presentation.screens.home.HomeWelcomeContent
import app.mymultiverse.ammo.presentation.screens.home.HomeNutritionSummary
import app.mymultiverse.ammo.presentation.components.HomePrimaryActionsTestTags
import app.mymultiverse.ammo.presentation.screens.nutrition.AiHelperSheet
import app.mymultiverse.ammo.presentation.screens.nutrition.AiHelperLaunchContext
import app.mymultiverse.ammo.presentation.screens.nutrition.AiHelperSheetTestTags
import app.mymultiverse.ammo.presentation.screens.nutrition.GroceryShoppingScreen
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionAiAdviceScreen
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionAiState
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionAiTestTags
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionHubScreen
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionHubTestTags
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.ammo.presentation.screens.nutrition.WeeklyMealPlanScreen
import app.mymultiverse.ammo.presentation.theme.AppTheme
import app.mymultiverse.ammo.ui.InstrumentedComposeTest.waitFor
import app.mymultiverse.ammo.ui.InstrumentedComposeTest.waitForState
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CompletableDeferred
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
 * [app.mymultiverse.ammo.presentation.screens.nutrition.NutritionScreenModelTest].
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
        householdId: String? = null,
        favorites: InstrumentedFavoriteDishesRepository = InstrumentedFavoriteDishesRepository(),
        repository: InstrumentedNutritionRepository = InstrumentedNutritionRepository(weekKey, householdId = householdId),
    ): NutritionScreenModel {
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
            favoriteDishesRepository = favorites,
            ghostPairingDismissStore = GroceryGhostPairingDismissStore(MapSettings()),
            logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "instrumented")),
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
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
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
        val screenModel = nutritionScreenModel(householdId = "instrumented-household")

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Tortillas")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.ghostPairingOffer) { it != null }

        composeRule.onNodeWithTag(GroceryGhostPairingTestTags.ROOT).assertIsDisplayed()
        composeRule.onNodeWithTag(GroceryGhostPairingTestTags.ACTION).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size >= 4 }

        composeRule.onNodeWithText("salsa").assertIsDisplayed()
        composeRule.onNodeWithText("cheese").assertIsDisplayed()
    }

    @Test
    fun grocery_stickyInputBar_isDisplayed() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.STICKY_BAR).assertIsDisplayed()
        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD).assertIsDisplayed()
    }

    @Test
    fun grocery_doubleTap_opensEditField() {
        val itemId = "instrumented-item-1"
        val screenModel = nutritionScreenModel(itemId = itemId)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        composeRule.onNodeWithTag("${GroceryItemRowTestTags.ROW_PREFIX}$itemId")
            .performScrollTo()
            .performTouchInput { doubleClick() }

        composeRule.onNodeWithTag("${GroceryItemRowTestTags.EDIT_FIELD_PREFIX}$itemId")
            .assertIsDisplayed()
    }

    @Test
    fun grocery_editButton_opensEditField() {
        val itemId = "instrumented-item-1"
        val screenModel = nutritionScreenModel(itemId = itemId)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        composeRule.onNodeWithTag("${GroceryItemRowTestTags.EDIT_BUTTON_PREFIX}$itemId")
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithTag("${GroceryItemRowTestTags.EDIT_FIELD_PREFIX}$itemId")
            .assertIsDisplayed()
    }

    @Test
    fun grocery_generateFromMeal_addsItemsToModel() {
        val weekKey = WeekCalendar.currentWeekKey()
        val dayIndex = WeekCalendar.todayIndexInWeek(weekKey) ?: 0
        val screenModel = nutritionScreenModel(weekKey = weekKey)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    WeeklyMealPlanScreen(
                        onBack = {},
                        onOpenSection = { _, _ -> },
                        screenModel = screenModel,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(MealPlanTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(MealPlanTestTags.lunchField(dayIndex)))
        val lunchField = composeRule.onNodeWithTag(MealPlanTestTags.lunchField(dayIndex))
        lunchField.performClick()
        lunchField.performTextInput("Soup")
        composeRule.waitForState(screenModel.mealPlan) { it.days[dayIndex].lunch == "Soup" }

        composeRule.onNodeWithTag(MealPlanTestTags.groceryButton(dayIndex, MealSlot.Lunch))
            .performScrollTo()
            .performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.isNotEmpty() }
    }

    @Test
    fun grocery_row_meetsMinimumTouchTarget() {
        val itemId = "instrumented-item-1"
        val screenModel = nutritionScreenModel(itemId = itemId)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        composeRule.onNodeWithTag("${GroceryItemRowTestTags.ROW_PREFIX}$itemId")
            .performScrollTo()
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun grocery_clearCheckedAction_removesCompletedItems() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
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
    fun mealPlan_generateLunchGrocery_appendsGroceryItems() {
        val weekKey = WeekCalendar.currentWeekKey()
        val dayIndex = WeekCalendar.todayIndexInWeek(weekKey) ?: 0
        val screenModel = nutritionScreenModel(weekKey = weekKey)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    WeeklyMealPlanScreen(
                        onBack = {},
                        onOpenSection = { _, _ -> },
                        screenModel = screenModel,
                    )
                }
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
        composeRule.waitForState(screenModel.groceryItems) { it.size == 3 }
    }

    @Test
    fun mealPlan_dailySection_displaysAfterWeekOverview() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    WeeklyMealPlanScreen(
                        onBack = {},
                        onOpenSection = { _, _ -> },
                        screenModel = nutritionScreenModel(),
                    )
                }
            }
        }

        composeRule.onNodeWithTag(MealPlanTestTags.WEEK_OVERVIEW).assertIsDisplayed()
        composeRule.onNodeWithTag(MealPlanTestTags.DAILY_SECTION).assertIsDisplayed()
    }

    @Test
    fun nutritionAi_idle_showsEmptyStatePrompt() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    NutritionAiAdviceScreen(onBack = {}, screenModel = screenModel)
                }
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
                InstrumentedKoinHost {
                    NutritionAiAdviceScreen(onBack = {}, screenModel = screenModel)
                }
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
                InstrumentedKoinHost {
                    NutritionAiAdviceScreen(onBack = {}, screenModel = screenModel)
                }
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
                InstrumentedKoinHost {
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
                InstrumentedKoinHost {
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
                InstrumentedKoinHost {
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
        }

        composeRule.onNodeWithTag(NutritionHubTestTags.MEAL_PLAN_CARD)
            .performScrollTo()
            .performClick()

        assertEquals(NutritionSection.MealPlan, opened)
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

        composeRule.onNodeWithTag(HomeTestTags.DAILY_TAB_THIS_WEEK)
            .performScrollTo()
            .performClick()
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
    fun homeContent_withGreeting_omitsInspirationLineFromDashboard() {
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

        composeRule.onNodeWithTag(HomeTestTags.GREETING_LINE).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTestTags.DAILY_MEAL_PLAN_BLOCK)
            .performScrollTo()
            .assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodesWithTag(HomeTestTags.INSPIRATION_LINE)
                .fetchSemanticsNodes().isEmpty(),
        )
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
        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.PLAN)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTestTags.DAILY_MEAL_PLAN_BLOCK)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun aiHelperSheet_whenVisible_showsMealPlanMode() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    AiHelperSheet(
                        visible = true,
                        launchContext = AiHelperLaunchContext(mode = NutritionAiMode.MealPlan),
                        onDismiss = {},
                        onApplied = {},
                        screenModel = screenModel,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(AiHelperSheetTestTags.SHEET).assertIsDisplayed()
        composeRule.onNodeWithTag(NutritionAiTestTags.MODE_MEAL_PLAN).assertDoesNotExist()
        composeRule.onNodeWithTag(NutritionAiTestTags.MORE_OPTIONS_TOGGLE).assertIsDisplayed()
    }

    @Test
    fun aiHelperSheet_acceptMeal_showsUndoSnackbarAndAcceptedStatus() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    AiHelperSheet(
                        visible = true,
                        launchContext = AiHelperLaunchContext(mode = NutritionAiMode.MealPlan),
                        onDismiss = {},
                        onApplied = {},
                        screenModel = screenModel,
                    )
                }
            }
        }

        composeRule.runOnIdle {
            screenModel.runAiAssistant(
                NutritionAiMode.MealPlan,
                "vegetarian",
                MealPlanGenerationScope.FullWeek,
            )
        }
        composeRule.waitFor { screenModel.aiState.value is NutritionAiState.MealPlanPreview }

        val acceptLunchTag = "${NutritionAiTestTags.MEAL_PLAN_ACCEPT_LUNCH_PREFIX}0"
        composeRule.onNodeWithTag(NutritionAiTestTags.SCROLL_LIST)
            .performScrollToNode(hasTestTag(acceptLunchTag))
        composeRule.onNodeWithTag(acceptLunchTag).performClick()

        // The sheet draws above the screen's Scaffold, so the Undo snackbar needs the
        // sheet's own host to be visible at all.
        composeRule.waitFor {
            composeRule.onAllNodesWithText("Added to your plan").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Added to your plan").assertIsDisplayed()
        // The check mark that replaces the Accept button is announced to screen readers.
        composeRule.onNodeWithContentDescription("Added to your plan").assertExists()
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
                InstrumentedKoinHost {
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
    fun mealPlan_favoriteStar_savesMealAndConfirms() {
        val weekKey = WeekCalendar.currentWeekKey()
        val dayIndex = WeekCalendar.todayIndexInWeek(weekKey) ?: 0
        val favorites = InstrumentedFavoriteDishesRepository()
        val screenModel = nutritionScreenModel(
            weekKey = weekKey,
            plannedLunch = dayIndex to "Pasta al pomodoro",
            favorites = favorites,
        )
        val star = showFavoriteStar(screenModel, weekKey, dayIndex)

        composeRule.onNodeWithTag(star).assertIsOff().performClick()

        composeRule.waitForState(favorites.favorites) { list ->
            list.any { it.normalisedLabel == "pasta al pomodoro" }
        }
        composeRule.onNodeWithTag(star).assertIsOn()
        composeRule.onNodeWithText("Saved to favorites").assertIsDisplayed()
    }

    @Test
    fun mealPlan_favoriteStar_savesTheEditedDraftNotTheDebouncedSavedText() {
        val weekKey = WeekCalendar.currentWeekKey()
        val dayIndex = WeekCalendar.todayIndexInWeek(weekKey) ?: 0
        val original = FavoriteDish(label = "Pasta al pomodoro", normalisedLabel = "pasta al pomodoro")
        val favorites = InstrumentedFavoriteDishesRepository(initial = listOf(original))
        val repository = InstrumentedNutritionRepository(weekKey)
        val screenModel = nutritionScreenModel(
            weekKey = weekKey,
            plannedLunch = dayIndex to "Pasta al pomodoro",
            favorites = favorites,
            repository = repository,
        )
        val star = showFavoriteStar(screenModel, weekKey, dayIndex)
        val lunchField = MealPlanTestTags.lunchField(dayIndex)

        // The saved meal is a favorite, so the star starts checked.
        composeRule.onNodeWithTag(star).assertIsOn()

        // Hold the debounced save: the saved meal stays "Pasta al pomodoro" while the box shows
        // the draft "Pasta al pesto". Unheld, the save lands on its own clock and the two converge,
        // so a star that follows the saved meal would pass unnoticed.
        val saveGate = CompletableDeferred<Unit>()
        repository.saveGate = saveGate
        composeRule.onNodeWithTag(lunchField).performTextReplacement("Pasta al pesto")

        fun assertSavedAndDraftDiffer() {
            assertEquals("saved meal", "Pasta al pomodoro", screenModel.mealPlan.value.days[dayIndex].lunch)
            // Text only: a focused field's text can carry IME styling (a composing underline),
            // which an AnnotatedString equality would also compare.
            composeRule.onNodeWithTag(lunchField).assert(
                SemanticsMatcher("draft text is 'Pasta al pesto'") {
                    it.config.getOrNull(SemanticsProperties.EditableText)?.text == "Pasta al pesto"
                },
            )
        }

        // The draft is not a favorite, so the star must be off even though the saved meal is one.
        assertSavedAndDraftDiffer()
        composeRule.onNodeWithTag(star).assertIsOff()
        assertSavedAndDraftDiffer()

        composeRule.onNodeWithTag(star).performClick()
        composeRule.waitForState(favorites.favorites) { it != listOf(original) }
        val afterClick = favorites.favorites.value
        assertTrue(
            "the click must save the draft 'Pasta al pesto'; favorites were $afterClick",
            afterClick.any { it.label == "Pasta al pesto" && it.normalisedLabel == "pasta al pesto" },
        )
        assertTrue("the original favorite must be unchanged; favorites were $afterClick", original in afterClick)
        assertSavedAndDraftDiffer()
        composeRule.onNodeWithTag(star).assertIsOn()

        // Released, the save stores the draft as the meal.
        saveGate.complete(Unit)
        composeRule.waitForState(screenModel.mealPlan) { it.days[dayIndex].lunch == "Pasta al pesto" }
    }

    @Test
    fun mealPlan_favoriteStar_whenTenSaved_replacesOneThroughDialog() {
        val weekKey = WeekCalendar.currentWeekKey()
        val dayIndex = WeekCalendar.todayIndexInWeek(weekKey) ?: 0
        val favorites = InstrumentedFavoriteDishesRepository(
            initial = (1..10).map { FavoriteDish(label = "Dish $it", normalisedLabel = "dish $it") },
        )
        val screenModel = nutritionScreenModel(
            weekKey = weekKey,
            plannedLunch = dayIndex to "Pasta al pomodoro",
            favorites = favorites,
        )
        val star = showFavoriteStar(screenModel, weekKey, dayIndex)

        composeRule.onNodeWithTag(star).performClick()

        composeRule.waitFor {
            composeRule.onAllNodesWithText("Favorites are full").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Favorites are full").assertIsDisplayed()
        composeRule.onNodeWithText("Dish 1").performClick()
        composeRule.onNodeWithText("Replace").performClick()

        composeRule.waitForState(favorites.favorites) { list ->
            list.any { it.normalisedLabel == "pasta al pomodoro" } &&
                list.none { it.normalisedLabel == "dish 1" }
        }
        composeRule.onNodeWithTag(star).assertIsOn()
    }

    /** Renders the week plan, opens the day if needed, and scrolls the lunch star into view. */
    private fun showFavoriteStar(screenModel: NutritionScreenModel, weekKey: String, dayIndex: Int): String {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    WeeklyMealPlanScreen(
                        onBack = {},
                        onOpenSection = { _, _ -> },
                        onOpenAiSheet = {},
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
        val star = MealPlanTestTags.favoriteButton(dayIndex, MealSlot.Lunch)
        composeRule.onNodeWithTag(MealPlanTestTags.SCROLL_LIST).performScrollToNode(hasTestTag(star))
        return star
    }

    @Test
    fun grocery_toBuySection_showsStickyInputBarOnPhone() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        composeRule.onNodeWithTag(GroceryListTestTags.TO_BUY_SECTION).assertIsDisplayed()
        composeRule.onNodeWithTag(GroceryInputBarTestTags.STICKY_BAR).assertIsDisplayed()
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).assertIsDisplayed()
    }

    @Test
    fun grocery_emptyStateCta_focusesStickyInputBar() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        composeRule.onNodeWithTag(GroceryListTestTags.EMPTY_STATE).performScrollTo()
        composeRule.onNodeWithTag(JourneyEmptyStateTestTags.PRIMARY_ACTION).performClick()
        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD).assertIsFocused()
    }

    @Test
    fun grocery_checkButton_checksItem() {
        val itemId = "instrumented-item-1"
        val screenModel = nutritionScreenModel(itemId = itemId)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        composeRule.onNodeWithTag("${GroceryItemRowTestTags.CHECKBOX_PREFIX}$itemId")
            .performScrollTo()
            .performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.single().isChecked }
    }

    @Test
    fun grocery_deleteButton_showsConfirmationDialog() {
        val itemId = "instrumented-item-1"
        val screenModel = nutritionScreenModel(itemId = itemId)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        composeRule.onNodeWithTag("${GroceryItemRowTestTags.DELETE_BUTTON_PREFIX}$itemId")
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithTag(GroceryItemRowTestTags.DELETE_CONFIRM_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun grocery_deleteConfirm_removesItem() {
        val itemId = "instrumented-item-1"
        val screenModel = nutritionScreenModel(itemId = itemId)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Milk")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }

        composeRule.onNodeWithTag("${GroceryItemRowTestTags.DELETE_BUTTON_PREFIX}$itemId")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag(GroceryItemRowTestTags.DELETE_CONFIRM_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeRule.waitForState(screenModel.groceryItems) { it.isEmpty() }
    }

    @Test
    fun groceryScreen_enablesKeepScreenOnFlag() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = nutritionScreenModel())
                }
            }
        }

        val flags = composeRule.activity.window.attributes.flags
        assertTrue(flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0)
    }

    @Test
    fun groceryScreen_keepScreenOnToggle_disablesFlag() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = nutritionScreenModel())
                }
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
