package app.mymultiverse.ammo.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.components.JourneyIcon
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nav_tab_grocery
import ammo.composeapp.generated.resources.nav_tab_home
import ammo.composeapp.generated.resources.nav_tab_meal_plan
import org.jetbrains.compose.resources.stringResource

object MainTabDefaults {
    val barHeight = 80.dp
}

val LocalMainTabBarVisible = staticCompositionLocalOf { false }

@Composable
fun MainTabShell(
    selectedTab: AppMainTab,
    onTabSelected: (AppMainTab) -> Unit,
    showBottomBar: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    CompositionLocalProvider(LocalMainTabBarVisible provides showBottomBar) {
        Scaffold(
            modifier = modifier,
            // Transparent shell — theme background from NapolitanBackground must bleed through.
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = JourneySemanticColors.elevatedSurface(),
                        contentColor = JourneySemanticColors.inkDeep(),
                    ) {
                        val homeTabLabel = stringResource(Res.string.nav_tab_home)
                        val mealPlanTabLabel = stringResource(Res.string.nav_tab_meal_plan)
                        val groceryTabLabel = stringResource(Res.string.nav_tab_grocery)
                        NavigationBarItem(
                            selected = selectedTab == AppMainTab.Home,
                            onClick = { onTabSelected(AppMainTab.Home) },
                            icon = {
                                JourneyIcon(
                                    role = AppIconRole.NavHome,
                                    contentDescription = homeTabLabel,
                                    tint = if (selectedTab == AppMainTab.Home) {
                                        JourneySemanticColors.brandTeal()
                                    } else {
                                        JourneySemanticColors.inkMuted()
                                    },
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { Text(homeTabLabel) },
                            modifier = Modifier.testTag(NavigationTestTags.TAB_HOME),
                            colors = navigationBarItemColors(),
                        )
                        NavigationBarItem(
                            selected = selectedTab == AppMainTab.MealPlan,
                            onClick = { onTabSelected(AppMainTab.MealPlan) },
                            icon = {
                                JourneyIcon(
                                    role = AppIconRole.NavMealPlan,
                                    contentDescription = mealPlanTabLabel,
                                    tint = if (selectedTab == AppMainTab.MealPlan) {
                                        JourneySemanticColors.brandTeal()
                                    } else {
                                        JourneySemanticColors.inkMuted()
                                    },
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { Text(mealPlanTabLabel) },
                            modifier = Modifier.testTag(NavigationTestTags.TAB_MEAL_PLAN),
                            colors = navigationBarItemColors(),
                        )
                        NavigationBarItem(
                            selected = selectedTab == AppMainTab.Grocery,
                            onClick = { onTabSelected(AppMainTab.Grocery) },
                            icon = {
                                JourneyIcon(
                                    role = AppIconRole.NavGrocery,
                                    contentDescription = groceryTabLabel,
                                    tint = if (selectedTab == AppMainTab.Grocery) {
                                        JourneySemanticColors.brandTeal()
                                    } else {
                                        JourneySemanticColors.inkMuted()
                                    },
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { Text(groceryTabLabel) },
                            modifier = Modifier.testTag(NavigationTestTags.TAB_GROCERY),
                            colors = navigationBarItemColors(),
                        )
                    }
                }
            },
        ) { padding ->
            content(Modifier.padding(padding))
        }
    }
}

@Composable
private fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = JourneySemanticColors.brandTeal(),
    selectedTextColor = JourneySemanticColors.brandTeal(),
    indicatorColor = JourneySemanticColors.navIndicator(),
    unselectedIconColor = JourneySemanticColors.inkMuted(),
    unselectedTextColor = JourneySemanticColors.inkMuted(),
)
