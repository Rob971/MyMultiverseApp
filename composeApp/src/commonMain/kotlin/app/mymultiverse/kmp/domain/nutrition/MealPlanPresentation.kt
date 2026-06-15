package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals

object MealPlanPresentation {

    fun isPlanned(day: DayMeals): Boolean =
        day.lunch.isNotBlank() || day.dinner.isNotBlank()

    fun summaryText(day: DayMeals, notPlannedLabel: String): String {
        val parts = buildList {
            if (day.lunch.isNotBlank()) add(day.lunch.trim())
            if (day.dinner.isNotBlank()) add(day.dinner.trim())
        }
        return parts.joinToString(" · ").ifBlank { notPlannedLabel }
    }
}
