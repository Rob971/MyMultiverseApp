package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.local.nutrition.NutritionWeekMaintenanceStore
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.repository.NutritionRepositoryImpl
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivity
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivityDetector
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.domain.repository.NutritionRepository
import app.mymultiverse.ammo.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class NutritionSessionCoordinatorImpl(
    private val settings: Settings,
    private val remoteApi: NutritionRemoteDataSource?,
    private val syncEngine: NutritionSyncEngine,
    private val realtimeSync: NutritionHouseholdRealtimeSync?,
    private val diagnostics: DiagnosticsContext,
    private val logger: AppLogger,
    private val weekMaintenanceRunner: NutritionWeekMaintenanceRunner = NutritionWeekMaintenanceRunner(
        settings = settings,
        maintenanceStore = NutritionWeekMaintenanceStore(settings),
        remoteApi = remoteApi,
    ),
) : NutritionSessionCoordinator {

    private val personalRepository = NutritionRepositoryImpl(settings)
    private val _nutrition = MutableStateFlow<NutritionRepository>(personalRepository)
    private val _collaborationActivity = MutableSharedFlow<NutritionCollaborationActivity>(extraBufferCapacity = 8)
    private var activeHouseholdId: String? = null

    override val nutrition = _nutrition.asStateFlow()

    override fun observeSyncStatus(): Flow<NutritionSyncStatus> = syncEngine.observeStatus()

    override fun observeCollaborationActivity(): Flow<NutritionCollaborationActivity> =
        _collaborationActivity.asSharedFlow()

    override suspend fun activateHousehold(householdId: String) {
        activeHouseholdId = householdId
        bindRepository(
            householdId = householdId,
            weekKey = WeekCalendar.currentWeekKey(),
        )
    }

    override suspend fun selectWeek(weekKey: String) {
        val householdId = activeHouseholdId ?: return
        bindRepository(householdId = householdId, weekKey = weekKey)
    }

    override fun deactivate() {
        realtimeSync?.stop()
        syncEngine.markIdle()
        diagnostics.activeHouseholdId = null
        activeHouseholdId = null
        _nutrition.value = personalRepository
    }

    private suspend fun bindRepository(householdId: String, weekKey: String) {
        realtimeSync?.stop()
        diagnostics.activeHouseholdId = householdId
        logger.breadcrumb("nutrition_household_activated household_id=$householdId week_key=$weekKey")
        val repository = OfflineFirstNutritionRepository(
            localStore = NutritionLocalStore(
                settings = settings,
                householdId = householdId,
                weekKey = weekKey,
            ),
            syncEngine = syncEngine,
            householdId = householdId,
            weekKey = weekKey,
            remoteEnabled = remoteApi != null,
        )
        _nutrition.value = repository
        repository.refreshFromRemote()
        if (weekKey == WeekCalendar.currentWeekKey()) {
            weekMaintenanceRunner.runForCurrentWeek(
                repository = repository,
                householdId = householdId,
                weekKey = weekKey,
            )
        }
        realtimeSync?.start(
            householdId = householdId,
            weekKey = weekKey,
        ) { row ->
            if (row.dataKind == "grocery") {
                val before = repository.currentGroceryItems()
                repository.applyRemoteWeekData(row)
                val after = repository.currentGroceryItems()
                NutritionCollaborationActivityDetector.detectGroceryChanges(
                    before = before,
                    after = after,
                    actorUserId = row.updatedBy,
                ).forEach { activity ->
                    _collaborationActivity.tryEmit(activity)
                }
            } else {
                repository.applyRemoteWeekData(row)
            }
        }
    }

    companion object {
        fun create(
            settings: Settings,
            remoteApi: NutritionRemoteDataSource?,
            outbox: NutritionSyncOutbox,
            realtimeSync: NutritionHouseholdRealtimeSync?,
            logger: AppLogger,
            diagnostics: DiagnosticsContext,
        ): NutritionSessionCoordinatorImpl =
            NutritionSessionCoordinatorImpl(
                settings = settings,
                remoteApi = remoteApi,
                syncEngine = NutritionSyncEngine(remoteApi, outbox, logger),
                realtimeSync = realtimeSync,
                diagnostics = diagnostics,
                logger = logger,
            )
    }
}
