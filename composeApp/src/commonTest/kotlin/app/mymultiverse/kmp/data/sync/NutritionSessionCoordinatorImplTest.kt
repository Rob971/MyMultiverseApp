package app.mymultiverse.kmp.data.sync

import app.mymultiverse.kmp.data.observability.TestObservability
import app.mymultiverse.kmp.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.kmp.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
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
        val logger = app.mymultiverse.kmp.data.observability.AppLogger(
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

    private suspend fun advance() {
        // Session coordinator activate is suspend; no extra dispatcher work required in these tests.
    }

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

    private class RecordingCrashReporter : app.mymultiverse.kmp.domain.observability.CrashReporter {
        val breadcrumbs = mutableListOf<String>()

        override fun initialize() = Unit

        override fun setUserId(userId: String?) = Unit

        override fun logBreadcrumb(message: String) {
            breadcrumbs += message
        }

        override fun recordNonFatal(throwable: Throwable, context: Map<String, String>) = Unit
    }
}
