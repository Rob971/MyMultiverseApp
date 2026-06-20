package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan

object MealPlanPresentation {

    data class PlannedMeal(
        val dayIndex: Int,
        val slot: MealSlot,
        val text: String,
    )

    fun isPlanned(day: DayMeals): Boolean =
        day.lunch.isNotBlank() || day.dinner.isNotBlank()

    fun summaryText(
        day: DayMeals,
        notPlannedLabel: String,
        unplannedSlotLabel: String,
        lunchLabel: String,
        dinnerLabel: String,
    ): String {
        if (!isPlanned(day)) return notPlannedLabel

        val lunchPart = day.lunch.trim().ifBlank { "$lunchLabel: $unplannedSlotLabel" }
        val dinnerPart = day.dinner.trim().ifBlank { "$dinnerLabel: $unplannedSlotLabel" }
        return "$lunchPart · $dinnerPart"
    }

    fun plannedMeals(days: List<DayMeals>): List<PlannedMeal> = buildList {
        days.forEachIndexed { dayIndex, day ->
            if (day.lunch.isNotBlank()) {
                add(PlannedMeal(dayIndex, MealSlot.Lunch, day.lunch.trim()))
            }
            if (day.dinner.isNotBlank()) {
                add(PlannedMeal(dayIndex, MealSlot.Dinner, day.dinner.trim()))
            }
        }
    }

    fun tomorrowIndex(dayIndex: Int): Int? =
        (dayIndex + 1).takeIf { it in 0 until WeeklyMealPlan.DAYS_IN_WEEK }

    fun mealLabelSuggestions(
        days: List<DayMeals>,
        query: String,
        minQueryLength: Int = 2,
        maxResults: Int = 5,
    ): List<String> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.length < minQueryLength) return emptyList()
        val queryLower = normalizedQuery.lowercase()
        return days.asSequence()
            .flatMap { day -> sequenceOf(day.lunch, day.dinner) }
            .map { it.trim() }
            .filter { label ->
                label.isNotBlank() &&
                    !label.equals(normalizedQuery, ignoreCase = true) &&
                    label.lowercase().contains(queryLower)
            }
            .distinctBy { it.lowercase() }
            .take(maxResults)
            .toList()
    }
}
