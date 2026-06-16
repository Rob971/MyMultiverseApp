package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.domain.model.sharing.AppTopic
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.SharingSpace
import app.mymultiverse.kmp.domain.repository.SharingSpaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnconfiguredSharingSpaceRepository : SharingSpaceRepository {
    private val spaces = MutableStateFlow<List<SharingSpace>>(emptyList())

    override fun observeNutritionSpaces(): Flow<List<SharingSpace>> = spaces.asStateFlow()

    override suspend fun createNutritionSpace(
        name: String,
        features: Set<NutritionSharingFeature>,
    ): Result<SharingSpace> = Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun refreshNutritionSpaces() = Unit
}
