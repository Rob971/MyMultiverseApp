package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.local.nutrition.PendingNutritionPush
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Coordinates pull from Supabase, push with offline outbox, and exposes sync status.
 */
class NutritionSyncEngine(
    private val remote: NutritionRemoteDataSource?,
    private val outbox: NutritionSyncOutbox,
    private val logger: AppLogger,
) {
    private val _status = MutableStateFlow<NutritionSyncStatus>(NutritionSyncStatus.Idle)

    fun observeStatus(): Flow<NutritionSyncStatus> = _status.asStateFlow()

    suspend fun pullRemote(
        householdId: String,
        weekKey: String,
        applyRow: (NutritionWeekDataRow) -> Unit,
    ) {
        val api = remote ?: run {
            _status.value = NutritionSyncStatus.RemoteUnavailable
            return
        }
        logOutboxCorruptionIfNeeded(householdId, weekKey, "pull")
        _status.value = NutritionSyncStatus.Syncing
        val rows = try {
            api.fetchWeek(householdId, weekKey)
        } catch (e: CancellationException) {
            throw e
        } catch (error: Exception) {
            logger.recordError(
                tag = TAG,
                message = "pull_remote_failed",
                throwable = error,
                context = syncContext(householdId, weekKey, "pull"),
            )
            markRemoteFailure(householdId)
            return
        }
        val fetched = rows.latestByDataKind()
        fetched.forEach(applyRow)
        logger.breadcrumb("sync_pull_ok rows=${fetched.size} week_key=$weekKey")
        refreshStatus(householdId)
    }

    suspend fun pushNowOrEnqueue(
        householdId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
    ) {
        val api = remote ?: run {
            _status.value = NutritionSyncStatus.RemoteUnavailable
            return
        }
        logOutboxCorruptionIfNeeded(householdId, weekKey, "push", dataKind)
        try {
            api.upsert(householdId, weekKey, dataKind, payload)
            outbox.removeFor(householdId, weekKey, dataKind)
            logger.breadcrumb("sync_push_ok kind=$dataKind week_key=$weekKey")
            refreshStatus(householdId)
        } catch (e: CancellationException) {
            throw e
        } catch (error: Exception) {
            logger.recordError(
                tag = TAG,
                message = "push_failed_enqueue",
                throwable = error,
                context = syncContext(householdId, weekKey, "push", dataKind),
            )
            outbox.enqueue(
                PendingNutritionPush(
                    householdId = householdId,
                    weekKey = weekKey,
                    dataKind = dataKind,
                    payload = payload,
                    enqueuedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            refreshStatus(householdId)
        }
    }

    suspend fun flushPending(householdId: String, weekKey: String): FlushResult {
        val api = remote ?: run {
            _status.value = NutritionSyncStatus.RemoteUnavailable
            return FlushResult()
        }
        logOutboxCorruptionIfNeeded(householdId, weekKey, "flush")
        val pending = outbox.pendingFor(householdId, weekKey)
        if (pending.isEmpty()) {
            refreshStatus(householdId)
            return FlushResult()
        }
        _status.value = NutritionSyncStatus.Syncing
        var flushed = 0
        var skippedStale = 0
        var failed = 0
        for (item in pending) {
            try {
                val remoteRows = api.fetchWeek(item.householdId, item.weekKey)
                val remoteRow = remoteRows.latestForDataKind(item.dataKind)
                if (isRemoteNewerThanPending(remoteRow, item)) {
                    outbox.remove(item)
                    skippedStale++
                    logger.breadcrumb(
                        "sync_flush_skip_stale kind=${item.dataKind} week_key=${item.weekKey}",
                    )
                    continue
                }
                api.upsert(item.householdId, item.weekKey, item.dataKind, item.payload)
                outbox.remove(item)
                flushed++
            } catch (e: CancellationException) {
                throw e
            } catch (error: Exception) {
                logger.recordError(
                    tag = TAG,
                    message = "flush_pending_failed",
                    throwable = error,
                    context = syncContext(householdId, weekKey, "flush", item.dataKind),
                )
                failed++
            }
        }
        if (flushed > 0 || skippedStale > 0) {
            logger.breadcrumb(
                "sync_flush_ok flushed=$flushed skipped_stale=$skippedStale failed=$failed week_key=$weekKey",
            )
        }
        refreshStatus(householdId)
        return FlushResult(flushed = flushed, skippedStale = skippedStale, failed = failed)
    }

    suspend fun flushAllPendingForHousehold(householdId: String): FlushResult {
        val weekKeys = outbox.pendingForHousehold(householdId).map { it.weekKey }.distinct()
        var total = FlushResult()
        for (weekKey in weekKeys) {
            total += flushPending(householdId, weekKey)
        }
        return total
    }

    fun markIdle() {
        _status.value = NutritionSyncStatus.Idle
    }

    fun markRemoteUnavailable() {
        _status.value = NutritionSyncStatus.RemoteUnavailable
    }

    /**
     * Returns true when the outbox holds at least one unsent entry for [dataKind] in the
     * given household/week. Used by [OfflineFirstNutritionRepository.applyRemoteWeekData]
     * to apply timestamp-aware conflict resolution while local edits are pending.
     */
    fun hasPending(householdId: String, weekKey: String, dataKind: String): Boolean =
        outbox.pendingFor(householdId, weekKey, dataKind) != null

    fun pendingFor(householdId: String, weekKey: String, dataKind: String): PendingNutritionPush? =
        outbox.pendingFor(householdId, weekKey, dataKind)

    fun dropPending(householdId: String, weekKey: String, dataKind: String) {
        outbox.removeFor(householdId, weekKey, dataKind)
        refreshStatus(householdId)
    }

    fun shouldApplyRemoteOverPending(
        row: NutritionWeekDataRow,
        pending: PendingNutritionPush,
    ): Boolean = row.updatedAtEpochMilliseconds() > pending.enqueuedAtEpochMs

    private fun refreshStatus(householdId: String) {
        val pendingCount = outbox.pendingForHousehold(householdId).size
        _status.value = when {
            remote == null -> NutritionSyncStatus.RemoteUnavailable
            pendingCount > 0 -> NutritionSyncStatus.PendingPush(pendingCount)
            else -> NutritionSyncStatus.Idle
        }
    }

    private fun markRemoteFailure(householdId: String) {
        val pendingCount = outbox.pendingForHousehold(householdId).size
        _status.value = if (pendingCount > 0) {
            NutritionSyncStatus.PendingPush(pendingCount)
        } else {
            NutritionSyncStatus.RemoteUnavailable
        }
    }

    private fun logOutboxCorruptionIfNeeded(
        householdId: String,
        weekKey: String,
        operation: String,
        dataKind: String? = null,
    ) {
        if (!outbox.isCorrupted()) return
        val error = outbox.corruptionError() ?: IllegalStateException("outbox_corrupted")
        logger.recordError(
            tag = TAG,
            message = "outbox_corrupted",
            throwable = error,
            context = syncContext(householdId, weekKey, operation, dataKind),
        )
    }

    private fun isRemoteNewerThanPending(
        remote: NutritionWeekDataRow?,
        pending: PendingNutritionPush,
    ): Boolean = remote != null && remote.updatedAtEpochMilliseconds() > pending.enqueuedAtEpochMs

    private fun List<NutritionWeekDataRow>.latestByDataKind(): List<NutritionWeekDataRow> =
        groupBy { it.dataKind }
            .values
            .mapNotNull { rows -> rows.maxWithOrNull(compareBy { it.updatedAtEpochMilliseconds() }) }

    private fun syncContext(
        householdId: String,
        weekKey: String,
        operation: String,
        dataKind: String? = null,
    ): Map<String, String> = buildMap {
        put("operation", operation)
        put("household_id", householdId)
        put("week_key", weekKey)
        dataKind?.let { put("data_kind", it) }
    }

    data class FlushResult(
        val flushed: Int = 0,
        val skippedStale: Int = 0,
        val failed: Int = 0,
    ) {
        operator fun plus(other: FlushResult): FlushResult =
            FlushResult(
                flushed = flushed + other.flushed,
                skippedStale = skippedStale + other.skippedStale,
                failed = failed + other.failed,
            )
    }

    private companion object {
        const val TAG = "NutritionSyncEngine"
    }
}
