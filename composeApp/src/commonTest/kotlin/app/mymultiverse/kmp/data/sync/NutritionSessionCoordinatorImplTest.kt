package app.mymultiverse.kmp.data.sync

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

class NutritionSessionCoordinatorImplTest {

    @Test
    fun activateSpace_switchesToSharedRepositoryWithSpaceId() = runTest {
        val coordinator = coordinator()

        coordinator.activateSpace("space-family")
        advance()

        assertEquals("space-family", coordinator.nutrition.value.spaceId)
    }

    @Test
    fun deactivate_returnsToPersonalRepository() = runTest {
        val coordinator = coordinator()

        coordinator.activateSpace("space-family")
        advance()
        coordinator.deactivate()

        assertNull(coordinator.nutrition.value.spaceId)
        assertEquals(NutritionSyncStatus.Idle, coordinator.observeSyncStatus().first())
    }

    @Test
    fun activateSpace_withoutRemote_reportsRemoteUnavailableAfterRefresh() = runTest {
        val settings = MapSettings()
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings,
            remoteApi = null,
            outbox = NutritionSyncOutbox(settings),
            realtimeSync = null,
        )

        coordinator.activateSpace("space-offline")
        advance()

        assertEquals(NutritionSyncStatus.RemoteUnavailable, coordinator.observeSyncStatus().first())
        assertEquals("space-offline", coordinator.nutrition.value.spaceId)
    }

    @Test
    fun activateSpace_switchesRepositoryBeforeRemoteRefresh() = runTest {
        val settings = MapSettings()
        lateinit var coordinator: NutritionSessionCoordinatorImpl
        val remote = ObservingRemote { coordinator.nutrition.value.spaceId }
        coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings,
            remoteApi = remote,
            outbox = NutritionSyncOutbox(settings),
            realtimeSync = null,
        )

        coordinator.activateSpace("space-family")

        assertEquals("space-family", remote.spaceIdDuringFetch)
        assertEquals("space-family", coordinator.nutrition.value.spaceId)
    }

    @Test
    fun activateSpace_whenRemoteRefreshFails_keepsSharedRepository() = runTest {
        val settings = MapSettings()
        val coordinator = NutritionSessionCoordinatorImpl.create(
            settings = settings,
            remoteApi = FailingFetchRemote,
            outbox = NutritionSyncOutbox(settings),
            realtimeSync = null,
        )

        coordinator.activateSpace("space-family")

        assertEquals("space-family", coordinator.nutrition.value.spaceId)
        assertEquals(NutritionSyncStatus.RemoteUnavailable, coordinator.observeSyncStatus().first())
    }

    private fun coordinator(): NutritionSessionCoordinatorImpl =
        NutritionSessionCoordinatorImpl.create(
            settings = MapSettings(),
            remoteApi = null,
            outbox = NutritionSyncOutbox(MapSettings()),
            realtimeSync = null,
        )

    private suspend fun advance() {
        // Session coordinator activate is suspend; no extra dispatcher work required in these tests.
    }

    private class ObservingRemote(
        private val activeSpaceId: () -> String?,
    ) : NutritionRemoteDataSource {
        var spaceIdDuringFetch: String? = null

        override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> {
            spaceIdDuringFetch = activeSpaceId()
            return emptyList()
        }

        override suspend fun upsert(spaceId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }

    private object FailingFetchRemote : NutritionRemoteDataSource {
        override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> {
            throw IllegalStateException("offline")
        }

        override suspend fun upsert(spaceId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }
}
