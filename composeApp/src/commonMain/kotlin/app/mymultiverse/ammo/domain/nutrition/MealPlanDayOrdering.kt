package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.DayMeals

data class MealPlanDayEntry(
    val index: Int,
    val day: DayMeals,
)

object MealPlanDayOrdering {

    fun orderDaysForDisplay(
        days: List<DayMeals>,
        todayIndex: Int?,
    ): List<MealPlanDayEntry> {
        val entries = days.mapIndexed { index, day -> MealPlanDayEntry(index, day) }
        if (todayIndex == null) return entries
        val today = entries.getOrNull(todayIndex) ?: return entries
        return listOf(today) + entries.filterNot { it.index == todayIndex }
    }
}
