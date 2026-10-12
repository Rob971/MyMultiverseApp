@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.remote.nutrition.PushResult
import app.mymultiverse.ammo.data.remote.nutrition.pushViaUpsert
import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivity
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivityKind
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
import app.mymultiverse.ammo.presentation.screens.nutrition.groceryActiveItemListIndex
import app.mymultiverse.ammo.presentation.screens.nutrition.mealPlanActiveItemListIndex
import app.mymultiverse.ammo.presentation.screens.nutrition.shouldShowSyncBannerOnListScreen
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NutritionSessionCoordinatorImplTest {

    @Test
    fun activateHousehold_switchesToSharedRepositoryWithHouseholdId() = runTest {
        val coordinator = coordinator()
        coordinator.activateHousehold("household-family")
        advanceUntilIdle()
        assertEquals("household-family", coordinator.nutrition.value.householdId)
    }

    @Test
    fun deactivate_returnsToPersonalRepository() = runTest {
        val coordinator = coordinator()
        coordinator.activateHousehold("household-family")
        advanceUntilIdle()
        coordinator.deactivate()
        assertNull(coordinator.nutrition.value.householdId)
        assertEquals(NutritionSyncStatus.Idle, coordinator.observeSyncStatus().first())
    }

    @Test
    fun activateHousehold_withoutRemote_reportsRemoteUnavailableAfterRefresh() = runTest {
        val settings = MapSettings()
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings, remoteApi = null,
            outbox = NutritionSyncOutbox(settings), realtimeSync = null,
            logger = TestObservability.logger, diagnostics = TestObservability.diagnostics,
        )
        coordinator.activateHousehold("household-offline")
        advanceUntilIdle()
        assertEquals(NutritionSyncStatus.RemoteUnavailable, coordinator.observeSyncStatus().first())
        assertEquals("household-offline", coordinator.nutrition.value.householdId)
    }

    @Test
    fun activateHousehold_switchesRepositoryBeforeRemoteRefresh() = runTest {
        val settings = MapSettings()
        lateinit var coordinator: NutritionSessionCoordinatorImpl
        val remote = ObservingRemote { coordinator.nutrition.value.householdId }
        coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings, remoteApi = remote,
            outbox = NutritionSyncOutbox(settings), realtimeSync = null,
            logger = TestObservability.logger, diagnostics = TestObservability.diagnostics,
        )
        coordinator.activateHousehold("household-family")
        assertEquals("household-family", remote.householdIdDuringFetch)
        assertEquals("household-family", coordinator.nutrition.value.householdId)
    }

    @Test
    fun activateHousehold_whenRemoteRefreshFails_keepsSharedRepository() = runTest {
        val settings = MapSettings()
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings, remoteApi = FailingFetchRemote,
            outbox = NutritionSyncOutbox(settings), realtimeSync = null,
            logger = TestObservability.logger, diagnostics = TestObservability.diagnostics,
        )
        coordinator.activateHousehold("household-family")
        assertEquals("household-family", coordinator.nutrition.value.householdId)
        assertEquals(NutritionSyncStatus.RemoteUnavailable, coordinator.observeSyncStatus().first())
    }

    @Test
    fun activateHousehold_setsDiagnosticsActiveHouseholdId() = runTest {
        val coordinator = coordinator()
        coordinator.activateHousehold("household-family")
        assertEquals("household-family", TestObservability.diagnostics.activeHouseholdId)
    }

    @Test
    fun activateHousehold_logsHouseholdActivatedBreadcrumb() = runTest {
        val crashReporter = RecordingCrashReporter()
        val logger = app.mymultiverse.ammo.data.observability.AppLogger(crashReporter, TestObservability.diagnostics)
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = MapSettings(), remoteApi = null,
            outbox = NutritionSyncOutbox(MapSettings()), realtimeSync = null,
            logger = logger, diagnostics = TestObservability.diagnostics,
        )
        coordinator.activateHousehold("household-family")
        assertEquals(1, crashReporter.breadcrumbs.size)
        assertTrue(crashReporter.breadcrumbs.single().startsWith("nutrition_household_activated household_id=household-family week_key="))
    }

    @Test
    fun P1_noSubscriberAtActivation_subscribeLater_deliversExactlyOneRemoval() = runTest {
        val fixture = pullFixture()
        fixture.coordinator.activateHousehold(HH)
        advanceUntilIdle()
        val items = mutableListOf<NutritionCollaborationActivity>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.coordinator.observeCollaborationActivity().collect { items += it }
        }
        advanceUntilIdle()

        assertEquals(1, items.size, "P1 count")
        assertEquals("Pasta", items.single().itemLabel, "P1 label")
        assertEquals(
            listOf("Apples"),
            fixture.coordinator.nutrition.value.observeGroceryItems().first().map { it.label },
            "P1 local list is [a]",
        )
    }

    @Test
    fun P2_activateDeactivateThenSubscribe_deliversNothing() = runTest {
        val fixture = pullFixture()
        fixture.coordinator.activateHousehold(HH)
        advanceUntilIdle()
        fixture.coordinator.deactivate()
        val items = mutableListOf<NutritionCollaborationActivity>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.coordinator.observeCollaborationActivity().collect { items += it }
        }
        advanceUntilIdle()

        assertEquals(0, items.size, "P2 count")
    }

    @Test
    fun P3_activateOtherHouseholdThenSubscribe_deliversNothing() = runTest {
        val fixture = pullFixture()
        fixture.coordinator.activateHousehold(HH)
        advanceUntilIdle()
        fixture.coordinator.activateHousehold("other-household")
        advanceUntilIdle()
        val items = mutableListOf<NutritionCollaborationActivity>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.coordinator.observeCollaborationActivity().collect { items += it }
        }
        advanceUntilIdle()

        assertEquals(0, items.size, "P3 count")
    }

    @Test
    fun P4_subscriberFirstThenActivate_firstGetsOne_secondLateGetsZero() = runTest {
        val fixture = pullFixture()
        val first = mutableListOf<NutritionCollaborationActivity>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.coordinator.observeCollaborationActivity().collect { first += it }
        }
        advanceUntilIdle()

        fixture.coordinator.activateHousehold(HH)
        advanceUntilIdle()

        assertEquals(1, first.size, "P4 first count")
        assertEquals("Pasta", first.single().itemLabel, "P4 first label")

        val second = mutableListOf<NutritionCollaborationActivity>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.coordinator.observeCollaborationActivity().collect { second += it }
        }
        advanceUntilIdle()

        assertEquals(0, second.size, "P4 second count")
    }

    @Test
    fun P5_sameHouseholdReactivation_subscribeLater_deliversOnceTotal() = runTest {
        val fixture = pullFixture()
        fixture.coordinator.activateHousehold(HH)
        advanceUntilIdle()
        fixture.coordinator.activateHousehold(HH)
        advanceUntilIdle()
        val items = mutableListOf<NutritionCollaborationActivity>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.coordinator.observeCollaborationActivity().collect { items += it }
        }
        advanceUntilIdle()

        assertEquals(1, items.size, "P5 count (no re-announce, no drain loss)")
        assertEquals("Pasta", items.single().itemLabel, "P5 label")
    }

    @Test
    fun P6_homeRefreshPath_subscribeLater_deliversOnceTotal() = runTest {
        val fixture = pullFixture()
        fixture.coordinator.activateHousehold(HH)
        advanceUntilIdle()
        // Home refresh: second pull finds no change, so no second activity.
        fixture.coordinator.nutrition.value.refreshFromRemote()
        advanceUntilIdle()
        val items = mutableListOf<NutritionCollaborationActivity>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.coordinator.observeCollaborationActivity().collect { items += it }
        }
        advanceUntilIdle()

        assertEquals(1, items.size, "P6 count")
        assertEquals("Pasta", items.single().itemLabel, "P6 label")
    }
