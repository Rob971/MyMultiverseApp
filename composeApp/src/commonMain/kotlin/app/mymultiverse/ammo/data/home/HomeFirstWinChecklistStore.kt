package app.mymultiverse.ammo.data.home

import com.russhwolf.settings.Settings

class HomeFirstWinChecklistStore(
    private val settings: Settings,
) {
    fun isDismissed(householdId: String): Boolean =
        settings.getBoolean(dismissKey(householdId), defaultValue = false)

    fun setDismissed(householdId: String) {
        settings.putBoolean(dismissKey(householdId), true)
    }

    fun clearDismissed(householdId: String) {
        settings.remove(dismissKey(householdId))
    }

    private fun dismissKey(householdId: String): String =
        "home_first_win_dismissed_$householdId"
}
