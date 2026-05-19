package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

object WeekCalendar {
    fun currentWeekKey(timeZone: TimeZone = TimeZone.currentSystemDefault()): String =
        weekKeyFor(Clock.System.todayIn(timeZone))

    fun weekKeyFor(date: LocalDate): String = startOfWeek(date).toString()

    fun startOfWeek(date: LocalDate): LocalDate {
        val daysFromMonday = (date.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
        return date.minus(daysFromMonday, DateTimeUnit.DAY)
    }

    fun weekDayDates(weekKey: String): List<LocalDate> {
        val start = LocalDate.parse(weekKey)
        return List(WeeklyMealPlan.DAYS_IN_WEEK) { index ->
            start.plus(index, DateTimeUnit.DAY)
        }
    }
}
