package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.data.nutrition.GroceryGhostPairingDismissStore
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.model.Greeting
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.presentation.components.GroceryInputBarTestTags
import app.mymultiverse.ammo.presentation.components.MealPlanTestTags
import app.mymultiverse.ammo.presentation.components.ScreenLayout
import app.mymultiverse.ammo.presentation.screens.auth.LoginScreen
import app.mymultiverse.ammo.presentation.screens.auth.LoginScreenModel
import app.mymultiverse.ammo.presentation.screens.auth.LoginTestTags
import app.mymultiverse.ammo.presentation.screens.home.HomeTestTags
import app.mymultiverse.ammo.presentation.screens.home.HomeWelcomeContent
import app.mymultiverse.ammo.presentation.screens.nutrition.GroceryListTestTags
import app.mymultiverse.ammo.presentation.screens.nutrition.GroceryShoppingScreen
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.ammo.presentation.screens.nutrition.WeeklyMealPlanScreen
import app.mymultiverse.ammo.presentation.screens.onboarding.AuthScreen
import app.mymultiverse.ammo.presentation.screens.onboarding.AuthTestTags
import app.mymultiverse.ammo.presentation.screens.onboarding.OnboardingScreenModel
import app.mymultiverse.ammo.presentation.theme.AppTheme
import app.mymultiverse.ammo.ui.InstrumentedComposeTest.waitForState
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WideLayoutInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun nutritionScreenModel(
        weekKey: String = WeekCalendar.currentWeekKey(),
    ): NutritionScreenModel {
        val repository = InstrumentedNutritionRepository(weekKey)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        var nextItemId = 0
        return NutritionScreenModel(
            session = InstrumentedNutritionSessionCoordinator(repository),
            householdRepository = InstrumentedHouseholdRepository(),
            collaborationRepository = InstrumentedHouseholdCollaborationRepository(),
            aiAssistant = InstrumentedNutritionAdviceService(),
            ghostPairingDismissStore = GroceryGhostPairingDismissStore(MapSettings()),
            logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "instrumented")),
            scope = scope,
            newItemId = { "wide-layout-item-${nextItemId++}" },
        )
    }

    @Test
    fun grocery_wideLayout_usesSidePanelNotBottomBar() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    WideLayoutHost {
                        GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                    }
                }
            }
        }

        composeRule.onNodeWithTag(GroceryListTestTags.WIDE_LAYOUT).assertIsDisplayed()
        composeRule.onNodeWithTag(GroceryInputBarTestTags.STICKY_BAR).assertIsDisplayed()
        composeRule.onNodeWithTag(GroceryInputBarTestTags.INPUT_FIELD)
            .performTextInput("Bread")
        composeRule.onNodeWithTag(GroceryInputBarTestTags.ADD_BUTTON).performClick()
        composeRule.waitForState(screenModel.groceryItems) { it.size == 1 }
    }

    @Test
    fun grocery_phoneLayout_keepsBottomStickyInput() {
        val screenModel = nutritionScreenModel()

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    PhoneLayoutHost {
                        GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                    }
                }
            }
        }

        composeRule.onNodeWithTag(GroceryListTestTags.WIDE_LAYOUT).assertDoesNotExist()
        composeRule.onNodeWithTag(GroceryInputBarTestTags.STICKY_BAR).assertIsDisplayed()
    }

    @Test
    fun mealPlan_wideLayout_showsTodayAndWeekColumns() {
        val weekKey = WeekCalendar.currentWeekKey()
        val todayIndex = WeekCalendar.todayIndexInWeek(weekKey)
        assertTrue("Current week must include today for wide meal-plan layout", todayIndex != null)

        val screenModel = nutritionScreenModel(weekKey = weekKey)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    WideLayoutHost {
                        WeeklyMealPlanScreen(
                            onBack = {},
                            onOpenSection = { _, _ -> },
                            screenModel = screenModel,
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag(MealPlanTestTags.WIDE_DAYS_ROW).assertIsDisplayed()
    }

    @Test
    fun home_wideLayout_showsTwoColumnWelcome() {
        composeRule.setContent {
            AppTheme {
                WideLayoutHost {
                    HomeWelcomeContent(
                        greeting = Greeting("Good morning"),
                        userDisplayName = "Alex",
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
                        onOpenHouseholdMembers = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.WIDE_LAYOUT).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTestTags.DAILY_MEAL_PLAN_BLOCK).assertIsDisplayed()
    }

    @Test
    fun home_phoneLayout_hidesWideLayoutTag() {
        composeRule.setContent {
            AppTheme {
                PhoneLayoutHost {
                    HomeWelcomeContent(
                        greeting = Greeting("Good morning"),
                        userDisplayName = "Alex",
                        nutritionSummary = null,
                        isRefreshing = false,
                        onRefresh = {},
                        onOpenMealPlan = {},
                        onOpenGrocery = {},
                        onOpenHouseholdMembers = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.WIDE_LAYOUT).assertDoesNotExist()
    }

    /**
     * [WideLayoutHost] minus [AuthScreen]/[LoginScreen]'s horizontal screen padding on both
     * sides — the width [AdaptiveFormWidth] receives as its incoming constraint, comfortably
     * over the 600dp breakpoint so the form is expected to clamp to [ScreenLayout.formMaxWidth].
     */
    private val wideAvailableFormWidth =
        (ScreenLayout.expandedMinWidth + 200.dp) - (ScreenLayout.horizontalPadding * 2)

    /** Same computation for [PhoneLayoutHost] — under the breakpoint, so no clamping applies. */
    private val phoneAvailableFormWidth =
        (ScreenLayout.expandedMinWidth - 48.dp) - (ScreenLayout.horizontalPadding * 2)

    @Test
    fun auth_wideLayout_capsFormWidthAtFormMaxWidth() {
        composeRule.setContent {
            AppTheme {
                WideLayoutHost {
                    AuthScreen(
                        pendingInviteToken = null,
                        showConfigMissing = false,
                        onContinueWithEmail = {},
                        screenModel = OnboardingScreenModel(
                            authRepository = InstrumentedFakeAuthRepository(AuthState.Unauthenticated),
                            collaborationRepository = InstrumentedHouseholdCollaborationRepository(),
                        ),
                    )
                }
            }
        }

        assertTrue(
            "Test host must offer more width than the form cap to prove clamping",
            wideAvailableFormWidth > ScreenLayout.formMaxWidth,
        )
        composeRule.onNodeWithTag(AuthTestTags.SCREEN)
            .assertWidthIsEqualTo(ScreenLayout.formMaxWidth)
    }

    @Test
    fun auth_phoneLayout_fillsAvailableWidth() {
        composeRule.setContent {
            AppTheme {
                PhoneLayoutHost {
                    AuthScreen(
                        pendingInviteToken = null,
                        showConfigMissing = false,
                        onContinueWithEmail = {},
                        screenModel = OnboardingScreenModel(
                            authRepository = InstrumentedFakeAuthRepository(AuthState.Unauthenticated),
                            collaborationRepository = InstrumentedHouseholdCollaborationRepository(),
                        ),
                    )
                }
            }
        }

        composeRule.onNodeWithTag(AuthTestTags.SCREEN)
            .assertWidthIsEqualTo(phoneAvailableFormWidth)
    }

    @Test
    fun login_wideLayout_capsFormWidthAtFormMaxWidth() {
        composeRule.setContent {
            AppTheme {
                WideLayoutHost {
                    LoginScreen(
                        showConfigMissing = false,
                        screenModel = LoginScreenModel(
                            authRepository = InstrumentedFakeAuthRepository(AuthState.Unauthenticated),
                            logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "instrumented")),
                        ),
                    )
                }
            }
        }

        composeRule.onNodeWithTag(LoginTestTags.SCREEN)
            .assertWidthIsEqualTo(ScreenLayout.formMaxWidth)
    }

    @Test
    fun login_phoneLayout_fillsAvailableWidth() {
        composeRule.setContent {
            AppTheme {
                PhoneLayoutHost {
                    LoginScreen(
                        showConfigMissing = false,
                        screenModel = LoginScreenModel(
                            authRepository = InstrumentedFakeAuthRepository(AuthState.Unauthenticated),
                            logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "instrumented")),
                        ),
                    )
                }
            }
        }

        composeRule.onNodeWithTag(LoginTestTags.SCREEN)
            .assertWidthIsEqualTo(phoneAvailableFormWidth)
    }
}

@Composable
private fun WideLayoutHost(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .width(ScreenLayout.expandedMinWidth + 200.dp)
            .fillMaxHeight(),
    ) {
        content()
    }
}

@Composable
private fun PhoneLayoutHost(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .width(ScreenLayout.expandedMinWidth - 48.dp)
            .fillMaxHeight(),
    ) {
        content()
    }
}
