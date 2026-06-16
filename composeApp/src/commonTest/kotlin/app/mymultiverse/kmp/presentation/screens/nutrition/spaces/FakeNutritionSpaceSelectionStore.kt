package app.mymultiverse.kmp.presentation.screens.nutrition.spaces

import app.mymultiverse.kmp.domain.repository.NutritionSpaceSelectionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeNutritionSpaceSelectionStore : NutritionSpaceSelectionStore {
    val activeSpaceId = MutableStateFlow<String?>(null)

    override fun observeActiveSpaceId(): Flow<String?> = activeSpaceId.asStateFlow()

    override suspend fun setActiveSpaceId(spaceId: String) {
        activeSpaceId.value = spaceId
    }

    override suspend fun clearActiveSpaceId() {
        activeSpaceId.value = null
    }
}
