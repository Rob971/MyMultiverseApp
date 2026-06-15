package app.mymultiverse.kmp.data.repository

import app.mymultiverse.kmp.data.supabase.NutritionSpaceRealtimeSync
import app.mymultiverse.kmp.data.supabase.SyncingNutritionRepository
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NutritionRepositoryHolder(
    private val localFallback: NutritionRepository,
    private val remoteFactory: (spaceId: String) -> NutritionRepository,
    private val realtimeSync: NutritionSpaceRealtimeSync? = null,
) {
    private val _active = MutableStateFlow<NutritionRepository>(localFallback)
    val active: StateFlow<NutritionRepository> = _active.asStateFlow()

    suspend fun activate(spaceId: String) {
        realtimeSync?.stop()
        val repository = remoteFactory(spaceId)
        repository.refreshFromRemote()
        _active.value = repository
        if (repository is SyncingNutritionRepository) {
            realtimeSync?.start(
                spaceId = spaceId,
                weekKey = repository.weekKey,
            ) { row ->
                repository.applyRemoteWeekData(row)
            }
        }
    }

    fun deactivate() {
        realtimeSync?.stop()
        _active.value = localFallback
    }
}
