package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.runtime.Composable
import app.mymultiverse.kmp.presentation.navigation.NutritionSection

@Composable
fun NutritionFlow(
    section: NutritionSection,
    onBackToHome: () -> Unit,
    onBackToHub: () -> Unit,
    onOpenSection: (NutritionSection) -> Unit,
) {
    when (section) {
        NutritionSection.Hub -> NutritionHubScreen(
            onBack = onBackToHome,
            onOpenSection = onOpenSection,
        )
        NutritionSection.Grocery -> GroceryShoppingScreen(onBack = onBackToHub)
        NutritionSection.MealPlan -> WeeklyMealPlanScreen(onBack = onBackToHub)
        NutritionSection.AiAdvice -> NutritionAiAdviceScreen(onBack = onBackToHub)
    }
}
