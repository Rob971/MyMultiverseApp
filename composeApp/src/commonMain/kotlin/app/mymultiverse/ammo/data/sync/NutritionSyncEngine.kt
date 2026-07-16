package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.local.nutrition.PendingNutritionPush
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
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
        _status.value = NutritionSyncStatus.Syncing
        val rows = try {
            api.fetchWeek(householdId, weekKey)
        } catch (error: Exception) {
            logger.recordError(
                tag = TAG,
                message = "pull_remote_failed",
                throwable = error,
                context = syncContext(householdId, weekKey, "pull"),
            )
            markRemoteFailure(householdId, weekKey)
            return
        }
        rows.latestByDataKind().forEach(applyRow)
        refreshStatus(householdId, weekKey)
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
        try {
            api.upsert(householdId, weekKey, dataKind, payload)
            outbox.removeFor(householdId, weekKey, dataKind)
            refreshStatus(householdId, weekKey)
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
            refreshStatus(householdId, weekKey)
        }
    }

    suspend fun flushPending(householdId: String, weekKey: String) {
        val api = remote ?: run {
            _status.value = NutritionSyncStatus.RemoteUnavailable
            return
        }
        val pending = outbox.pendingFor(householdId, weekKey)
        if (pending.isEmpty()) {
            refreshStatus(householdId, weekKey)
            return
        }
        _status.value = NutritionSyncStatus.Syncing
        for (item in pending) {
            try {
                api.upsert(item.householdId, item.weekKey, item.dataKind, item.payload)
                outbox.remove(item)
            } catch (error: Exception) {
                logger.recordError(
                    tag = TAG,
                    message = "flush_pending_failed",
                    throwable = error,
                    context = syncContext(householdId, weekKey, "flush", item.dataKind),
                )
                refreshStatus(householdId, weekKey)
                return
            }
        }
        refreshStatus(householdId, weekKey)
    }

    fun markIdle() {
        _status.value = NutritionSyncStatus.Idle
    }

    fun markRemoteUnavailable() {
        _status.value = NutritionSyncStatus.RemoteUnavailable
    }

    /**
     * Returns true when the outbox holds at least one unsent entry for [dataKind] in the
     * given household/week.  Used by [OfflineFirstNutritionRepository.applyRemoteWeekData]
     * to skip a realtime overwrite while the user has pending local edits.
     */
    fun hasPending(householdId: String, weekKey: String, dataKind: String): Boolean =
        outbox.pendingFor(householdId, weekKey).any { it.dataKind == dataKind }

    private fun refreshStatus(householdId: String, weekKey: String) {
        val pendingCount = outbox.pendingFor(householdId, weekKey).size
        _status.value = when {
            remote == null -> NutritionSyncStatus.RemoteUnavailable
            pendingCount > 0 -> NutritionSyncStatus.PendingPush(pendingCount)
            else -> NutritionSyncStatus.Idle
        }
    }

    private fun markRemoteFailure(householdId: String, weekKey: String) {
        val pendingCount = outbox.pendingFor(householdId, weekKey).size
        _status.value = if (pendingCount > 0) {
            NutritionSyncStatus.PendingPush(pendingCount)
        } else {
            NutritionSyncStatus.RemoteUnavailable
        }
    }

    private fun List<NutritionWeekDataRow>.latestByDataKind(): List<NutritionWeekDataRow> =
        groupBy { it.dataKind }
            .values
            .mapNotNull { rows ->
                rows.maxWithOrNull(
                    compareBy<NutritionWeekDataRow> { it.updatedAtEpochMilliseconds() },
                )
            }

    private fun NutritionWeekDataRow.updatedAtEpochMilliseconds(): Long =
        updatedAt
            ?.let { raw -> runCatching { Instant.parse(raw).toEpochMilliseconds() }.getOrNull() }
            ?: Long.MIN_VALUE

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

    private companion object {
        const val TAG = "NutritionSyncEngine"
    }
}
