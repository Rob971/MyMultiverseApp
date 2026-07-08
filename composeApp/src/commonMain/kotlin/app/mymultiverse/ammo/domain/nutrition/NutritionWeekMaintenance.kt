package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan

/**
 * Week-boundary and daily upkeep for shared nutrition data.
 *
 * - Grocery: unchecked items carry into the next calendar week.
 * - Meal plan: meals on days before today in the current week are cleared.
 */
object NutritionWeekMaintenance {

    fun uncheckedGroceryToCarry(items: List<GroceryItem>): List<GroceryItem> =
        items.filter { item -> !item.isChecked && !item.isPantryCheck }

    /**
     * Merges [carried] into [current], skipping duplicate labels.
     * Returns null when nothing would change.
     */
    fun mergeCarriedGrocery(
        carried: List<GroceryItem>,
        current: List<GroceryItem>,
        newItemId: () -> String,
    ): List<GroceryItem>? {
        if (carried.isEmpty()) return null
        var merged = current
        var changed = false
        carried.forEach { item ->
            val label = item.label.trim()
            if (label.isEmpty()) return@forEach
            if (GroceryListPresentation.isDuplicateLabel(merged, label)) return@forEach
            merged = listOf(GroceryItem(id = newItemId(), label = label)) + merged
            changed = true
        }
        return merged.takeIf { changed }
    }

    /**
     * Clears lunch/dinner for every day before [todayIndex] in the current week.
     * Returns null when the plan is already pruned.
     */
    fun prunePastMealDays(
        plan: WeeklyMealPlan,
        todayIndex: Int?,
    ): WeeklyMealPlan? {
        if (todayIndex == null || todayIndex <= 0) return null
        val days = plan.days.toMutableList()
        var changed = false
        for (index in 0 until todayIndex) {
            if (MealPlanPresentation.isPlanned(days[index])) {
                days[index] = DayMeals()
                changed = true
            }
        }
        return plan.copy(days = days).takeIf { changed }
    }

    fun shouldCarryGrocery(
        currentWeekKey: String,
        lastMaintainedWeekKey: String?,
    ): Boolean = lastMaintainedWeekKey != currentWeekKey
}
