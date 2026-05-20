package app.mymultiverse.kmp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import app.mymultiverse.kmp.presentation.components.NapolitanBackground
import app.mymultiverse.kmp.presentation.navigation.AppRoute
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.navigation.rememberAppNavigator
import app.mymultiverse.kmp.presentation.screens.home.HomeScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionFlow
import app.mymultiverse.kmp.presentation.theme.AppTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    AppTheme {
        val navigator = rememberAppNavigator()
        val route = navigator.current

        BackHandler(enabled = navigator.canGoBack) {
            navigator.navigateBack()
        }

        NapolitanBackground {
            when (route) {
                AppRoute.Home -> HomeScreen(
                    onOpenNutrition = {
                        navigator.navigateTo(AppRoute.Nutrition(section = NutritionSection.Hub))
                    },
                )

                is AppRoute.Nutrition -> NutritionFlow(
                    section = route.section,
                    onBack = navigator::navigateBack,
                    onOpenSection = { section ->
                        navigator.navigateTo(AppRoute.Nutrition(section = section))
                    },
                )
            }
        }
    }
}
