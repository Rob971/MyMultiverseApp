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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow

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
    /**
     * Bounded channel for pull-path removals while no subscriber is listening.
     * Channel has exactly-once, bounded, race-free semantics: items sent via
     * [trySend] are buffered until drained by the first subscriber; overflow
     * drops the oldest item.
     */
    private val pullActivities = Channel<NutritionCollaborationActivity>(
        capacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private var activeHouseholdId: String? = null

    override val nutrition = _nutrition.asStateFlow()

    override fun observeSyncStatus(): Flow<NutritionSyncStatus> = syncEngine.observeStatus()

    override fun observeCollaborationActivity(): Flow<NutritionCollaborationActivity> {
        // Merge pull-path removals (from the bounded channel) with realtime events
        // (from the SharedFlow). Single consumer by design: NutritionScreenModel
        // collects once. A second concurrent collector would split pull items
        // between collectors — the channel has exactly-once delivery.
        return merge(pullActivities.receiveAsFlow(), _collaborationActivity.asSharedFlow())
    }

    override suspend fun activateHousehold(householdId: String) {
        val previousId = activeHouseholdId
        activeHouseholdId = householdId
        bindRepository(
            householdId = householdId,
            weekKey = WeekCalendar.currentWeekKey(),
            previousHouseholdId = previousId,
        )
    }

    override suspend fun selectWeek(weekKey: String) {
        val householdId = activeHouseholdId ?: return
        bindRepository(householdId = householdId, weekKey = weekKey, previousHouseholdId = null)
    }

    override fun deactivate() {
        realtimeSync?.stop()
        syncEngine.markIdle()
        diagnostics.activeHouseholdId = null
        activeHouseholdId = null
        // Drain the pull-activities channel — any undelivered removals belong to
        // the deactivated household and must not surface on the next subscription.
        while (pullActivities.tryReceive().isSuccess) {}
        _nutrition.value = personalRepository
        logger.breadcrumb("nutrition_session_deactivated")
    }

    private suspend fun bindRepository(householdId: String, weekKey: String, previousHouseholdId: String?) {
        realtimeSync?.stop()
        diagnostics.activeHouseholdId = householdId
        logger.breadcrumb("nutrition_household_activated household_id=$householdId week_key=$weekKey")
        // Drain the pull-activities channel when switching to a different household.
        // Same-household activation (e.g. selectWeek) must NOT drain: an undelivered
        // removal from the initial activation must survive the repository rebuild.
        if (previousHouseholdId != null && previousHouseholdId != householdId) {
            while (pullActivities.tryReceive().isSuccess) {}
        }
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
                // Pull-path removals go into the bounded channel. The channel
                // provides exactly-once delivery to the first subscriber; no
                // replay, no duplication, no cross-household leakage.
                pullActivities.trySend(activity)
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
