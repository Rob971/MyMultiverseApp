package app.mymultiverse.ammo.data.repository

import app.mymultiverse.ammo.domain.repository.NutritionHouseholdSelectionStore
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsNutritionHouseholdSelectionStore(
    private val settings: Settings,
) : NutritionHouseholdSelectionStore {
    private val activeHouseholdId = MutableStateFlow(
        settings.getStringOrNull(KEY_ACTIVE_HOUSEHOLD_ID)?.takeIf { it.isNotBlank() },
    )

    override fun observeActiveHouseholdId(): Flow<String?> = activeHouseholdId.asStateFlow()

    override suspend fun setActiveHouseholdId(householdId: String) {
        settings.putString(KEY_ACTIVE_HOUSEHOLD_ID, householdId)
        activeHouseholdId.value = householdId
    }

    override suspend fun clearActiveHouseholdId() {
        settings.remove(KEY_ACTIVE_HOUSEHOLD_ID)
        activeHouseholdId.value = null
    }

    private companion object {
        const val KEY_ACTIVE_HOUSEHOLD_ID = "nutrition_active_household_id"
    }
}
