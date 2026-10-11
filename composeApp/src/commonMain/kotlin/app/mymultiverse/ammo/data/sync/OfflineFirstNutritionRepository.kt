package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.GroceryMerge
import app.mymultiverse.ammo.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class GroceryChange(
    val before: List<GroceryItem>,
    val after: List<GroceryItem>,
)

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

    private val mutex = Mutex()

    override fun observeGroceryItems(): Flow<List<GroceryItem>> = localStore.observeGroceryItems()

    override fun observeAiGroceryItems(): Flow<List<GroceryItem>> = localStore.observeAiGroceryItems()

    override fun observeMealPlan(): Flow<WeeklyMealPlan> = localStore.observeMealPlan()

    override suspend fun refreshFromRemote() {
        if (!remoteEnabled) {
            syncEngine.markRemoteUnavailable()
            return
        }
        val pulled = syncEngine.pullRemote(householdId, weekKey) { applyRemoteWeekData(it) }
        if (pulled) {
            mutex.withLock {
                syncEngine.flushPending(householdId, weekKey)
                    .filter { it.dataKind == "grocery" || it.dataKind == "ai_grocery" }
                    .forEach { localStore.markSynced(it.dataKind, it.payload) }
            }
        }
    }

    fun currentGroceryItems(): List<GroceryItem> = localStore.currentGroceryItems()

    fun currentMealPlan(): WeeklyMealPlan = localStore.currentMealPlan()

    suspend fun applyRemoteWeekData(row: NutritionWeekDataRow): GroceryChange? {
        if (row.householdId != householdId || row.weekKey != weekKey) return null
        if (row.dataKind == "meal_plan") {
            if (syncEngine.hasPending(householdId, weekKey, row.dataKind)) return null
            localStore.applyPayload(row.dataKind, row.payload)
            return null
        }
        val result = mutex.withLock { applyGroceryRemoteLocked(row) }
        // Return GroceryChange only for "grocery"; ai_grocery processes but returns null
        return if (row.dataKind == "grocery") result else null
    }

    private suspend fun applyGroceryRemoteLocked(row: NutritionWeekDataRow): GroceryChange? {
        val dataKind = row.dataKind
        val remoteItems = NutritionStorageCodec.decodeGrocery(row.payload)
        val localItems = currentItems(dataKind)
        val before = localItems.toList()
        val pending = syncEngine.hasPending(householdId, weekKey, dataKind)
        val basePayload = localStore.lastSyncedPayload(dataKind)
        val merged = if (basePayload == null && !pending) {
            remoteItems
        } else {
            val baseItems = if (basePayload != null) {
                NutritionStorageCodec.decodeGrocery(basePayload)
            } else {
                emptyList()
            }
            GroceryMerge.merge(baseItems, localItems, remoteItems)
        }
        saveLocalItems(dataKind, merged)
        localStore.markSynced(dataKind, row.payload)
        if (merged != remoteItems) {
            if (remoteEnabled) {
                val payload = localStore.encodeGrocery(merged)
                if (syncEngine.pushNowOrEnqueue(householdId, weekKey, dataKind, payload)) {
                    localStore.markSynced(dataKind, payload)
                }
            }
        } else {
            syncEngine.discardPending(householdId, weekKey, dataKind)
        }
        return GroceryChange(before = before, after = merged)
    }

    override suspend fun saveGroceryItems(items: List<GroceryItem>) {
        localStore.saveGroceryItems(items)
        if (remoteEnabled) pushList("grocery", items)
    }

    override suspend fun saveAiGroceryItems(items: List<GroceryItem>) {
        localStore.saveAiGroceryItems(items)
        if (remoteEnabled) pushList("ai_grocery", items)
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

    private suspend fun pushList(kind: String, items: List<GroceryItem>) {
        mutex.withLock {
            val payload = localStore.encodeGrocery(items)
            if (syncEngine.pushNowOrEnqueue(householdId, weekKey, kind, payload)) {
                localStore.markSynced(kind, payload)
            }
        }
    }

    private fun currentItems(dataKind: String): List<GroceryItem> = when (dataKind) {
        "grocery" -> localStore.currentGroceryItems()
        else -> localStore.currentAiGroceryItems()
    }

    private fun saveLocalItems(dataKind: String, items: List<GroceryItem>) {
        when (dataKind) {
            "grocery" -> localStore.saveGroceryItems(items)
            else -> localStore.saveAiGroceryItems(items)
        }
    }
}
