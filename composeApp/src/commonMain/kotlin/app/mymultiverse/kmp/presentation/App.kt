package app.mymultiverse.kmp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.mymultiverse.kmp.presentation.components.NapolitanBackground
import app.mymultiverse.kmp.presentation.navigation.AppRoute
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.screens.home.HomeScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionFlow
import app.mymultiverse.kmp.presentation.theme.AppTheme
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        AppTheme {
            var route by remember { mutableStateOf<AppRoute>(AppRoute.Home) }

            NapolitanBackground {
                when (val currentRoute = route) {
                    AppRoute.Home -> HomeScreen(
                        onOpenNutrition = {
                            route = AppRoute.Nutrition(section = NutritionSection.Hub)
                        },
                    )

                    is AppRoute.Nutrition -> NutritionFlow(
                        section = currentRoute.section,
                        onBackToHome = { route = AppRoute.Home },
                        onBackToHub = {
                            route = AppRoute.Nutrition(section = NutritionSection.Hub)
                        },
                        onOpenSection = { section ->
                            route = AppRoute.Nutrition(section = section)
                        },
                    )
                }
            }
        }
    }
}
