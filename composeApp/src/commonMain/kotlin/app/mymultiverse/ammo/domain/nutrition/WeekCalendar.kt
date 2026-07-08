package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
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

    fun weekEndDate(weekKey: String): LocalDate {
        val start = LocalDate.parse(weekKey)
        return start.plus(WeeklyMealPlan.DAYS_IN_WEEK - 1, DateTimeUnit.DAY)
    }

    fun nextWeekKey(weekKey: String): String {
        val start = LocalDate.parse(weekKey)
        return start.plus(WeeklyMealPlan.DAYS_IN_WEEK, DateTimeUnit.DAY).toString()
    }

    fun previousWeekKey(weekKey: String): String {
        val start = LocalDate.parse(weekKey)
        return start.minus(WeeklyMealPlan.DAYS_IN_WEEK, DateTimeUnit.DAY).toString()
    }

    fun weekKeyForOffset(
        baseWeekKey: String = currentWeekKey(),
        offset: Int,
    ): String {
        require(offset >= 0) { "week offset must be non-negative" }
        var key = baseWeekKey
        repeat(offset) { key = nextWeekKey(key) }
        return key
    }

    /** Compact range label, e.g. 19/05 – 25/05 (locale-neutral). */
    fun formatWeekRange(weekKey: String): String {
        val start = LocalDate.parse(weekKey)
        val end = weekEndDate(weekKey)
        return "${formatShortDate(start)} – ${formatShortDate(end)}"
    }

    fun todayIndexInWeek(
        weekKey: String,
        today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    ): Int? {
        if (weekKeyFor(today) != weekKey) return null
        val start = LocalDate.parse(weekKey)
        val index = (today.toEpochDays() - start.toEpochDays()).toInt()
        return index.takeIf { it in 0 until WeeklyMealPlan.DAYS_IN_WEEK }
    }

    fun isSunday(
        today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    ): Boolean = today.dayOfWeek == DayOfWeek.SUNDAY

    private fun formatShortDate(date: LocalDate): String {
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        return "$day/$month"
    }
}
