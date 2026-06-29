package app.mymultiverse.ammo.domain.home

import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HomeTonightDinnerTest {

    @Test
    fun resolve_returnsPlannedWhenDinnerSetForToday() {
        val wednesday = LocalDate(2026, 6, 17)
        val weekKey = "2026-06-15"
        val plan = WeeklyMealPlan(
            weekKey = weekKey,
            days = List(7) { DayMeals() }.toMutableList().apply {
                this[2] = DayMeals(dinner = "Pasta night")
            },
        )

        val result = HomeTonightDinner.resolve(plan, weekKey = weekKey, today = wednesday)

        assertEquals(HomeTonightDinner.State.Planned("Pasta night"), result)
    }

    @Test
    fun resolve_returnsUnplannedWhenDinnerEmpty() {
        val wednesday = LocalDate(2026, 6, 17)
        val weekKey = "2026-06-15"
        val plan = WeeklyMealPlan(weekKey = weekKey)

        val result = HomeTonightDinner.resolve(plan, weekKey = weekKey, today = wednesday)

        assertIs<HomeTonightDinner.State.Unplanned>(result)
    }

    @Test
    fun resolve_returnsHiddenForDifferentWeek() {
        val plan = WeeklyMealPlan(
            weekKey = "2026-06-15",
            days = listOf(DayMeals(dinner = "Soup")),
        )

        val result = HomeTonightDinner.resolve(
            plan,
            weekKey = "2026-06-15",
            today = LocalDate(2026, 6, 24),
        )

        assertEquals(HomeTonightDinner.State.Hidden, result)
    }
}
