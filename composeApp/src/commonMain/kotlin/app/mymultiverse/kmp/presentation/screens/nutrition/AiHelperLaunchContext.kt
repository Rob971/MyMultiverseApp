package app.mymultiverse.kmp.presentation.screens.nutrition

import app.mymultiverse.kmp.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode

data class AiHelperLaunchContext(
    val mode: NutritionAiMode,
    val mealPlanScope: MealPlanGenerationScope = MealPlanGenerationScope.FullWeek,
    val initialCriteria: String = "",
    val targetMealSlot: MealSlot? = null,
    val offerIngredientsAfterApply: Boolean = false,
)
