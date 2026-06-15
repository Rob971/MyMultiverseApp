package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeNutritionSessionCoordinator(
    initialRepository: NutritionRepository,
) : NutritionSessionCoordinator {
    private val _nutrition = MutableStateFlow(initialRepository)

    override val nutrition = _nutrition.asStateFlow()

    override fun observeSyncStatus(): Flow<NutritionSyncStatus> = flowOf(NutritionSyncStatus.Idle)

    override suspend fun activateSpace(spaceId: String) = Unit

    override fun deactivate() = Unit

    fun setRepository(repository: NutritionRepository) {
        _nutrition.value = repository
    }
}
