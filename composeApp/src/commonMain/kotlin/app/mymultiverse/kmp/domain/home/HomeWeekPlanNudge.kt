package app.mymultiverse.kmp.domain.home

import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

object HomeWeekPlanNudge {

    fun shouldShow(
        hasActiveHousehold: Boolean,
        plannedMealSlots: Int,
        weekKey: String,
        today: LocalDate,
        dismissedWeekKey: String?,
    ): Boolean = hasActiveHousehold &&
        plannedMealSlots == 0 &&
        weekKey == WeekCalendar.weekKeyFor(today) &&
        today.dayOfWeek == DayOfWeek.SUNDAY &&
        dismissedWeekKey != weekKey
}
