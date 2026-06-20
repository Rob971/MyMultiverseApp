package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.nutrition.NutritionHubSummary

internal object HomeNutritionStatusLine {

    fun format(
        summary: HomeNutritionSummary?,
        groceryProgressLabel: (checked: Int, total: Int) -> String,
        mealPlanProgressLabel: (plannedSlots: Int, totalSlots: Int) -> String,
        emptyLabel: String,
    ): String? {
        if (summary == null) return null

        val groceryProgress = summary.groceryProgress
        val hasGrocery = groceryProgress != null
        val hasMealPlan = summary.plannedMealSlots > 0

        if (!hasGrocery && !hasMealPlan) {
            return emptyLabel
        }

        return buildList {
            groceryProgress?.let { progress ->
                add(groceryProgressLabel(progress.checked, progress.total))
            }
            add(
                mealPlanProgressLabel(
                    summary.plannedMealSlots,
                    NutritionHubSummary.MEAL_SLOTS_PER_WEEK,
                ),
            )
        }.joinToString(" · ")
    }
}
