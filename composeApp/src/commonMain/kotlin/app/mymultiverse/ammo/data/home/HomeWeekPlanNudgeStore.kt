package app.mymultiverse.ammo.data.home

import com.russhwolf.settings.Settings

class HomeWeekPlanNudgeStore(
    private val settings: Settings,
) {
    fun dismissedWeekKey(householdId: String): String? =
        settings.getStringOrNull(dismissKey(householdId))

    fun setDismissed(householdId: String, weekKey: String) {
        settings.putString(dismissKey(householdId), weekKey)
    }

    fun clearDismissed(householdId: String) {
        settings.remove(dismissKey(householdId))
    }

    private fun dismissKey(householdId: String): String =
        "home_week_plan_nudge_dismissed_$householdId"
}
