package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan

/**
 * Pure 3-way merge for a weekly meal plan, cell by cell.
 *
 * The unit of merge is the CELL: day index x (lunch|dinner), 14 cells in total.
 * For each cell, given the common [base], this device's [local] and the
 * server's [remote]:
 *  - local == remote       -> that value
 *  - local == base         -> only remote changed, take remote
 *  - remote == base        -> only local changed, take local
 *  - both changed, differ  -> the PUSHING device (local) wins the cell
 * Clearing a cell (empty string) is a change like any other: a cell emptied on
 * one side is never resurrected by a device whose base still held it.
 *
 * With a null [base] (a device that never synced) there is no common anchor,
 * so cells are unioned: a non-empty local cell wins over an empty remote cell,
 * otherwise remote wins (including both sides non-empty and differing).
 *
 * If the week keys mismatch or any side does not contain exactly 7 days,
 * [local] is returned unchanged and no exception is thrown — a malformed
 * payload must never crash the sync path.
 */
object MealPlanMerge {

    fun merge(
        base: WeeklyMealPlan?,
        local: WeeklyMealPlan,
        remote: WeeklyMealPlan,
    ): WeeklyMealPlan {
        if (local.weekKey != remote.weekKey) return local
        if (local.days.size != WeeklyMealPlan.DAYS_IN_WEEK || remote.days.size != WeeklyMealPlan.DAYS_IN_WEEK) {
            return local
        }
        if (base != null &&
            (base.weekKey != local.weekKey || base.days.size != WeeklyMealPlan.DAYS_IN_WEEK)
        ) {
            return local
        }
        val mergedDays = List(WeeklyMealPlan.DAYS_IN_WEEK) { index ->
            DayMeals(
                lunch = resolveCell(base?.days?.get(index)?.lunch, local.days[index].lunch, remote.days[index].lunch),
                dinner = resolveCell(base?.days?.get(index)?.dinner, local.days[index].dinner, remote.days[index].dinner),
            )
        }
        return local.copy(days = mergedDays)
    }

    private fun resolveCell(base: String?, local: String, remote: String): String = when {
        local == remote -> local
        base == null -> if (local.isNotEmpty() && remote.isEmpty()) local else remote
        local == base -> remote
        remote == base -> local
        else -> local // both changed and differ -> the pushing device wins
    }
}