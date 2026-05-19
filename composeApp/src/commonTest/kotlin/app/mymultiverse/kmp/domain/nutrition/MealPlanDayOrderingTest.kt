package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import kotlin.test.Test
import kotlin.test.assertEquals

class MealPlanDayOrderingTest {

    @Test
    fun orderDaysForDisplay_keepsOrderWhenTodayIsUnknown() {
        val days = List(WeeklyMealPlan.DAYS_IN_WEEK) { DayMeals(lunch = "L$it") }

        val ordered = MealPlanDayOrdering.orderDaysForDisplay(days, todayIndex = null)

        assertEquals((0 until WeeklyMealPlan.DAYS_IN_WEEK).toList(), ordered.map { it.index })
    }

    @Test
    fun orderDaysForDisplay_putsTodayFirst() {
        val days = List(WeeklyMealPlan.DAYS_IN_WEEK) { index ->
            DayMeals(lunch = "day-$index")
        }

        val ordered = MealPlanDayOrdering.orderDaysForDisplay(days, todayIndex = 3)

        assertEquals(3, ordered.first().index)
        assertEquals("day-3", ordered.first().day.lunch)
        assertEquals(WeeklyMealPlan.DAYS_IN_WEEK, ordered.size)
        assertEquals(listOf(3, 0, 1, 2, 4, 5, 6), ordered.map { it.index })
    }

    @Test
    fun orderDaysForDisplay_ignoresInvalidTodayIndex() {
        val days = List(WeeklyMealPlan.DAYS_IN_WEEK) { DayMeals() }

        val ordered = MealPlanDayOrdering.orderDaysForDisplay(days, todayIndex = 99)

        assertEquals((0 until WeeklyMealPlan.DAYS_IN_WEEK).toList(), ordered.map { it.index })
    }
}
