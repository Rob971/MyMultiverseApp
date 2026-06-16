package app.mymultiverse.kmp.data.sync

import app.mymultiverse.kmp.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.kmp.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.kmp.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NutritionSessionCoordinatorImpl(
    private val settings: Settings,
    private val remoteApi: NutritionRemoteDataSource?,
    private val syncEngine: NutritionSyncEngine,
    private val realtimeSync: NutritionSpaceRealtimeSync?,
) : NutritionSessionCoordinator {

    private val personalRepository = NutritionRepositoryImpl(settings)
    private val _nutrition = MutableStateFlow<NutritionRepository>(personalRepository)

    override val nutrition = _nutrition.asStateFlow()

    override fun observeSyncStatus(): Flow<NutritionSyncStatus> = syncEngine.observeStatus()

    override suspend fun activateSpace(spaceId: String) {
        realtimeSync?.stop()
        val weekKey = WeekCalendar.currentWeekKey()
        val repository = OfflineFirstNutritionRepository(
            localStore = NutritionLocalStore(
                settings = settings,
                spaceId = spaceId,
                weekKey = weekKey,
            ),
            syncEngine = syncEngine,
            spaceId = spaceId,
            weekKey = weekKey,
            remoteEnabled = remoteApi != null,
        )
        _nutrition.value = repository
        repository.refreshFromRemote()
        realtimeSync?.start(
            spaceId = spaceId,
            weekKey = weekKey,
        ) { row ->
            repository.applyRemoteWeekData(row)
        }
    }

    override fun deactivate() {
        realtimeSync?.stop()
        syncEngine.markIdle()
        _nutrition.value = personalRepository
    }

    companion object {
        fun create(
            settings: Settings,
            remoteApi: NutritionRemoteDataSource?,
            outbox: NutritionSyncOutbox,
            realtimeSync: NutritionSpaceRealtimeSync?,
        ): NutritionSessionCoordinatorImpl =
            NutritionSessionCoordinatorImpl(
                settings = settings,
                remoteApi = remoteApi,
                syncEngine = NutritionSyncEngine(remoteApi, outbox),
                realtimeSync = realtimeSync,
            )
    }
}
