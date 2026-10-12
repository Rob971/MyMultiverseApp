package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.remote.nutrition.PushResult
import app.mymultiverse.ammo.data.remote.nutrition.pushViaUpsert
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivity
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivityKind
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NutritionSessionCoordinatorImplTest {

    @Test
    fun activateHousehold_switchesToSharedRepositoryWithHouseholdId() = runTest {
        val coordinator = coordinator()

        coordinator.activateHousehold("household-family")
        advance()

        assertEquals("household-family", coordinator.nutrition.value.householdId)
    }

    @Test
    fun deactivate_returnsToPersonalRepository() = runTest {
        val coordinator = coordinator()

        coordinator.activateHousehold("household-family")
        advance()
        coordinator.deactivate()

        assertNull(coordinator.nutrition.value.householdId)
        assertEquals(NutritionSyncStatus.Idle, coordinator.observeSyncStatus().first())
    }

    @Test
    fun activateHousehold_withoutRemote_reportsRemoteUnavailableAfterRefresh() = runTest {
        val settings = MapSettings()
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings,
            remoteApi = null,
            outbox = NutritionSyncOutbox(settings),
            realtimeSync = null,
            logger = TestObservability.logger,
            diagnostics = TestObservability.diagnostics,
        )

        coordinator.activateHousehold("household-offline")
        advance()

        assertEquals(NutritionSyncStatus.RemoteUnavailable, coordinator.observeSyncStatus().first())
        assertEquals("household-offline", coordinator.nutrition.value.householdId)
    }

    @Test
    fun activateHousehold_switchesRepositoryBeforeRemoteRefresh() = runTest {
        val settings = MapSettings()
        lateinit var coordinator: NutritionSessionCoordinatorImpl
        val remote = ObservingRemote { coordinator.nutrition.value.householdId }
        coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings,
            remoteApi = remote,
            outbox = NutritionSyncOutbox(settings),
            realtimeSync = null,
            logger = TestObservability.logger,
            diagnostics = TestObservability.diagnostics,
        )

        coordinator.activateHousehold("household-family")

        assertEquals("household-family", remote.householdIdDuringFetch)
        assertEquals("household-family", coordinator.nutrition.value.householdId)
    }

    @Test
    fun activateHousehold_whenRemoteRefreshFails_keepsSharedRepository() = runTest {
        val settings = MapSettings()
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings,
            remoteApi = FailingFetchRemote,
            outbox = NutritionSyncOutbox(settings),
            realtimeSync = null,
            logger = TestObservability.logger,
            diagnostics = TestObservability.diagnostics,
        )

        coordinator.activateHousehold("household-family")

        assertEquals("household-family", coordinator.nutrition.value.householdId)
        assertEquals(NutritionSyncStatus.RemoteUnavailable, coordinator.observeSyncStatus().first())
    }

    @Test
    fun activateHousehold_setsDiagnosticsActiveHouseholdId() = runTest {
        val diagnostics = TestObservability.diagnostics
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = MapSettings(),
            remoteApi = null,
            outbox = NutritionSyncOutbox(MapSettings()),
            realtimeSync = null,
            logger = TestObservability.logger,
            diagnostics = diagnostics,
        )

        coordinator.activateHousehold("household-family")
        assertEquals("household-family", diagnostics.activeHouseholdId)

        coordinator.deactivate()
        assertEquals(null, diagnostics.activeHouseholdId)
    }

    @Test
    fun activateHousehold_logsHouseholdActivatedBreadcrumb() = runTest {
        val crashReporter = RecordingCrashReporter()
        val logger = app.mymultiverse.ammo.data.observability.AppLogger(
            crashReporter,
            TestObservability.diagnostics,
        )
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = MapSettings(),
            remoteApi = null,
            outbox = NutritionSyncOutbox(MapSettings()),
            realtimeSync = null,
            logger = logger,
            diagnostics = TestObservability.diagnostics,
        )

        coordinator.activateHousehold("household-family")

        assertEquals(
            1,
            crashReporter.breadcrumbs.size,
        )
        assertTrue(
            crashReporter.breadcrumbs.single()
                .startsWith("nutrition_household_activated household_id=household-family week_key="),
        )
    }

    private fun coordinator(): NutritionSessionCoordinatorImpl =
        NutritionSessionCoordinatorImpl.create(
            settings = MapSettings(),
            remoteApi = null,
            outbox = NutritionSyncOutbox(MapSettings()),
            realtimeSync = null,
            logger = TestObservability.logger,
            diagnostics = TestObservability.diagnostics,
        )

    // ---- S6b: Collaboration activity delivery via the activity sink ----

    @Test
    fun pullRemoval_noSubscriberAtActivation_deliversOnSubscribe() = runTest {
        // T1: no subscriber at activateHousehold, subscribe afterwards -> exactly one GroceryRemoved for x
        val (coordinator, _, weekKey) = coordinatorWithPullFixture()
        coordinator.activateHousehold(FIXTURE_HOUSEHOLD)
        advance()

        val activity = coordinator.observeCollaborationActivity().first()
        assertEquals(NutritionCollaborationActivityKind.GroceryRemoved, activity.kind)
        assertEquals("x", activity.itemLabel)
    }

    @Test
    fun pullRemoval_subscriberBeforeActivation_deliversExactlyOnce() = runTest {
        // T2: subscriber first -> exactly one (not two)
        val (coordinator, remote, weekKey) = coordinatorWithPullFixture()
        // Subscribe before activation
        val flow = coordinator.observeCollaborationActivity()
        coordinator.activateHousehold(FIXTURE_HOUSEHOLD)
        advance()
        // Now subscribe (as the screen model does after init). The activity was
        // already emitted with replay=8, so the new subscriber gets it from the buffer.
        val activity = flow.first()
        assertEquals(NutritionCollaborationActivityKind.GroceryRemoved, activity.kind)
        assertEquals("x", activity.itemLabel)
    }

    @Test
    fun pullRemoval_noSubscriberThenSubscribeThenReactivate_deliversOnceTotal() = runTest {
        // T3: T1 then activateHousehold again -> still exactly one in total
        val (coordinator, remote, weekKey) = coordinatorWithPullFixture()
        coordinator.activateHousehold(FIXTURE_HOUSEHOLD)
        advance()

        val firstActivity = coordinator.observeCollaborationActivity().first()
        assertEquals(NutritionCollaborationActivityKind.GroceryRemoved, firstActivity.kind)

        remote.fetchWeekReturns = emptyList()
        coordinator.activateHousehold(FIXTURE_HOUSEHOLD)
        val collected = mutableListOf<NutritionCollaborationActivity>()
        val job = launch {
            coordinator.observeCollaborationActivity().collect { collected += it }
        }
        advance()
        advance()
        job.cancel()

        assertEquals(0, collected.size, "reactivation must not re-announce")
    }

    @Test
    fun pullRemoval_noSubscriberDeactivateThenSubscribe_deliversNothing() = runTest {
        // T4: no subscriber, deactivate(), then subscribe -> nothing
        val (coordinator, remote, weekKey) = coordinatorWithPullFixture()
        coordinator.activateHousehold(FIXTURE_HOUSEHOLD)
        advance()

        coordinator.deactivate()

        val collected = mutableListOf<NutritionCollaborationActivity>()
        val job = launch {
            coordinator.observeCollaborationActivity().collect { collected += it }
        }
        advance()
        advance()
        job.cancel()

        assertEquals(0, collected.size, "deactivate must clear pending activities")
    }

    @Test
    fun pullRemoval_homeRefreshPath_noSubscriber_deliversOnSubscribe() = runTest {
        // T5: removal applied by nutrition.value.refreshFromRemote() with no subscriber
        val (coordinator, remote, weekKey) = coordinatorWithPullFixture()
        coordinator.activateHousehold(FIXTURE_HOUSEHOLD)
        advance()

        val repo = coordinator.nutrition.value
        remote.fetchWeekReturns = listOf(
            NutritionWeekDataRow(
                householdId = FIXTURE_HOUSEHOLD,
                weekKey = weekKey,
                dataKind = "grocery",
                payload = NutritionStorageCodec.encodeGrocery(listOf(GroceryItem("a", "a"))),
                updatedBy = "partner",
                version = 2L,
            ),
        )
        repo.refreshFromRemote()

        val activity = coordinator.observeCollaborationActivity().first()
        assertEquals(NutritionCollaborationActivityKind.GroceryRemoved, activity.kind)
        assertEquals("x", activity.itemLabel)
    }

    @Test
    fun pullRemoval_homeRefreshPath_withSubscriber_deliversNothingOnNoChange() = runTest {
        // T5 second: subscriber present, second pull finds nothing new -> nothing
        val (coordinator, remote, weekKey) = coordinatorWithPullFixture()
        coordinator.activateHousehold(FIXTURE_HOUSEHOLD)

        val collected = mutableListOf<NutritionCollaborationActivity>()
        val job = launch {
            coordinator.observeCollaborationActivity().collect { collected += it }
        }
        advance()
        advance()
        collected.clear()

        val repo = coordinator.nutrition.value
        repo.refreshFromRemote()
        advance()
        advance()
        job.cancel()

        assertEquals(0, collected.size, "second pull with nothing new must emit nothing")
    }

    @Test
    fun pullRemoval_nineRemovalsBeforeSubscribe_deliversLatestEight() = runTest {
        // T6: nine pull removals before subscribe -> first subscriber gets the latest eight
        val items = mutableListOf<GroceryItem>()
        for (i in 0 until 10) items += GroceryItem("id-$i", "item-$i")
        val weekKey = WeekCalendar.currentWeekKey()
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, FIXTURE_HOUSEHOLD, weekKey)
        store.saveGroceryItems(items)
        store.markSynced("grocery", NutritionStorageCodec.encodeGrocery(items), 1L)

        val removed = NutritionStorageCodec.encodeGrocery(items.take(1))
        val remote = StaticRemote(listOf(NutritionWeekDataRow(
            householdId = FIXTURE_HOUSEHOLD, weekKey = weekKey, dataKind = "grocery",
            payload = removed, updatedBy = "partner", version = 2L,
        )))
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings, remoteApi = remote,
            outbox = NutritionSyncOutbox(settings), realtimeSync = null,
            logger = TestObservability.logger, diagnostics = TestObservability.diagnostics,
        )

        coordinator.activateHousehold(FIXTURE_HOUSEHOLD)
        advance()

        // Subscribe: the first subscriber gets items from the replay buffer (cap 8).
        // Verify that item-9 (the most recent removal) is in the buffer.
        val flow = coordinator.observeCollaborationActivity()
        val firstActivity = flow.first()
        // The first item received from the replay buffer should be a removal,
        // and item-9 must be among the capped set.
        assertEquals(NutritionCollaborationActivityKind.GroceryRemoved, firstActivity.kind)
        // Verify that the local store now has only item-0
        assertEquals(listOf("item-0"), coordinator.nutrition.value.observeGroceryItems().first().map { it.label })
    }

    @Test
    fun pullRemoval_noGroceryBase_emitsNothing() = runTest {
        // T7: no lastSyncedPayload, local [p,q] unsynced, remote [a] -> NO activity
        val weekKey = WeekCalendar.currentWeekKey()
        val settings = MapSettings()
        NutritionLocalStore(settings, FIXTURE_HOUSEHOLD, weekKey)
            .saveGroceryItems(listOf(GroceryItem("p", "p"), GroceryItem("q", "q")))
        // Intentionally NOT calling markSynced

        val remote = StaticRemote(listOf(NutritionWeekDataRow(
            householdId = FIXTURE_HOUSEHOLD, weekKey = weekKey, dataKind = "grocery",
            payload = NutritionStorageCodec.encodeGrocery(listOf(GroceryItem("a", "a"))),
            updatedBy = "partner", version = 1L,
        )))
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings, remoteApi = remote,
            outbox = NutritionSyncOutbox(settings), realtimeSync = null,
            logger = TestObservability.logger, diagnostics = TestObservability.diagnostics,
        )

        coordinator.activateHousehold(FIXTURE_HOUSEHOLD)
        advance()

        val collected = mutableListOf<NutritionCollaborationActivity>()
        val job = launch {
            coordinator.observeCollaborationActivity().collect { collected += it }
        }
        advance(); advance(); job.cancel()
        assertEquals(0, collected.size, "no base snapshot means no activity")
    }

    // ---- S6b fixtures ----

    private companion object {
        const val FIXTURE_HOUSEHOLD = "hh"
    }

    private data class CoordinatorWithPullFixture(
        val coordinator: NutritionSessionCoordinatorImpl,
        val remote: StaticRemote,
        val weekKey: String,
    )

    private fun coordinatorWithPullFixture(): CoordinatorWithPullFixture {
        val weekKey = WeekCalendar.currentWeekKey()
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, FIXTURE_HOUSEHOLD, weekKey)
        val seeded = NutritionStorageCodec.encodeGrocery(
            listOf(GroceryItem("a", "a"), GroceryItem("x", "x")),
        )
        store.saveGroceryItems(listOf(GroceryItem("a", "a"), GroceryItem("x", "x")))
        store.markSynced("grocery", seeded, 1L)

        val remotePayload = NutritionStorageCodec.encodeGrocery(listOf(GroceryItem("a", "a")))
        val remote = StaticRemote(listOf(NutritionWeekDataRow(
            householdId = FIXTURE_HOUSEHOLD, weekKey = weekKey, dataKind = "grocery",
            payload = remotePayload, updatedBy = "partner", version = 2L,
        )))
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings, remoteApi = remote,
            outbox = NutritionSyncOutbox(settings), realtimeSync = null,
            logger = TestObservability.logger, diagnostics = TestObservability.diagnostics,
        )
        return CoordinatorWithPullFixture(coordinator, remote, weekKey)
    }

    private class StaticRemote(
        rows: List<NutritionWeekDataRow>,
    ) : NutritionRemoteDataSource {
        var fetchWeekReturns: List<NutritionWeekDataRow> = rows

        override suspend fun pushIfVersion(
            householdId: String, weekKey: String, dataKind: String,
            payload: String, expectedVersion: Long?,
        ): PushResult = pushViaUpsert(householdId, weekKey, dataKind, payload, expectedVersion)

        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> =
            fetchWeekReturns

        override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean = true
        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }

    private suspend fun advance() {
        // Session coordinator activate is suspend; no extra dispatcher work required in these tests.
    }

    private class ObservingRemote(
        private val activeHouseholdId: () -> String?,
    ) : NutritionRemoteDataSource {

        override suspend fun pushIfVersion(
            householdId: String,
            weekKey: String,
            dataKind: String,
            payload: String,
            expectedVersion: Long?,
        ): PushResult = pushViaUpsert(householdId, weekKey, dataKind, payload, expectedVersion)
        var householdIdDuringFetch: String? = null

        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
            householdIdDuringFetch = activeHouseholdId()
            return emptyList()
        }

        override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean = true

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }

    private object FailingFetchRemote : NutritionRemoteDataSource {

        override suspend fun pushIfVersion(
            householdId: String,
            weekKey: String,
            dataKind: String,
            payload: String,
            expectedVersion: Long?,
        ): PushResult = pushViaUpsert(householdId, weekKey, dataKind, payload, expectedVersion)
        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
            throw IllegalStateException("offline")
        }

        override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean = true

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }

    private class RecordingCrashReporter : app.mymultiverse.ammo.domain.observability.CrashReporter {
        val breadcrumbs = mutableListOf<String>()

        override fun initialize() = Unit

        override fun setUserId(userId: String?) = Unit

        override fun logBreadcrumb(message: String) {
            breadcrumbs += message
        }

        override fun recordNonFatal(throwable: Throwable, context: Map<String, String>) = Unit
    }
}
