package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.GroceryMerge
import app.mymultiverse.ammo.domain.repository.NutritionRepository
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
        val pulled = syncEngine.pullRemote(householdId, weekKey) { applyRemoteWeekData(it) }
        if (pulled) {
            syncEngine.flushPending(householdId, weekKey)
        }
    }

    fun currentGroceryItems(): List<GroceryItem> = localStore.currentGroceryItems()

    fun currentMealPlan(): WeeklyMealPlan = localStore.currentMealPlan()

    suspend fun applyRemoteWeekData(row: NutritionWeekDataRow) {
        if (row.householdId != householdId || row.weekKey != weekKey) return
        when (row.dataKind) {
            "meal_plan" -> {
                // EXACTLY the old behaviour: skip when local edits are pending
                if (syncEngine.hasPending(householdId, weekKey, row.dataKind)) return
                localStore.applyPayload(row.dataKind, row.payload)
            }
            "grocery", "ai_grocery" -> {
                val remoteItems = NutritionStorageCodec.decodeGrocery(row.payload)
                val localItems = when (row.dataKind) {
                    "grocery" -> localStore.currentGroceryItems()
                    else -> localStore.currentAiGroceryItems()
                }
                val pending = syncEngine.hasPending(householdId, weekKey, row.dataKind)
                val basePayload = localStore.lastSyncedPayload(row.dataKind)
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
                when (row.dataKind) {
                    "grocery" -> localStore.saveGroceryItems(merged)
                    else -> localStore.saveAiGroceryItems(merged)
                }
                localStore.markSynced(row.dataKind, row.payload)
                if (merged != remoteItems) {
                    if (remoteEnabled) {
                        val payload = localStore.encodeGrocery(merged)
                        syncEngine.pushNowOrEnqueue(householdId, weekKey, row.dataKind, payload)
                        if (!syncEngine.hasPending(householdId, weekKey, row.dataKind)) {
                            localStore.markSynced(row.dataKind, payload)
                        }
                    }
                } else {
                    syncEngine.discardPending(householdId, weekKey, row.dataKind)
                }
            }
        }
    }

    override suspend fun saveGroceryItems(items: List<GroceryItem>) {
        localStore.saveGroceryItems(items)
        if (remoteEnabled) {
            val payload = localStore.encodeGrocery(items)
            syncEngine.pushNowOrEnqueue(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "grocery",
                payload = payload,
            )
            if (!syncEngine.hasPending(householdId, weekKey, "grocery")) {
                localStore.markSynced("grocery", payload)
            }
        }
    }

    override suspend fun saveAiGroceryItems(items: List<GroceryItem>) {
        localStore.saveAiGroceryItems(items)
        if (remoteEnabled) {
            val payload = localStore.encodeGrocery(items)
            syncEngine.pushNowOrEnqueue(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "ai_grocery",
                payload = payload,
            )
            if (!syncEngine.hasPending(householdId, weekKey, "ai_grocery")) {
                localStore.markSynced("ai_grocery", payload)
            }
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
