package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.local.nutrition.PendingNutritionPush
import app.mymultiverse.ammo.data.remote.nutrition.PushResult
import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.GroceryMerge
import app.mymultiverse.ammo.domain.nutrition.MealPlanMerge
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivity
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivityDetector
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivityKind
import app.mymultiverse.ammo.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class GroceryChange(
    val before: List<GroceryItem>,
    val after: List<GroceryItem>,
    val actorUserId: String? = null,
)

/** Local-first repository: writes are immediate, pushes go through CAS — no kind is flushed blind. */
class OfflineFirstNutritionRepository(
    private val localStore: NutritionLocalStore,
    private val syncEngine: NutritionSyncEngine,
    override val householdId: String,
    override val weekKey: String,
    private val remoteEnabled: Boolean,
    /**
     * Called for each collaboration activity detected during `refreshFromRemote`.
     * The coordinator uses this to emit or stash activities regardless of which
     * caller invoked the refresh.
     */
    private val onCollaborationActivity: ((NutritionCollaborationActivity) -> Unit)? = null,
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
        // Reminders only make sense relative to a snapshot this device actually
        // held: on a first sync the local "before" is empty, so announcing anything
        // there would describe the whole list, not a partner's edit.
        val hadGroceryBaseBeforePull = localStore.lastSyncedPayload("grocery") != null
        val pulledChanges = mutableListOf<GroceryChange>()
        val pulled = syncEngine.pullRemote(householdId, weekKey) { row ->
            applyRemoteWeekData(row)?.let { pulledChanges += it }
        }
        if (pulled) {
            mutex.withLock {
                // After pullRemote the merge has already incorporated local offline
                // changes into the local state. For rows that exist on the server,
                // push the CURRENT local state (merged result) through CAS to keep
                // the server in sync; remove the outbox entry only on CAS success.
                // For rows not on the server, CAS insert the pending payload.
                // No kind is flushed blind: every write goes through pushCAS.
                syncEngine.peekPending(householdId, weekKey).forEach { item ->
                    val remoteRow = syncEngine.fetchRow(householdId, weekKey, item.dataKind)
                    when (item.dataKind) {
                        "grocery", "ai_grocery" -> pushPending(item, remoteRow, groceryAdapter(item.dataKind))
                        "meal_plan" -> pushPending(item, remoteRow, mealPlanAdapter())
                    }
                }
            }
        }
        if (!hadGroceryBaseBeforePull) return
        pulledChanges.flatMap { change ->
            NutritionCollaborationActivityDetector.detectGroceryChanges(
                before = change.before,
                after = change.after,
                actorUserId = change.actorUserId,
            ).filter { it.kind == NutritionCollaborationActivityKind.GroceryRemoved }
        }.forEach { activity ->
            onCollaborationActivity?.invoke(activity)
        }
    }

    fun currentGroceryItems(): List<GroceryItem> = localStore.currentGroceryItems()

    fun currentMealPlan(): WeeklyMealPlan = localStore.currentMealPlan()

    suspend fun applyRemoteWeekData(row: NutritionWeekDataRow): GroceryChange? {
        if (row.householdId != householdId || row.weekKey != weekKey) return null
        if (row.dataKind == "meal_plan") {
            mutex.withLock { applyMealPlanRemoteLocked(row) }
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
                pushWithCASLoop(dataKind, merged, groceryAdapter(dataKind))
            }
        } else {
            syncEngine.discardPending(householdId, weekKey, dataKind)
        }
        return GroceryChange(before = before, after = merged, actorUserId = row.updatedBy)
    }

    private suspend fun applyMealPlanRemoteLocked(row: NutritionWeekDataRow) {
        val remotePlan = NutritionStorageCodec.decodeMealPlan(weekKey, row.payload)
        val localPlan = localStore.currentMealPlan()
        val pending = syncEngine.hasPending(householdId, weekKey, row.dataKind)
        val basePayload = localStore.lastSyncedPayload(row.dataKind)
        val merged = if (basePayload == null && !pending) {
            remotePlan
        } else {
            MealPlanMerge.merge(
                base = basePayload?.let { NutritionStorageCodec.decodeMealPlan(weekKey, it) },
                local = localPlan,
                remote = remotePlan,
            )
        }
        localStore.saveMealPlan(merged)
        localStore.markSynced(row.dataKind, row.payload, row.version)
        if (merged != remotePlan) {
            if (remoteEnabled) {
                pushWithCASLoop(row.dataKind, merged, mealPlanAdapter())
            }
        } else {
            syncEngine.discardPending(householdId, weekKey, row.dataKind)
        }
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
            mutex.withLock {
                pushWithCASLoop("meal_plan", plan, mealPlanAdapter())
            }
        }
    }

    private suspend fun pushList(kind: String, items: List<GroceryItem>) {
        mutex.withLock {
            pushWithCASLoop(kind, items, groceryAdapter(kind))
        }
    }

    /**
     * Flushes a pending outbox item through the CAS+merge loop.
     * Called by [refreshFromRemote] for rows not present on the server.
     */
    private suspend fun <T> flushPendingCAS(kind: String, payload: String, adapter: CasAdapter<T>) {
        pushWithCASLoop(kind, adapter.decode(payload), adapter)
    }

    /**
     * Compare-and-swap write loop with bounded retry.
     *
     * Tries up to 3 pushes with the stored version. On Conflict, fetches the
     * current server state, 3-way merges through [adapter], updates local store,
     * and retries. After 3 attempts the payload is enqueued to the outbox as
     * pending. The base (payload + version) advances only on a confirmed server
     * success — never on a failed or merely-enqueued push.
     */
    private suspend fun <T> pushWithCASLoop(
        kind: String,
        initial: T,
        adapter: CasAdapter<T>,
    ) {
        var version = localStore.lastSyncedVersion(kind)
        var current = initial
        var currentPayload = adapter.encode(current)
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
            val remote = adapter.decode(remoteRow.payload)
            val local = adapter.currentValue()
            val merged = adapter.merge(adapter.lastSyncedBase(), local, remote)
            adapter.saveLocal(merged)
            current = merged
            currentPayload = adapter.encode(merged)
            val remoteVersion = remoteRow.version
            if (remoteVersion == null) {
                // Capability fallback: the server has no version column yet (migration not
                // deployed). There is nothing to compare against, so use the legacy blind
                // upsert, but of the merged union: a partner's content already in hand must
                // not be overwritten.
                if (syncEngine.pushNowOrEnqueue(householdId, weekKey, kind, currentPayload)) {
                    localStore.markSynced(kind, currentPayload)
                }
                return
            }
            version = remoteVersion
        }
        // Bounded retry exhausted — enqueue and leave pending
        syncEngine.enqueuePending(householdId, weekKey, kind, currentPayload)
    }

    /**
     * Per-kind view over payloads for the generic [pushWithCASLoop]: how to read
     * the current local value, encode/decode the wire payload, persist a merged
     * value, and resolve a 3-way conflict.
     */
    private interface CasAdapter<T> {
        fun currentValue(): T
        fun decode(payload: String): T
        fun encode(value: T): String
        fun lastSyncedBase(): T?
        fun saveLocal(value: T)
        fun merge(base: T?, local: T, remote: T): T
    }

    /**
     * Pushes one pending entry through the CAS+merge loop: rows already on the
     * server push the CURRENT local state (which the pull merge already updated);
     * rows not on the server CAS-insert the pending payload.
     */
    private suspend fun <T> pushPending(
        item: PendingNutritionPush,
        remoteRow: NutritionWeekDataRow?,
        adapter: CasAdapter<T>,
    ) {
        if (remoteRow == null) {
            flushPendingCAS(item.dataKind, item.payload, adapter)
        } else {
            pushWithCASLoop(item.dataKind, adapter.currentValue(), adapter)
        }
    }

    private fun groceryAdapter(kind: String) = object : CasAdapter<List<GroceryItem>> {
        override fun currentValue(): List<GroceryItem> = currentItems(kind)

        override fun decode(payload: String): List<GroceryItem> = NutritionStorageCodec.decodeGrocery(payload)

        override fun encode(value: List<GroceryItem>): String = localStore.encodeGrocery(value)

        override fun lastSyncedBase(): List<GroceryItem>? =
            localStore.lastSyncedPayload(kind)?.let(NutritionStorageCodec::decodeGrocery)

        override fun saveLocal(value: List<GroceryItem>) = saveLocalItems(kind, value)

        override fun merge(
            base: List<GroceryItem>?,
            local: List<GroceryItem>,
            remote: List<GroceryItem>,
        ): List<GroceryItem> = GroceryMerge.merge(base ?: emptyList(), local, remote)
    }

    private fun mealPlanAdapter() = object : CasAdapter<WeeklyMealPlan> {
        override fun currentValue(): WeeklyMealPlan = localStore.currentMealPlan()

        override fun decode(payload: String): WeeklyMealPlan = NutritionStorageCodec.decodeMealPlan(weekKey, payload)

        override fun encode(value: WeeklyMealPlan): String = localStore.encodeMealPlan(value)

        override fun lastSyncedBase(): WeeklyMealPlan? =
            localStore.lastSyncedPayload("meal_plan")?.let { NutritionStorageCodec.decodeMealPlan(weekKey, it) }

        override fun saveLocal(value: WeeklyMealPlan) = localStore.saveMealPlan(value)

        override fun merge(
            base: WeeklyMealPlan?,
            local: WeeklyMealPlan,
            remote: WeeklyMealPlan,
        ): WeeklyMealPlan = MealPlanMerge.merge(base, local, remote)
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