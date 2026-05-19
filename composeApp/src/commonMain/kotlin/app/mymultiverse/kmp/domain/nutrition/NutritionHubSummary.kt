package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem

object NutritionHubSummary {

    data class GroceryProgress(
        val checked: Int,
        val total: Int,
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
}
