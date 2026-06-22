package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeekCalendarTest {

    @Test
    fun startOfWeek_returnsMondayForWednesday() {
        val wednesday = LocalDate(2026, 5, 20)

        val monday = WeekCalendar.startOfWeek(wednesday)

        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)
        assertEquals(LocalDate(2026, 5, 18), monday)
    }

    @Test
    fun startOfWeek_returnsSameDateWhenAlreadyMonday() {
        val monday = LocalDate(2026, 5, 18)

        assertEquals(monday, WeekCalendar.startOfWeek(monday))
    }

    @Test
    fun startOfWeek_returnsMondayForSunday() {
        val sunday = LocalDate(2026, 5, 24)

        assertEquals(LocalDate(2026, 5, 18), WeekCalendar.startOfWeek(sunday))
    }

    @Test
    fun weekKeyFor_usesIsoDateOfMonday() {
        val friday = LocalDate(2026, 5, 22)

        assertEquals("2026-05-18", WeekCalendar.weekKeyFor(friday))
    }

    @Test
    fun weekDayDates_returnsSevenConsecutiveDaysFromWeekKey() {
        val dates = WeekCalendar.weekDayDates("2026-05-18")

        assertEquals(WeeklyMealPlan.DAYS_IN_WEEK, dates.size)
        assertEquals(LocalDate(2026, 5, 18), dates.first())
        assertEquals(LocalDate(2026, 5, 24), dates.last())
    }

    @Test
    fun formatWeekRange_usesShortDates() {
        assertEquals("18/05 – 24/05", WeekCalendar.formatWeekRange("2026-05-18"))
    }

    @Test
    fun todayIndexInWeek_returnsIndexForDateInSameWeek() {
        val wednesday = LocalDate(2026, 5, 20)
        val weekKey = WeekCalendar.weekKeyFor(wednesday)

        assertEquals(2, WeekCalendar.todayIndexInWeek(weekKey, today = wednesday))
    }

    @Test
    fun todayIndexInWeek_returnsNullForDifferentWeek() {
        val wednesday = LocalDate(2026, 5, 20)

        assertEquals(null, WeekCalendar.todayIndexInWeek("2020-01-06", today = wednesday))
    }

    @Test
    fun currentWeekKey_matchesWeekKeyForToday() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        assertEquals(WeekCalendar.weekKeyFor(today), WeekCalendar.currentWeekKey())
    }

    @Test
    fun nextWeekKey_advancesSevenDays() {
        assertEquals("2026-05-25", WeekCalendar.nextWeekKey("2026-05-18"))
    }

    @Test
    fun weekKeyForOffset_returnsCurrentAndNextWeek() {
        val current = "2026-05-18"

        assertEquals(current, WeekCalendar.weekKeyForOffset(baseWeekKey = current, offset = 0))
        assertEquals("2026-05-25", WeekCalendar.weekKeyForOffset(baseWeekKey = current, offset = 1))
    }

    @Test
    fun isSunday_detectsSunday() {
        assertTrue(WeekCalendar.isSunday(LocalDate(2026, 5, 24)))
        assertFalse(WeekCalendar.isSunday(LocalDate(2026, 5, 25)))
    }
}