@Test
    fun P7_ninePullRemovalsBeforeSubscribe_firstGetsLatestEight() = runTest {
        val weekKey = WeekCalendar.currentWeekKey()
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, HH, weekKey)
        val seeds = (0 until 10).map { i -> GroceryItem(id = "id-$i", label = "item-$i") }
        store.saveGroceryItems(seeds)
        store.markSynced("grocery", NutritionStorageCodec.encodeGrocery(seeds), 1L)
        // Remote keeps only item-0; item-1..item-9 (nine) are removed on the pull.
        val remote = StaticRemote(listOf(NutritionWeekDataRow(
            householdId = HH, weekKey = weekKey, dataKind = "grocery",
            payload = NutritionStorageCodec.encodeGrocery(seeds.take(1)),
            updatedBy = "partner", version = 2L,
        )))
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings, remoteApi = remote,
            outbox = NutritionSyncOutbox(settings), realtimeSync = null,
            logger = TestObservability.logger, diagnostics = TestObservability.diagnostics,
        )
        coordinator.activateHousehold(HH)
        advanceUntilIdle()

        val items = mutableListOf<NutritionCollaborationActivity>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.observeCollaborationActivity().collect { items += it }
        }
        advanceUntilIdle()

        assertEquals(8, items.size, "P7 count: channel capacity 8")
        val labels = items.map { it.itemLabel }.toSet()
        assertTrue("item-9" in labels, "P7 latest kept")
        assertFalse("item-1" in labels, "P7 oldest dropped")
    }

    @Test
    fun P8_noBaseBeforePull_doesNotAnnounceRemovals() = runTest {
        val weekKey = WeekCalendar.currentWeekKey()
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, HH, weekKey)
        // Local unsynced [p,q]; remote [a]. No lastSyncedPayload -> base gate
        // must suppress any announcement.
        store.saveGroceryItems(listOf(GroceryItem("p", "p"), GroceryItem("q", "q")))
        val remote = StaticRemote(listOf(NutritionWeekDataRow(
            householdId = HH, weekKey = weekKey, dataKind = "grocery",
            payload = NutritionStorageCodec.encodeGrocery(listOf(GroceryItem("a", "Apples"))),
            updatedBy = "partner", version = 1L,
        )))
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings, remoteApi = remote,
            outbox = NutritionSyncOutbox(settings), realtimeSync = null,
            logger = TestObservability.logger, diagnostics = TestObservability.diagnostics,
        )
        coordinator.activateHousehold(HH)
        advanceUntilIdle()

        val items = mutableListOf<NutritionCollaborationActivity>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.observeCollaborationActivity().collect { items += it }
        }
        advanceUntilIdle()

        assertEquals(0, items.size, "P8 count")
    }
