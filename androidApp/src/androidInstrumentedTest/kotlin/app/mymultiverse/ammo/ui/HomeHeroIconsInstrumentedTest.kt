package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.presentation.components.HomePrimaryActionsTestTags
import app.mymultiverse.ammo.presentation.navigation.AppMainTab
import app.mymultiverse.ammo.presentation.navigation.MainTabShell
import app.mymultiverse.ammo.presentation.navigation.NavigationTestTags
import app.mymultiverse.ammo.presentation.screens.home.HomeDailyHubCircularActions
import app.mymultiverse.ammo.presentation.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeHeroIconsInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun homeDailyHubCircularActions_lightTheme_displayHeroCtasWithIcons() {
        composeRule.setContent {
            AppTheme(darkTheme = false) {
                HomeDailyHubCircularActions(
                    onOpenMealPlan = {},
                    onOpenGrocery = {},
                )
            }
        }

        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.PLAN).assertIsDisplayed()
        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.GROCERY).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("PLAN LUNCH").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("SHOPPING LIST").assertIsDisplayed()
    }

    @Test
    fun mainTabShell_lightTheme_navTabs_displayHeroIcons() {
        composeRule.setContent {
            AppTheme(darkTheme = false) {
                MainTabShell(
                    selectedTab = AppMainTab.Home,
                    onTabSelected = {},
                    showBottomBar = true,
                ) { modifier ->
                    Text("content", modifier = modifier)
                }
            }
        }

        composeRule.onNodeWithTag(NavigationTestTags.TAB_MEAL_PLAN).assertIsDisplayed()
        composeRule.onNodeWithTag(NavigationTestTags.TAB_GROCERY).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Plan").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Groceries").assertIsDisplayed()
    }

    @Test
    fun homeDailyHubCircularActions_displayHeroCtasWithIcons() {
        composeRule.setContent {
            AppTheme {
                HomeDailyHubCircularActions(
                    onOpenMealPlan = {},
                    onOpenGrocery = {},
                )
            }
        }

        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.PLAN).assertIsDisplayed()
        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.GROCERY).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("PLAN LUNCH").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("SHOPPING LIST").assertIsDisplayed()
    }

    @Test
    fun homeDailyHubCircularActions_planCta_invokesCallback() {
        var planClicked = false

        composeRule.setContent {
            AppTheme {
                HomeDailyHubCircularActions(
                    onOpenMealPlan = { planClicked = true },
                    onOpenGrocery = {},
                )
            }
        }

        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.PLAN).performClick()
        assertTrue(planClicked)
    }

    @Test
    fun homeDailyHubCircularActions_groceryCta_invokesCallback() {
        var groceryClicked = false

        composeRule.setContent {
            AppTheme {
                HomeDailyHubCircularActions(
                    onOpenMealPlan = {},
                    onOpenGrocery = { groceryClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag(HomePrimaryActionsTestTags.GROCERY).performClick()
        assertTrue(groceryClicked)
    }

    @Test
    fun mainTabShell_darkTheme_navTabs_displayHeroIcons() {
        composeRule.setContent {
            AppTheme(darkTheme = true) {
                MainTabShell(
                    selectedTab = AppMainTab.Home,
                    onTabSelected = {},
                    showBottomBar = true,
                ) { modifier ->
                    Text("content", modifier = modifier)
                }
            }
        }

        composeRule.onNodeWithTag(NavigationTestTags.TAB_MEAL_PLAN).assertIsDisplayed()
        composeRule.onNodeWithTag(NavigationTestTags.TAB_GROCERY).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Plan").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Groceries").assertIsDisplayed()
    }

    @Test
    fun mainTabShell_navTabs_displayHeroIcons() {
        composeRule.setContent {
            AppTheme {
                MainTabShell(
                    selectedTab = AppMainTab.Home,
                    onTabSelected = {},
                    showBottomBar = true,
                ) { modifier ->
                    Text("content", modifier = modifier)
                }
            }
        }

        composeRule.onNodeWithTag(NavigationTestTags.TAB_MEAL_PLAN).assertIsDisplayed()
        composeRule.onNodeWithTag(NavigationTestTags.TAB_GROCERY).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Plan").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Groceries").assertIsDisplayed()
    }

    @Test
    fun mainTabShell_mealPlanTab_invokesSelection() {
        var selectedTab: AppMainTab? = null

        composeRule.setContent {
            AppTheme {
                MainTabShell(
                    selectedTab = AppMainTab.Home,
                    onTabSelected = { selectedTab = it },
                    showBottomBar = true,
                ) { }
            }
        }

        composeRule.onNodeWithTag(NavigationTestTags.TAB_MEAL_PLAN).performClick()
        assertTrue(selectedTab == AppMainTab.MealPlan)
    }
}
