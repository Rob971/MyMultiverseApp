package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.remote.nutrition.PushResult
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
                // After pullRemote the merge has already incorporated local
                // offline changes into the local list. For rows that exist on
                // the server, push the CURRENT local items (merged result)
                // through CAS to keep the server in sync. Remove the outbox
                // entry only on CAS success. For rows not on the server, CAS
                // insert the pending payload.
                syncEngine.peekPending(householdId, weekKey).forEach { item ->
                    if (item.dataKind == "grocery" || item.dataKind == "ai_grocery") {
                        val remoteRow = syncEngine.fetchRow(householdId, weekKey, item.dataKind)
                        if (remoteRow == null) {
                            flushPendingCAS(item.dataKind, item.payload)
                        } else {
                            pushWithCASLoop(item.dataKind, currentItems(item.dataKind))
                        }
                    }
                }
                // Remaining pending items (meal_plan) use the legacy blind flush
                syncEngine.flushPending(householdId, weekKey)
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
        localStore.markSynced(dataKind, row.payload, row.version)
        if (merged != remoteItems) {
            if (remoteEnabled) {
                pushWithCASLoop(dataKind, merged)
            }
        } else {
            syncEngine.discardPending(householdId, weekKey, dataKind)
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
            pushWithCASLoop(kind, items)
        }
    }

    /**
     * Flushes a pending outbox item through the CAS+merge loop.
     * Called by [refreshFromRemote] for grocery/ai_grocery items.
     */
    private suspend fun flushPendingCAS(kind: String, payload: String) {
        val items = NutritionStorageCodec.decodeGrocery(payload)
        pushWithCASLoop(kind, items)
    }

    /**
     * Compare-and-swap write loop with bounded retry.
     *
     * Tries up to 3 pushes with the stored version. On Conflict, fetches the
     * current server state, 3-way merges, updates local store, and retries.
     * After 3 attempts the payload is enqueued to the outbox as pending.
     */
    private suspend fun pushWithCASLoop(kind: String, items: List<GroceryItem>) {
        var version = localStore.lastSyncedVersion(kind)
        var currentPayload = localStore.encodeGrocery(items)
        for (attempt in 1..3) {
            val result = syncEngine.pushCAS(
                householdId, weekKey, kind, currentPayload, version,
            )
            if (result.ok) {
                localStore.markSynced(kind, currentPayload, result.newVersion)
                return
            }
            if (result.failed) {
                // Network/transient error — enqueue and stop immediately
                syncEngine.enqueuePending(householdId, weekKey, kind, currentPayload)
                return
            }
            // Conflict: fetch current server state, merge, retry
            val remoteRow = syncEngine.fetchRow(householdId, weekKey, kind) ?: break
            // Capability fallback: if the server row has no version column
            // (migration not deployed), use a legacy blind upsert.
            if (remoteRow.version == null) {
                if (syncEngine.pushNowOrEnqueue(householdId, weekKey, kind, currentPayload)) {
                    localStore.markSynced(kind, currentPayload)
                }
                return
            }
            val remoteItems = NutritionStorageCodec.decodeGrocery(remoteRow.payload)
            val localItems = currentItems(kind)
            val basePayload = localStore.lastSyncedPayload(kind)
            val baseItems = if (basePayload != null) {
                NutritionStorageCodec.decodeGrocery(basePayload)
            } else {
                emptyList()
            }
            val merged = GroceryMerge.merge(baseItems, localItems, remoteItems)
            saveLocalItems(kind, merged)
            currentPayload = localStore.encodeGrocery(merged)
            version = remoteRow.version
        }
        // Bounded retry exhausted — enqueue and leave pending
        syncEngine.enqueuePending(householdId, weekKey, kind, currentPayload)
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
