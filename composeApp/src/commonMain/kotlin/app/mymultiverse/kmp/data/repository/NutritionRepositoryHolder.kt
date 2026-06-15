package app.mymultiverse.kmp.data.repository

import app.mymultiverse.kmp.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NutritionRepositoryHolder(
    private val localFallback: NutritionRepository,
    private val remoteFactory: (spaceId: String) -> NutritionRepository,
) {
    private val _active = MutableStateFlow<NutritionRepository>(localFallback)
    val active: StateFlow<NutritionRepository> = _active.asStateFlow()

    suspend fun activate(spaceId: String) {
        val repository = remoteFactory(spaceId)
        repository.refreshFromRemote()
        _active.value = repository
    }

    fun deactivate() {
        _active.value = localFallback
    }
}
