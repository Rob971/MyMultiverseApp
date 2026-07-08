package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.local.nutrition.NutritionWeekMaintenanceStore
import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NutritionWeekMaintenanceRunnerTest {

    @Test
    fun runForCurrentWeek_carriesUncheckedGroceryAndPrunesPastMeals() = runTest {
        val settings = MapSettings()
        val householdId = "household-a"
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"
        val wednesday = LocalDate(2026, 6, 17)

        val previousStore = NutritionLocalStore(settings, householdId, previousWeek)
        previousStore.saveGroceryItems(
            listOf(
                GroceryItem(id = "1", label = "Milk"),
                GroceryItem(id = "2", label = "Bread", isChecked = true),
            ),
        )

        val repository = offlineRepository(settings, householdId, currentWeek)
        repository.saveMealPlan(
            WeeklyMealPlan(
                weekKey = currentWeek,
                days = List(7) { index ->
                    DayMeals(lunch = if (index < 3) "Lunch $index" else "", dinner = "")
                },
            ),
        )

        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = null,
            newItemId = { "carried" },
        )

        runner.runForCurrentWeek(
            repository = repository,
            householdId = householdId,
            weekKey = currentWeek,
            today = wednesday,
        )

        assertEquals(listOf("Milk"), repository.currentGroceryItems().map { it.label })
        assertEquals("", repository.currentMealPlan().days[0].lunch)
        assertEquals("", repository.currentMealPlan().days[1].lunch)
        assertEquals("Lunch 2", repository.currentMealPlan().days[2].lunch)
        assertEquals(
            currentWeek,
            NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId),
        )
    }

    @Test
    fun runForCurrentWeek_skipsWhenViewingNonCurrentWeek() = runTest {
        val settings = MapSettings()
        val householdId = "household-b"
        val pastWeek = "2026-06-08"
        val previousWeek = "2026-06-01"

        NutritionLocalStore(settings, householdId, previousWeek).saveGroceryItems(
            listOf(GroceryItem(id = "1", label = "Milk")),
        )

        val repository = offlineRepository(settings, householdId, pastWeek)
        repository.saveMealPlan(
            WeeklyMealPlan(
                weekKey = pastWeek,
                days = listOf(DayMeals(lunch = "Old lunch")) + List(6) { DayMeals() },
            ),
        )

        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = null,
        )

        runner.runForCurrentWeek(
            repository = repository,
            householdId = householdId,
            weekKey = pastWeek,
            today = LocalDate(2026, 6, 17),
        )

        assertTrue(repository.currentGroceryItems().isEmpty())
        assertEquals("Old lunch", repository.currentMealPlan().days[0].lunch)
        assertEquals(null, NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId))
    }

    @Test
    fun runForCurrentWeek_prefersRemotePreviousWeekGrocery() = runTest {
        val settings = MapSettings()
        val householdId = "household-c"
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"
        val wednesday = LocalDate(2026, 6, 17)
        val repository = offlineRepository(settings, householdId, currentWeek)
        val remote = RecordingRemote(
            rowsByWeek = mapOf(
                previousWeek to listOf(
                    NutritionWeekDataRow(
                        householdId = householdId,
                        weekKey = previousWeek,
                        dataKind = "grocery",
                        payload = NutritionStorageCodec.encodeGrocery(
                            listOf(GroceryItem(id = "remote", label = "Remote eggs")),
                        ),
                        updatedAt = "2026-06-14T12:00:00Z",
                    ),
                ),
            ),
        )

        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = remote,
            newItemId = { "carried-remote" },
        )

        runner.runForCurrentWeek(
            repository = repository,
            householdId = householdId,
            weekKey = currentWeek,
            today = wednesday,
        )

        assertEquals(listOf("Remote eggs"), repository.currentGroceryItems().map { it.label })
        assertEquals(1, remote.fetchCount)
    }

    private fun offlineRepository(
        settings: MapSettings,
        householdId: String,
        weekKey: String,
    ): OfflineFirstNutritionRepository =
        OfflineFirstNutritionRepository(
            localStore = NutritionLocalStore(settings, householdId, weekKey),
            syncEngine = NutritionSyncEngine(null, NutritionSyncOutbox(settings), TestObservability.logger),
            householdId = householdId,
            weekKey = weekKey,
            remoteEnabled = false,
        )

    private class RecordingRemote(
        private val rowsByWeek: Map<String, List<NutritionWeekDataRow>>,
    ) : NutritionRemoteDataSource {
        var fetchCount = 0

        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
            fetchCount++
            return rowsByWeek[weekKey].orEmpty()
        }

        override suspend fun upsert(
            householdId: String,
            weekKey: String,
            dataKind: String,
            payload: String,
        ) = Unit
    }
}
