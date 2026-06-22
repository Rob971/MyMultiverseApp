package app.mymultiverse.kmp.domain.home

import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Resolves tonight's dinner from the current week's meal plan for the Today tab.
 */
object HomeTonightDinner {

    sealed interface State {
        /** Meal plan week is not the calendar week containing today. */
        data object Hidden : State

        /** Today is in range but dinner slot is empty. */
        data object Unplanned : State

        data class Planned(val title: String) : State
    }

    fun resolve(
        mealPlan: WeeklyMealPlan,
        weekKey: String = mealPlan.weekKey,
        today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    ): State {
        val todayIndex = WeekCalendar.todayIndexInWeek(weekKey, today = today) ?: return State.Hidden
        val dinner = mealPlan.days.getOrNull(todayIndex)?.dinner?.trim().orEmpty()
        return if (dinner.isBlank()) {
            State.Unplanned
        } else {
            State.Planned(dinner)
        }
    }
}
