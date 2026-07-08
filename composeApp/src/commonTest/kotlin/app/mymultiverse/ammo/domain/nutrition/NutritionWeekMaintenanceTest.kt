package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NutritionWeekMaintenanceTest {

    @Test
    fun uncheckedGroceryToCarry_omitsCheckedAndPantryItems() {
        val items = listOf(
            GroceryItem(id = "1", label = "Milk"),
            GroceryItem(id = "2", label = "Bread", isChecked = true),
            GroceryItem(id = "3", label = "Salt", isPantryCheck = true),
            GroceryItem(id = "4", label = "Eggs"),
        )

        val carried = NutritionWeekMaintenance.uncheckedGroceryToCarry(items)

        assertEquals(listOf("Milk", "Eggs"), carried.map { it.label })
    }

    @Test
    fun mergeCarriedGrocery_prependsNewItemsAndSkipsDuplicates() {
        val carried = listOf(
            GroceryItem(id = "old-1", label = "Milk"),
            GroceryItem(id = "old-2", label = "Eggs"),
        )
        val current = listOf(GroceryItem(id = "cur-1", label = "Milk"))
        var nextId = 0

        val merged = NutritionWeekMaintenance.mergeCarriedGrocery(
            carried = carried,
            current = current,
            newItemId = { "new-${nextId++}" },
        )

        assertEquals(listOf("Eggs", "Milk"), merged?.map { it.label })
        assertEquals("new-0", merged!!.first().id)
        assertEquals("cur-1", merged.last().id)
    }

    @Test
    fun mergeCarriedGrocery_returnsNullWhenNothingToAdd() {
        val carried = listOf(GroceryItem(id = "1", label = "Milk"))
        val current = listOf(GroceryItem(id = "2", label = "Milk"))

        assertNull(
            NutritionWeekMaintenance.mergeCarriedGrocery(
                carried = carried,
                current = current,
                newItemId = { "new" },
            ),
        )
    }

    @Test
    fun prunePastMealDays_clearsDaysBeforeToday() {
        val plan = WeeklyMealPlan(
            weekKey = "2026-06-15",
            days = List(WeeklyMealPlan.DAYS_IN_WEEK) { index ->
                DayMeals(
                    lunch = "Lunch $index",
                    dinner = if (index == 6) "Sunday dinner" else "",
                )
            },
        )

        val pruned = NutritionWeekMaintenance.prunePastMealDays(plan, todayIndex = 3)

        assertEquals("", pruned!!.days[0].lunch)
        assertEquals("", pruned.days[1].lunch)
        assertEquals("", pruned.days[2].lunch)
        assertEquals("Lunch 3", pruned.days[3].lunch)
        assertEquals("Sunday dinner", pruned.days[6].dinner)
    }

    @Test
    fun prunePastMealDays_returnsNullOnMondayOrOutsideWeek() {
        val plan = WeeklyMealPlan(
            weekKey = "2026-06-15",
            days = listOf(DayMeals(lunch = "Monday")) + List(6) { DayMeals() },
        )

        assertNull(NutritionWeekMaintenance.prunePastMealDays(plan, todayIndex = 0))
        assertNull(NutritionWeekMaintenance.prunePastMealDays(plan, todayIndex = null))
    }

    @Test
    fun mergeCarriedGrocery_allCarriedItemsReceiveUniqueIds() {
        val carried = listOf(
            GroceryItem(id = "old-1", label = "Milk"),
            GroceryItem(id = "old-2", label = "Eggs"),
            GroceryItem(id = "old-3", label = "Butter"),
        )
        var counter = 0

        val merged = NutritionWeekMaintenance.mergeCarriedGrocery(
            carried = carried,
            current = emptyList(),
            newItemId = { "${counter++}" },
        )!!

        val ids = merged.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "All generated IDs must be unique: $ids")
    }

    @Test
    fun shouldCarryGrocery_onlyWhenWeekChanges() {
        assertTrue(
            NutritionWeekMaintenance.shouldCarryGrocery(
                currentWeekKey = "2026-06-15",
                lastMaintainedWeekKey = null,
            ),
        )
        assertTrue(
            NutritionWeekMaintenance.shouldCarryGrocery(
                currentWeekKey = "2026-06-15",
                lastMaintainedWeekKey = "2026-06-08",
            ),
        )
        assertFalse(
            NutritionWeekMaintenance.shouldCarryGrocery(
                currentWeekKey = "2026-06-15",
                lastMaintainedWeekKey = "2026-06-15",
            ),
        )
    }
}
