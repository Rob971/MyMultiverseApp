package app.mymultiverse.kmp.domain.model.nutrition

data class GroceryItem(
    val id: String,
    val label: String,
    val isChecked: Boolean = false,
)

data class DayMeals(
    val lunch: String = "",
    val dinner: String = "",
)

data class WeeklyMealPlan(
    val weekKey: String,
    val days: List<DayMeals> = List(DAYS_IN_WEEK) { DayMeals() },
) {
    companion object {
        const val DAYS_IN_WEEK = 7
    }
}
