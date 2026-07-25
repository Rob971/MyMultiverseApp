package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.sync.NetworkConnectivityMonitor
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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

        assertEquals("household-family", coordinator.nutrition.value.householdId)
    }

    @Test
    fun deactivate_returnsToPersonalRepository() = runTest {
        val coordinator = coordinator()

        coordinator.activateHousehold("household-family")
        coordinator.deactivate()

        assertNull(coordinator.nutrition.value.householdId)
        assertEquals(NutritionSyncStatus.Idle, coordinator.observeSyncStatus().first())
    }

    @Test
    fun activateHousehold_withoutRemote_reportsRemoteUnavailableAfterRefresh() = runTest {
        val settings = MapSettings()
        val coordinator = createCoordinator(
            settings = settings,
            remoteApi = null,
        )

        coordinator.activateHousehold("household-offline")

        assertEquals(NutritionSyncStatus.RemoteUnavailable, coordinator.observeSyncStatus().first())
        assertEquals("household-offline", coordinator.nutrition.value.householdId)
    }

    @Test
    fun activateHousehold_switchesRepositoryBeforeRemoteRefresh() = runTest {
        val settings = MapSettings()
        lateinit var coordinator: NutritionSessionCoordinatorImpl
        val remote = ObservingRemote { coordinator.nutrition.value.householdId }
        coordinator = createCoordinator(
            settings = settings,
            remoteApi = remote,
        )

        coordinator.activateHousehold("household-family")

        assertEquals("household-family", remote.householdIdDuringFetch)
        assertEquals("household-family", coordinator.nutrition.value.householdId)
    }

    @Test
    fun activateHousehold_whenRemoteRefreshFails_keepsSharedRepository() = runTest {
        val settings = MapSettings()
        val coordinator = createCoordinator(
            settings = settings,
            remoteApi = FailingFetchRemote,
        )

        coordinator.activateHousehold("household-family")

        assertEquals("household-family", coordinator.nutrition.value.householdId)
        assertEquals(NutritionSyncStatus.RemoteUnavailable, coordinator.observeSyncStatus().first())
    }

    @Test
    fun activateHousehold_setsDiagnosticsActiveHouseholdId() = runTest {
        val diagnostics = TestObservability.diagnostics
        val coordinator = createCoordinator(
            settings = MapSettings(),
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
        val coordinator = createCoordinator(
            settings = MapSettings(),
            logger = logger,
        )

        coordinator.activateHousehold("household-family")

        assertEquals(1, crashReporter.breadcrumbs.size)
        assertTrue(
            crashReporter.breadcrumbs.single()
                .startsWith("nutrition_household_activated household_id=household-family week_key="),
        )
    }

    @Test
    fun connectivityReturningOnline_retriesPendingFlush() = runTest {
        val settings = MapSettings()
        val outbox = NutritionSyncOutbox(settings)
        outbox.enqueue(
            app.mymultiverse.ammo.data.local.nutrition.PendingNutritionPush(
                householdId = "household-family",
                weekKey = "2025-W24",
                dataKind = "grocery",
                payload = "pending",
                enqueuedAtEpochMs = 1L,
            ),
        )
        val connectivity = FakeConnectivityMonitor(initialOnline = false)
        val remote = ConnectivityAwareRemote(connectivity)
        val supervisor = SupervisorJob()
        val coordinator = createCoordinator(
            settings = settings,
            remoteApi = remote,
            outbox = outbox,
            connectivity = connectivity,
            scope = CoroutineScope(coroutineContext + supervisor),
        )

        try {
            coordinator.activateHousehold("household-family")
            assertEquals(1, outbox.pendingForHousehold("household-family").size)

            connectivity.setOnline(true)
            advanceUntilIdle()

            assertTrue(outbox.pendingForHousehold("household-family").isEmpty())
            assertEquals(1, remote.upsertCount)
        } finally {
            coordinator.deactivate()
            supervisor.cancel()
        }
    }

    private fun coordinator(): NutritionSessionCoordinatorImpl =
        createCoordinator(settings = MapSettings())

    private fun createCoordinator(
        settings: MapSettings = MapSettings(),
        remoteApi: NutritionRemoteDataSource? = null,
        outbox: NutritionSyncOutbox = NutritionSyncOutbox(settings),
        connectivity: FakeConnectivityMonitor = FakeConnectivityMonitor(initialOnline = true),
        scope: CoroutineScope = CoroutineScope(SupervisorJob()),
        logger: app.mymultiverse.ammo.data.observability.AppLogger = TestObservability.logger,
        diagnostics: app.mymultiverse.ammo.domain.observability.DiagnosticsContext = TestObservability.diagnostics,
    ): NutritionSessionCoordinatorImpl =
        NutritionSessionCoordinatorImpl.create(
            settings = settings,
            remoteApi = remoteApi,
            outbox = outbox,
            realtimeSync = null,
            connectivityMonitor = connectivity,
            scope = scope,
            logger = logger,
            diagnostics = diagnostics,
        )

    private class ObservingRemote(
        private val activeHouseholdId: () -> String?,
    ) : NutritionRemoteDataSource {
        var householdIdDuringFetch: String? = null

        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
            householdIdDuringFetch = activeHouseholdId()
            return emptyList()
        }

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }

    private object FailingFetchRemote : NutritionRemoteDataSource {
        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
            throw IllegalStateException("offline")
        }

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }

    private class RecordingRemote : NutritionRemoteDataSource {
        var upsertCount = 0

        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> = emptyList()

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) {
            upsertCount++
        }
    }

    private class ConnectivityAwareRemote(
        private val connectivity: FakeConnectivityMonitor,
    ) : NutritionRemoteDataSource {
        var upsertCount = 0

        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> = emptyList()

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) {
            if (!connectivity.isOnline()) throw IllegalStateException("offline")
            upsertCount++
        }
    }

    private class FakeConnectivityMonitor(
        initialOnline: Boolean,
    ) : NetworkConnectivityMonitor {
        private val online = MutableStateFlow(initialOnline)

        override fun observeOnline(): Flow<Boolean> = online

        override fun isOnline(): Boolean = online.value

        fun setOnline(value: Boolean) {
            online.value = value
        }
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
