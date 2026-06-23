package app.mymultiverse.kmp.presentation.screens.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.NutritionContextualChips

object NutritionContextualChipsResolver {

    fun ingredientMatches(
        mealPlan: WeeklyMealPlan,
        groceryItems: List<GroceryItem>,
        maxChips: Int = 3,
    ): List<NutritionContextualChips.IngredientMatch> {
        val mealTexts = mealPlan.days.flatMap { day ->
            listOf(day.lunch, day.dinner)
        }
        val uncheckedGrocery = groceryItems
            .filterNot { it.isChecked }
            .map { it.label }
        return NutritionContextualChips.ingredientsFromHistory(
            mealTexts = mealTexts,
            groceryLabels = uncheckedGrocery,
            maxChips = maxChips,
        )
    }
}
