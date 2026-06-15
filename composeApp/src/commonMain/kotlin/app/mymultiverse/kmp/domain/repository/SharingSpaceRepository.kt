package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.SharingSpace
import kotlinx.coroutines.flow.Flow

interface SharingSpaceRepository {
    fun observeNutritionSpaces(): Flow<List<SharingSpace>>

    suspend fun createNutritionSpace(
        name: String,
        features: Set<NutritionSharingFeature>,
    ): Result<SharingSpace>

    suspend fun refreshNutritionSpaces()
}
