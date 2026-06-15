package app.mymultiverse.kmp.data.sync

import app.mymultiverse.kmp.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.kmp.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.kmp.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineFirstNutritionRepositoryTest {

    private val weekKey = "2026-05-18"
    private val spaceId = "space-1"

    @Test
    fun saveGrocery_persistsLocallyWhenRemoteDisabled() = runTest {
        val settings = MapSettings()
        val repository = repository(settings, remoteEnabled = false)

        repository.saveGroceryItems(listOf(GroceryItem("1", "Milk", false)))

        assertEquals("Milk", repository.observeGroceryItems().first().single().label)
        assertEquals(0, NutritionSyncOutbox(settings).pendingFor(spaceId, weekKey).size)
    }

    @Test
    fun saveGrocery_enqueuesWhenRemotePushFails() = runTest {
        val settings = MapSettings()
        val outbox = NutritionSyncOutbox(settings)
        val repository = repository(
            settings = settings,
            remote = FailingRemote,
            outbox = outbox,
            remoteEnabled = true,
        )

        repository.saveGroceryItems(listOf(GroceryItem("1", "Bread", false)))

        assertEquals("Bread", repository.observeGroceryItems().first().single().label)
        assertEquals(1, outbox.pendingFor(spaceId, weekKey).size)
    }

    @Test
    fun applyRemoteWeekData_updatesLocalCache() = runTest {
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, spaceId, weekKey)
        val repository = repository(settings, store = store, remoteEnabled = false)
        val payload = store.encodeGrocery(listOf(GroceryItem("r1", "Remote rice", false)))

        repository.applyRemoteWeekData(
            NutritionWeekDataRow(
                spaceId = spaceId,
                weekKey = weekKey,
                dataKind = "grocery",
                payload = payload,
            ),
        )

        assertEquals("Remote rice", repository.observeGroceryItems().first().single().label)
    }

    @Test
    fun applyRemoteWeekData_ignoresOtherSpaceOrWeek() = runTest {
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, spaceId, weekKey)
        val repository = repository(settings, store = store, remoteEnabled = false)
        val payload = store.encodeGrocery(listOf(GroceryItem("1", "Ignored", false)))

        repository.applyRemoteWeekData(
            NutritionWeekDataRow(
                spaceId = "other-space",
                weekKey = weekKey,
                dataKind = "grocery",
                payload = payload,
            ),
        )

        assertTrue(repository.observeGroceryItems().first().isEmpty())
    }

    private fun repository(
        settings: MapSettings,
        store: NutritionLocalStore = NutritionLocalStore(settings, spaceId, weekKey),
        remote: NutritionRemoteDataSource? = null,
        outbox: NutritionSyncOutbox = NutritionSyncOutbox(settings),
        remoteEnabled: Boolean = remote != null,
    ): OfflineFirstNutritionRepository =
        OfflineFirstNutritionRepository(
            localStore = store,
            syncEngine = NutritionSyncEngine(remote, outbox),
            spaceId = spaceId,
            weekKey = weekKey,
            remoteEnabled = remoteEnabled,
        )

    private object FailingRemote : NutritionRemoteDataSource {
        override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> = emptyList()

        override suspend fun upsert(spaceId: String, weekKey: String, dataKind: String, payload: String) {
            throw IllegalStateException("offline")
        }
    }
}