@Test
    fun P9_realtimeEventWithNoSubscriber_isNotDeliveredToLaterSubscriber() = runTest {
        // Channel holds the single Pasta removal from the pull. A realtime CHECKED
        // event emitted through the seam with NO subscriber must be dropped
        // (SharedFlow replay=0) and must NOT reach the later subscriber.
        val realtime = CapturingRealtimeSync()
        val fixture = pullFixture(realtime = realtime)
        fixture.coordinator.activateHousehold(HH)
        advanceUntilIdle()
        assertTrue(realtime.installedCallback != null, "P9: seam installed at activation")

        realtime.installedCallback!!.invoke(NutritionWeekDataRow(
            householdId = HH, weekKey = fixture.weekKey, dataKind = "grocery",
            payload = NutritionStorageCodec.encodeGrocery(
                listOf(GroceryItem("a", "Apples"), GroceryItem("x", "Pasta", isChecked = true)),
            ),
            updatedBy = "partner", version = 2L,
        ))
        advanceUntilIdle()

        val items = mutableListOf<NutritionCollaborationActivity>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.coordinator.observeCollaborationActivity().collect { items += it }
        }
        advanceUntilIdle()

        assertEquals(1, items.size, "P9 count: only the pull-path removal is delivered")
        assertEquals("Pasta", items.single().itemLabel, "P9 label: realtime checked event is NOT replayed")
        assertEquals(
            NutritionCollaborationActivityKind.GroceryRemoved,
            items.single().kind,
            "P9 kind: the realtime GroceryChecked event is absent",
        )
    }
// ================================================================
    // Slice B: index functions match the real builder item order
    // ================================================================

    @Test
    fun mealPlanIndex_everyFlagCombination() {
        // Real order in WeeklyMealPlanScreenContent: week-selector (0),
        // sync-banner?, week-overview, partner-nudge?, viewer-notice?,
        // empty-state?, daily-section-header. Base 2 + each shown optional.
        // The banner slot is decided inside the function from the status, so both
        // a banner-on status (PendingPush) and banner-off status (Idle) are covered.
        var total = 0
        for (status in listOf(NutritionSyncStatus.Idle, NutritionSyncStatus.PendingPush(2))) {
            for (mask in 0 until 8) {
                val nudge = (mask and 1) != 0
                val viewer = (mask and 2) != 0
                val empty = (mask and 4) != 0
                val expected = 2 + (if (shouldShowSyncBannerOnListScreen(status)) 1 else 0) +
                    listOf(nudge, viewer, empty).count { it }
                assertEquals(
                    expected,
                    mealPlanActiveItemListIndex(status, nudge, viewer, empty),
                    "mealPlan status=$status mask=$mask",
                )
                total++
            }
        }
        assertTrue(total == 16, "mealPlan: 2 banner states x 8 flag combinations = 16 covered")
    }

    @Test
    fun groceryIndex_everyFlagCombination() {
        // Real order: sync-banner?, week-selector (0/1), partner-nudge?,
        // to-buy-header, viewer-notice?, ghost-pairing?, shopping-mode-toggle?,
        // item rows. Base 2 + each shown optional + active offset. Banner slot
        // derived inside from the status (PendingPush vs Idle).
        var total = 0
        for (status in listOf(NutritionSyncStatus.Idle, NutritionSyncStatus.PendingPush(2))) {
            for (mask in 0 until 32) {
                val nudge = (mask and 1) != 0
                val viewer = (mask and 2) != 0
                val ghost = (mask and 4) != 0
                val shopping = (mask and 8) != 0
                val empty = (mask and 16) != 0
                val active = mask % 5
                val expected = when {
                    empty -> -1
                    else -> 2 + (if (shouldShowSyncBannerOnListScreen(status)) 1 else 0) +
                        listOf(nudge, viewer, ghost, shopping).count { it } + active
                }
                assertEquals(
                    expected,
                    groceryActiveItemListIndex(status, nudge, viewer, ghost, shopping, empty, active),
                    "grocery status=$status mask=$mask",
                )
                total++
            }
        }
        assertTrue(total == 64, "grocery: 2 banner states x 32 flag combinations = 64 covered")
    }

    @Test
    fun syncBannerStatusMapping_bannerOnlyCountedForPendingPushAndRemoteUnavailable() {
        // One predicate decides both the rendered banner and its scroll slot.
        val bannerFor = mapOf(
            NutritionSyncStatus.Idle to false,
            NutritionSyncStatus.Syncing to false,
            NutritionSyncStatus.PendingPush(2) to true,
            NutritionSyncStatus.RemoteUnavailable to true,
            NutritionSyncStatus.Synced to false,
        )
        bannerFor.forEach { (status, expected) ->
            assertEquals(expected, shouldShowSyncBannerOnListScreen(status), "predicate for $status")
            val mealPlanWith = mealPlanActiveItemListIndex(status, false, false, false)
            assertEquals(if (expected) 3 else 2, mealPlanWith, "mealPlan banner slot for $status")
            val groceryWith = groceryActiveItemListIndex(status, false, false, false, false, false, 0)
            assertEquals(if (expected) 3 else 2, groceryWith, "grocery banner slot for $status")
        }
    }
