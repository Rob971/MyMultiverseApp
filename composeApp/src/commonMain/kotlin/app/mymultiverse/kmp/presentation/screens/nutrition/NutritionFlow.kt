package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.runtime.Composable
import app.mymultiverse.kmp.presentation.navigation.NutritionSection

@Composable
fun NutritionFlow(
    section: NutritionSection,
    onBack: () -> Unit,
    onOpenSection: (NutritionSection) -> Unit,
) {
    when (section) {
        NutritionSection.Hub -> NutritionHubScreen(
            onBack = onBack,
            onOpenSection = onOpenSection,
        )
        NutritionSection.Grocery -> GroceryShoppingScreen(onBack = onBack)
        NutritionSection.MealPlan -> WeeklyMealPlanScreen(onBack = onBack)
        NutritionSection.AiAdvice -> NutritionAiAdviceScreen(onBack = onBack)
    }
}
