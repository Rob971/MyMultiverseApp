package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.NutritionHubSummary

internal object HomeNutritionStatusLine {

    fun format(
        summary: HomeNutritionSummary?,
        groceryProgressLabel: (checked: Int, total: Int) -> String,
        mealPlanProgressLabel: (plannedDays: Int, daysInWeek: Int) -> String,
        emptyLabel: String,
    ): String? {
        if (summary == null) return null

        val groceryProgress = summary.groceryProgress
        val hasGrocery = groceryProgress != null
        val hasMealPlan = summary.plannedDays > 0

        if (!hasGrocery && !hasMealPlan) {
            return emptyLabel
        }

        return buildList {
            groceryProgress?.let { progress ->
                add(groceryProgressLabel(progress.checked, progress.total))
            }
            add(
                mealPlanProgressLabel(
                    summary.plannedDays,
                    WeeklyMealPlan.DAYS_IN_WEEK,
                ),
            )
        }.joinToString(" · ")
    }
}