private companion object { const val HH = "hh" }

    private fun pullFixture(realtime: NutritionHouseholdRealtimeSync? = null): PullFixture {
        val weekKey = WeekCalendar.currentWeekKey()
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, HH, weekKey)
        val seeded = listOf(GroceryItem("a", "Apples"), GroceryItem("x", "Pasta"))
        store.saveGroceryItems(seeded)
        store.markSynced("grocery", NutritionStorageCodec.encodeGrocery(seeded), 1L)
        val remote = StaticRemote(listOf(NutritionWeekDataRow(
            householdId = HH, weekKey = weekKey, dataKind = "grocery",
            payload = NutritionStorageCodec.encodeGrocery(listOf(GroceryItem("a", "Apples"))),
            updatedBy = "partner", version = 2L,
        )))
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings, remoteApi = remote,
            outbox = NutritionSyncOutbox(settings), realtimeSync = realtime,
            logger = TestObservability.logger, diagnostics = TestObservability.diagnostics,
        )
        return PullFixture(coordinator, weekKey)
    }

    private data class PullFixture(
        val coordinator: NutritionSessionCoordinatorImpl,
        val weekKey: String,
    )

    private fun coordinator(): NutritionSessionCoordinatorImpl =
        NutritionSessionCoordinatorImpl.create(
            settings = MapSettings(), remoteApi = null,
            outbox = NutritionSyncOutbox(MapSettings()), realtimeSync = null,
            logger = TestObservability.logger, diagnostics = TestObservability.diagnostics,
        )

    private class CapturingRealtimeSync : NutritionHouseholdRealtimeSync {
        var installedCallback: (suspend (NutritionWeekDataRow) -> Unit)? = null
            private set

        override fun start(
            householdId: String,
            weekKey: String,
            onUpdate: suspend (NutritionWeekDataRow) -> Unit,
        ) {
            installedCallback = onUpdate
        }

        override fun stop() {
            installedCallback = null
        }
    }

    private class StaticRemote(rows: List<NutritionWeekDataRow>) : NutritionRemoteDataSource {
        var fetchWeekReturns: List<NutritionWeekDataRow> = rows
        override suspend fun pushIfVersion(
            householdId: String, weekKey: String, dataKind: String,
            payload: String, expectedVersion: Long?,
        ): PushResult = pushViaUpsert(householdId, weekKey, dataKind, payload, expectedVersion)
        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> = fetchWeekReturns
        override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean = true
        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }

    private class ObservingRemote(private val activeHouseholdId: () -> String?) : NutritionRemoteDataSource {
        override suspend fun pushIfVersion(
            householdId: String, weekKey: String, dataKind: String,
            payload: String, expectedVersion: Long?,
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
            householdId: String, weekKey: String, dataKind: String,
            payload: String, expectedVersion: Long?,
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
        override fun logBreadcrumb(message: String) { breadcrumbs += message }
        override fun recordNonFatal(throwable: Throwable, context: Map<String, String>) = Unit
    }
}
