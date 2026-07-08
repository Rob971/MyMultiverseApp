package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionStorageKeys
import app.mymultiverse.ammo.data.local.nutrition.NutritionWeekMaintenanceStore
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.nutrition.NutritionWeekMaintenance
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import com.russhwolf.settings.Settings
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Applies grocery carry-over and meal-plan past-day pruning when the session binds
 * to the current calendar week.
 */
class NutritionWeekMaintenanceRunner(
    private val settings: Settings,
    private val maintenanceStore: NutritionWeekMaintenanceStore,
    private val remoteApi: NutritionRemoteDataSource?,
    private val newItemId: () -> String = { "${Clock.System.now().toEpochMilliseconds()}" },
) {
    suspend fun runForCurrentWeek(
        repository: OfflineFirstNutritionRepository,
        householdId: String,
        weekKey: String,
        today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    ) {
        if (weekKey != WeekCalendar.weekKeyFor(today)) return

        val todayIndex = WeekCalendar.todayIndexInWeek(weekKey, today = today)
        pruneMealPlanIfNeeded(repository, todayIndex)

        if (!NutritionWeekMaintenance.shouldCarryGrocery(
                currentWeekKey = weekKey,
                lastMaintainedWeekKey = maintenanceStore.lastMaintainedWeekKey(householdId),
            )
        ) {
            return
        }

        val previousWeekKey = WeekCalendar.previousWeekKey(weekKey)
        val carried = NutritionWeekMaintenance.uncheckedGroceryToCarry(
            loadGroceryItems(householdId, previousWeekKey),
        )
        val merged = NutritionWeekMaintenance.mergeCarriedGrocery(
            carried = carried,
            current = repository.currentGroceryItems(),
            newItemId = newItemId,
        )
        if (merged != null) {
            repository.saveGroceryItems(merged)
        }
        maintenanceStore.setLastMaintainedWeekKey(householdId, weekKey)
    }

    private suspend fun pruneMealPlanIfNeeded(
        repository: OfflineFirstNutritionRepository,
        todayIndex: Int?,
    ) {
        val pruned = NutritionWeekMaintenance.prunePastMealDays(
            plan = repository.currentMealPlan(),
            todayIndex = todayIndex,
        ) ?: return
        repository.saveMealPlan(pruned)
    }

    private suspend fun loadGroceryItems(householdId: String, weekKey: String): List<GroceryItem> {
        val remoteItems = loadRemoteGrocery(householdId, weekKey)
        if (remoteItems.isNotEmpty()) return remoteItems
        return NutritionStorageCodec.decodeGrocery(
            settings.getStringOrNull(NutritionStorageKeys.grocery(householdId, weekKey)),
        )
    }

    private suspend fun loadRemoteGrocery(householdId: String, weekKey: String): List<GroceryItem> {
        val api = remoteApi ?: return emptyList()
        return runCatching {
            api.fetchWeek(householdId, weekKey)
                .filter { row -> row.dataKind == "grocery" }
                .maxByOrNull { row -> row.updatedAtEpochMilliseconds() }
                ?.let { row -> NutritionStorageCodec.decodeGrocery(row.payload) }
                .orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun NutritionWeekDataRow.updatedAtEpochMilliseconds(): Long =
        updatedAt
            ?.let { raw -> runCatching { Instant.parse(raw).toEpochMilliseconds() }.getOrNull() }
            ?: Long.MIN_VALUE
}
