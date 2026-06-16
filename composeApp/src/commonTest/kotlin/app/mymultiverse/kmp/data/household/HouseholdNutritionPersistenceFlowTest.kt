package app.mymultiverse.kmp.data.household

import app.mymultiverse.kmp.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.kmp.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.kmp.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.kmp.data.sync.NutritionSyncEngine
import app.mymultiverse.kmp.data.sync.OfflineFirstNutritionRepository
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies the household id returned by Supabase bootstrap is the persistence scope
 * for nutrition week data (grocery, meal plan, AI grocery).
 */
class HouseholdNutritionPersistenceFlowTest {

    private val householdId = "household-space-1"
    private val weekKey = "2026-06-16"

    @Test
    fun ensureHousehold_activatesSameIdUsedForNutritionPersistence() = runTest {
        val householdRepository = FakeHouseholdRepository(
            household = Household(
                id = householdId,
                name = "Our household",
                ownerId = "owner-1",
                ownerDisplayName = "Owner",
                nutritionFeatures = setOf(NutritionSharingFeature.Grocery),
            ),
        )

        val household = householdRepository.ensureHousehold().getOrThrow()
        assertEquals(householdId, household.id)

        val settings = MapSettings()
        val remote = RecordingHouseholdRemote()
        val outbox = NutritionSyncOutbox(settings)
        val store = NutritionLocalStore(settings, householdId, weekKey)
        val repository = OfflineFirstNutritionRepository(
            localStore = store,
            syncEngine = NutritionSyncEngine(remote, outbox),
            spaceId = householdId,
            weekKey = weekKey,
            remoteEnabled = true,
        )

        repository.saveGroceryItems(listOf(GroceryItem("1", "Milk", false)))

        assertEquals("Milk", repository.observeGroceryItems().first().single().label)
        assertEquals(1, remote.upserts.size)
        assertEquals(householdId, remote.upserts.single().spaceId)
        assertEquals(0, outbox.pendingFor(householdId, weekKey).size)
    }

    @Test
    fun refreshFromRemote_readsHouseholdScopedWeekRows() = runTest {
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, householdId, weekKey)
        val payload = store.encodeGrocery(listOf(GroceryItem("r1", "Rice", false)))
        val remote = StaticHouseholdRemote(
            listOf(
                NutritionWeekDataRow(
                    spaceId = householdId,
                    weekKey = weekKey,
                    dataKind = "grocery",
                    payload = payload,
                ),
            ),
        )
        val repository = OfflineFirstNutritionRepository(
            localStore = store,
            syncEngine = NutritionSyncEngine(remote, NutritionSyncOutbox(settings)),
            spaceId = householdId,
            weekKey = weekKey,
            remoteEnabled = true,
        )

        repository.refreshFromRemote()

        assertEquals("Rice", repository.observeGroceryItems().first().single().label)
        assertTrue(remote.fetchCalls.any { it.first == householdId && it.second == weekKey })
    }
}

private class RecordingHouseholdRemote : NutritionRemoteDataSource {
    data class UpsertCall(
        val spaceId: String,
        val weekKey: String,
        val dataKind: String,
        val payload: String,
    )

    val upserts = mutableListOf<UpsertCall>()

    override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> = emptyList()

    override suspend fun upsert(spaceId: String, weekKey: String, dataKind: String, payload: String) {
        upserts += UpsertCall(spaceId, weekKey, dataKind, payload)
    }
}

private class StaticHouseholdRemote(
    private val rows: List<NutritionWeekDataRow>,
) : NutritionRemoteDataSource {
    val fetchCalls = mutableListOf<Pair<String, String>>()

    override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> {
        fetchCalls += spaceId to weekKey
        return rows
    }

    override suspend fun upsert(spaceId: String, weekKey: String, dataKind: String, payload: String) = Unit
}
