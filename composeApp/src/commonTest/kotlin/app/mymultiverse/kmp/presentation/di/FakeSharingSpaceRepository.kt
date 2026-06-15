package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.model.sharing.AppTopic
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.SharingSpace
import app.mymultiverse.kmp.domain.repository.SharingSpaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSharingSpaceRepository : SharingSpaceRepository {
    private val spaces = MutableStateFlow<List<SharingSpace>>(emptyList())

    override fun observeNutritionSpaces(): Flow<List<SharingSpace>> = spaces.asStateFlow()

    override suspend fun createNutritionSpace(
        name: String,
        features: Set<NutritionSharingFeature>,
    ): Result<SharingSpace> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("space_name_required"))
        if (features.isEmpty()) return Result.failure(IllegalArgumentException("space_features_required"))

        val created = SharingSpace(
            id = "space-${spaces.value.size + 1}",
            topic = AppTopic.Nutrition,
            name = trimmed,
            ownerId = "test-user",
            features = features,
        )
        spaces.update { current -> current + created }
        return Result.success(created)
    }

    override suspend fun refreshNutritionSpaces() = Unit
}
