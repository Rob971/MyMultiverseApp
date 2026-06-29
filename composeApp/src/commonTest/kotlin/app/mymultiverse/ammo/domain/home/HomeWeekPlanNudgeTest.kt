package app.mymultiverse.ammo.domain.home

import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeWeekPlanNudgeTest {

    @Test
    fun shouldShow_onSundayWithEmptyCurrentWeek() {
        val sunday = LocalDate(2026, 5, 24)
        val weekKey = WeekCalendar.weekKeyFor(sunday)

        assertTrue(
            HomeWeekPlanNudge.shouldShow(
                hasActiveHousehold = true,
                plannedMealSlots = 0,
                weekKey = weekKey,
                today = sunday,
                dismissedWeekKey = null,
            ),
        )
    }

    @Test
    fun shouldShow_hiddenWhenMealsPlanned() {
        val sunday = LocalDate(2026, 5, 24)
        val weekKey = WeekCalendar.weekKeyFor(sunday)

        assertFalse(
            HomeWeekPlanNudge.shouldShow(
                hasActiveHousehold = true,
                plannedMealSlots = 2,
                weekKey = weekKey,
                today = sunday,
                dismissedWeekKey = null,
            ),
        )
    }

    @Test
    fun shouldShow_hiddenWhenNotSunday() {
        val monday = LocalDate(2026, 5, 18)
        val weekKey = WeekCalendar.weekKeyFor(monday)

        assertFalse(
            HomeWeekPlanNudge.shouldShow(
                hasActiveHousehold = true,
                plannedMealSlots = 0,
                weekKey = weekKey,
                today = monday,
                dismissedWeekKey = null,
            ),
        )
    }

    @Test
    fun shouldShow_hiddenWhenDismissedForWeek() {
        val sunday = LocalDate(2026, 5, 24)
        val weekKey = WeekCalendar.weekKeyFor(sunday)

        assertFalse(
            HomeWeekPlanNudge.shouldShow(
                hasActiveHousehold = true,
                plannedMealSlots = 0,
                weekKey = weekKey,
                today = sunday,
                dismissedWeekKey = weekKey,
            ),
        )
    }
}
