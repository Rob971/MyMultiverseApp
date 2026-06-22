package app.mymultiverse.kmp.data.sync

import app.mymultiverse.kmp.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Local-first nutrition repository for a household: writes are immediate on device,
 * pushes go through [NutritionSyncEngine] (queued when offline).
 */
class OfflineFirstNutritionRepository(
    private val localStore: NutritionLocalStore,
    private val syncEngine: NutritionSyncEngine,
    override val householdId: String,
    override val weekKey: String,
    private val remoteEnabled: Boolean,
) : NutritionRepository {

    override fun observeGroceryItems(): Flow<List<GroceryItem>> = localStore.observeGroceryItems()

    override fun observeAiGroceryItems(): Flow<List<GroceryItem>> = localStore.observeAiGroceryItems()

    override fun observeMealPlan(): Flow<WeeklyMealPlan> = localStore.observeMealPlan()

    override suspend fun refreshFromRemote() {
        if (!remoteEnabled) {
            syncEngine.markRemoteUnavailable()
            return
        }
        syncEngine.flushPending(householdId, weekKey)
        syncEngine.pullRemote(householdId, weekKey) { applyRemoteWeekData(it) }
    }

    fun currentGroceryItems(): List<GroceryItem> = localStore.currentGroceryItems()

    fun applyRemoteWeekData(row: NutritionWeekDataRow) {
        if (row.householdId != householdId || row.weekKey != weekKey) return
        localStore.applyPayload(row.dataKind, row.payload)
    }

    override suspend fun saveGroceryItems(items: List<GroceryItem>) {
        localStore.saveGroceryItems(items)
        if (remoteEnabled) {
            syncEngine.pushNowOrEnqueue(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "grocery",
                payload = localStore.encodeGrocery(items),
            )
        }
    }

    override suspend fun saveAiGroceryItems(items: List<GroceryItem>) {
        localStore.saveAiGroceryItems(items)
        if (remoteEnabled) {
            syncEngine.pushNowOrEnqueue(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "ai_grocery",
                payload = localStore.encodeGrocery(items),
            )
        }
    }

    override suspend fun saveMealPlan(plan: WeeklyMealPlan) {
        localStore.saveMealPlan(plan)
        if (remoteEnabled) {
            syncEngine.pushNowOrEnqueue(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "meal_plan",
                payload = localStore.encodeMealPlan(plan),
            )
        }
    }
}
