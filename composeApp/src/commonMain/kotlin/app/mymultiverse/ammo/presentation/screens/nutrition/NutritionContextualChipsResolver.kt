package app.mymultiverse.ammo.presentation.screens.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.NutritionContextualChips

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
