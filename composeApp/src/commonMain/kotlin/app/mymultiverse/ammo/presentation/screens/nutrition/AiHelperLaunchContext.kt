package app.mymultiverse.ammo.presentation.screens.nutrition

import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.MealSlot
import app.mymultiverse.ammo.domain.nutrition.NutritionAiMode

data class AiHelperLaunchContext(
    val mode: NutritionAiMode,
    val mealPlanScope: MealPlanGenerationScope = MealPlanGenerationScope.FullWeek,
    val initialCriteria: String = "",
    val targetMealSlot: MealSlot? = null,
    val offerIngredientsAfterApply: Boolean = false,
)
