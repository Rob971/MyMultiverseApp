package app.mymultiverse.ammo.domain.repository

import kotlinx.coroutines.flow.Flow

interface NutritionHouseholdSelectionStore {
    fun observeActiveHouseholdId(): Flow<String?>

    suspend fun setActiveHouseholdId(householdId: String)

    suspend fun clearActiveHouseholdId()
}
