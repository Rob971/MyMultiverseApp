package app.mymultiverse.kmp.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
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
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nav_tab_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nav_tab_home
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nav_tab_meal_plan
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
                                Icon(
                                    imageVector = AppIcons.Home,
                                    contentDescription = homeTabLabel,
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
                                Icon(
                                    imageVector = AppIcons.DateRange,
                                    contentDescription = mealPlanTabLabel,
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
                                Icon(
                                    imageVector = AppIcons.ShoppingCart,
                                    contentDescription = groceryTabLabel,
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
