package app.mymultiverse.kmp.domain.repository

import kotlinx.coroutines.flow.Flow

interface NutritionSpaceSelectionStore {
    fun observeActiveSpaceId(): Flow<String?>

    suspend fun setActiveSpaceId(spaceId: String)

    suspend fun clearActiveSpaceId()
}
