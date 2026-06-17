package app.mymultiverse.kmp.data.sync

import app.mymultiverse.kmp.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.kmp.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.kmp.data.observability.TestObservability
import app.mymultiverse.kmp.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineFirstNutritionRepositoryTest {

    private val weekKey = "2026-05-18"
    private val householdId = "household-1"

    @Test
    fun saveGrocery_persistsLocallyWhenRemoteDisabled() = runTest {
        val settings = MapSettings()
        val repository = repository(settings, remoteEnabled = false)

        repository.saveGroceryItems(listOf(GroceryItem("1", "Milk", false)))

        assertEquals("Milk", repository.observeGroceryItems().first().single().label)
        assertEquals(0, NutritionSyncOutbox(settings).pendingFor(householdId, weekKey).size)
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
        assertEquals(1, outbox.pendingFor(householdId, weekKey).size)
    }

    @Test
    fun refreshFromRemote_appliesFetchedSupabaseRowsToLocalCache() = runTest {
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, householdId, weekKey)
        val plan = WeeklyMealPlan(
            weekKey = weekKey,
            days = List(WeeklyMealPlan.DAYS_IN_WEEK) { index ->
                if (index == 0) DayMeals(lunch = "Remote lunch", dinner = "Remote dinner") else DayMeals()
            },
        )
        val remote = StaticRemote(
            listOf(
                NutritionWeekDataRow(
                    householdId = householdId,
                    weekKey = weekKey,
                    dataKind = "grocery",
                    payload = store.encodeGrocery(listOf(GroceryItem("r1", "Remote rice", false))),
                ),
                NutritionWeekDataRow(
                    householdId = householdId,
                    weekKey = weekKey,
                    dataKind = "ai_grocery",
                    payload = store.encodeGrocery(listOf(GroceryItem("ai1", "AI lentils", true))),
                ),
                NutritionWeekDataRow(
                    householdId = householdId,
                    weekKey = weekKey,
                    dataKind = "meal_plan",
                    payload = store.encodeMealPlan(plan),
                ),
            ),
        )
        val repository = repository(settings, store = store, remote = remote, remoteEnabled = true)

        repository.refreshFromRemote()

        assertEquals("Remote rice", repository.observeGroceryItems().first().single().label)
        assertEquals("AI lentils", repository.observeAiGroceryItems().first().single().label)
        assertEquals("Remote lunch", repository.observeMealPlan().first().days.first().lunch)
        assertEquals(1, remote.fetchCount)
    }

    @Test
    fun applyRemoteWeekData_updatesLocalCache() = runTest {
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, householdId, weekKey)
        val repository = repository(settings, store = store, remoteEnabled = false)
        val payload = store.encodeGrocery(listOf(GroceryItem("r1", "Remote rice", false)))

        repository.applyRemoteWeekData(
            NutritionWeekDataRow(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "grocery",
                payload = payload,
            ),
        )

        assertEquals("Remote rice", repository.observeGroceryItems().first().single().label)
    }

    @Test
    fun applyRemoteWeekData_ignoresOtherHouseholdOrWeek() = runTest {
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, householdId, weekKey)
        val repository = repository(settings, store = store, remoteEnabled = false)
        val payload = store.encodeGrocery(listOf(GroceryItem("1", "Ignored", false)))

        repository.applyRemoteWeekData(
            NutritionWeekDataRow(
                householdId = "other-household",
                weekKey = weekKey,
                dataKind = "grocery",
                payload = payload,
            ),
        )

        assertTrue(repository.observeGroceryItems().first().isEmpty())
    }

    private fun repository(
        settings: MapSettings,
        store: NutritionLocalStore = NutritionLocalStore(settings, householdId, weekKey),
        remote: NutritionRemoteDataSource? = null,
        outbox: NutritionSyncOutbox = NutritionSyncOutbox(settings),
        remoteEnabled: Boolean = remote != null,
    ): OfflineFirstNutritionRepository =
        OfflineFirstNutritionRepository(
            localStore = store,
            syncEngine = NutritionSyncEngine(remote, outbox, TestObservability.logger),
            householdId = householdId,
            weekKey = weekKey,
            remoteEnabled = remoteEnabled,
        )

    private object FailingRemote : NutritionRemoteDataSource {
        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> = emptyList()

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) {
            throw IllegalStateException("offline")
        }
    }

    private class StaticRemote(
        private val rows: List<NutritionWeekDataRow>,
    ) : NutritionRemoteDataSource {
        var fetchCount = 0

        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
            fetchCount++
            return rows
        }

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }
}
