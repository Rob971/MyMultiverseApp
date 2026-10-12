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
    private val _collaborationActivity = MutableSharedFlow<NutritionCollaborationActivity>(extraBufferCapacity = 8, replay = 8)
    private val _pendingActivities = mutableListOf<NutritionCollaborationActivity>()
    private var activeHouseholdId: String? = null

    override val nutrition = _nutrition.asStateFlow()

    override fun observeSyncStatus(): Flow<NutritionSyncStatus> = syncEngine.observeStatus()

    override fun observeCollaborationActivity(): Flow<NutritionCollaborationActivity> {
        // Emit any activities that accumulated while no subscriber was present.
        // With replay=8, the SharedFlow buffers these for the first subscriber.
        val pending = _pendingActivities.toList()
        _pendingActivities.clear()
        pending.forEach { _collaborationActivity.tryEmit(it) }
        return _collaborationActivity.asSharedFlow()
    }

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
        _pendingActivities.clear()
        _nutrition.value = personalRepository
        logger.breadcrumb("nutrition_session_deactivated")
    }

    private suspend fun bindRepository(householdId: String, weekKey: String) {
        realtimeSync?.stop()
        diagnostics.activeHouseholdId = householdId
        logger.breadcrumb("nutrition_household_activated household_id=$householdId week_key=$weekKey")
        // A household switch means any pending activities from the previous household
        // are stale — they belong to a different grocery list.
        _pendingActivities.clear()
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
            onCollaborationActivity = { activity ->
                // Always try to emit: with replay=8 the item is buffered for
                // the first subscriber even when none is currently registered.
                _collaborationActivity.tryEmit(activity)
                // Also stash in pending so the replay buffer can be cleared
                // on household switch (see deactivate/bindRepository).
                _pendingActivities.add(activity)
                if (_pendingActivities.size > 8) {
                    _pendingActivities.removeAt(0)
                }
            },
        )
        _nutrition.value = repository
        repository.refreshFromRemote()
        if (weekKey == WeekCalendar.currentWeekKey()) {
            val beforeCarry = repository.currentGroceryItems().size
            weekMaintenanceRunner.runForCurrentWeek(
                repository = repository,
                householdId = householdId,
                weekKey = weekKey,
            )
            val afterCarry = repository.currentGroceryItems().size
            val carried = (afterCarry - beforeCarry).coerceAtLeast(0)
            if (carried > 0) {
                logger.breadcrumb("week_maintenance_carry carried=$carried")
            }
        }
        realtimeSync?.start(
            householdId = householdId,
            weekKey = weekKey,
        ) { row ->
            if (row.dataKind == "grocery") {
                val change = repository.applyRemoteWeekData(row)
                if (change != null) {
                    NutritionCollaborationActivityDetector.detectGroceryChanges(
                        before = change.before,
                        after = change.after,
                        actorUserId = row.updatedBy,
                    ).forEach { activity ->
                        _collaborationActivity.tryEmit(activity)
                    }
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
