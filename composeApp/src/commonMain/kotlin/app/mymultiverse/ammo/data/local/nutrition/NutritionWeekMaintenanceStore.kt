package app.mymultiverse.ammo.data.local.nutrition

import com.russhwolf.settings.Settings

/**
 * Tracks the last calendar week for which grocery carry-over ran for a nutrition scope.
 */
class NutritionWeekMaintenanceStore(
    private val settings: Settings,
) {
    fun lastMaintainedWeekKey(householdId: String?): String? =
        settings.getStringOrNull(maintenanceKey(householdId))

    fun setLastMaintainedWeekKey(householdId: String?, weekKey: String) {
        settings.putString(maintenanceKey(householdId), weekKey)
    }

    private fun maintenanceKey(householdId: String?): String =
        if (householdId == null) {
            "nutrition_maintenance_week_personal"
        } else {
            "nutrition_maintenance_week_$householdId"
        }
}
