package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan

object NutritionHubSummary {

    const val MEAL_SLOTS_PER_WEEK = WeeklyMealPlan.DAYS_IN_WEEK * 2

    data class GroceryProgress(
        val checked: Int,
        val total: Int,
    )

    data class MealPlanProgress(
        val plannedSlots: Int,
        val totalSlots: Int = MEAL_SLOTS_PER_WEEK,
    )

    fun groceryProgress(items: List<GroceryItem>): GroceryProgress? {
        if (items.isEmpty()) return null
        return GroceryProgress(
            checked = items.count { it.isChecked },
            total = items.size,
        )
    }

    fun plannedDaysCount(days: List<DayMeals>): Int =
        days.count { day -> day.lunch.isNotBlank() || day.dinner.isNotBlank() }

    fun plannedSlotsCount(days: List<DayMeals>): Int =
        days.sumOf { day ->
            (if (day.lunch.isNotBlank()) 1 else 0) + (if (day.dinner.isNotBlank()) 1 else 0)
        }

    fun mealPlanProgress(days: List<DayMeals>): MealPlanProgress =
        MealPlanProgress(plannedSlots = plannedSlotsCount(days))
}
