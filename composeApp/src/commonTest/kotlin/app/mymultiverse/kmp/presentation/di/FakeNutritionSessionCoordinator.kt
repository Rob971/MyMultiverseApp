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

    var activatedHouseholdId: String? = null
        private set
    var deactivateCount: Int = 0
        private set

    override val nutrition = _nutrition.asStateFlow()

    override fun observeSyncStatus(): Flow<NutritionSyncStatus> = flowOf(NutritionSyncStatus.Idle)

    override suspend fun activateHousehold(householdId: String) {
        activatedHouseholdId = householdId
    }

    override suspend fun selectWeek(weekKey: String) {
        // Tests use setRepository directly; production uses NutritionSessionCoordinatorImpl.
    }

    override fun deactivate() {
        deactivateCount++
    }

    fun setRepository(repository: NutritionRepository) {
        _nutrition.value = repository
    }
}
