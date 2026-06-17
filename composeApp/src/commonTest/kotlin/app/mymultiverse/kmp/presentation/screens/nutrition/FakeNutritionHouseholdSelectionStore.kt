package app.mymultiverse.kmp.presentation.screens.nutrition

import app.mymultiverse.kmp.domain.repository.NutritionHouseholdSelectionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeNutritionHouseholdSelectionStore : NutritionHouseholdSelectionStore {
    val activeHouseholdId = MutableStateFlow<String?>(null)

    override fun observeActiveHouseholdId(): Flow<String?> = activeHouseholdId.asStateFlow()

    override suspend fun setActiveHouseholdId(householdId: String) {
        activeHouseholdId.value = householdId
    }

    override suspend fun clearActiveHouseholdId() {
        activeHouseholdId.value = null
    }
}
