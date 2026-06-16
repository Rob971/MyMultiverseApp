package app.mymultiverse.kmp.data.sync

import app.mymultiverse.kmp.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.kmp.data.local.nutrition.PendingNutritionPush
import app.mymultiverse.kmp.data.observability.AppLogger
import app.mymultiverse.kmp.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
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
        spaceId: String,
        weekKey: String,
        applyRow: (NutritionWeekDataRow) -> Unit,
    ) {
        val api = remote ?: run {
            _status.value = NutritionSyncStatus.RemoteUnavailable
            return
        }
        _status.value = NutritionSyncStatus.Syncing
        val rows = try {
            api.fetchWeek(spaceId, weekKey)
        } catch (error: Exception) {
            logger.recordError(
                tag = TAG,
                message = "pull_remote_failed",
                throwable = error,
                context = syncContext(spaceId, weekKey, "pull"),
            )
            markRemoteFailure(spaceId, weekKey)
            return
        }
        rows.latestByDataKind().forEach(applyRow)
        refreshStatus(spaceId, weekKey)
    }

    suspend fun pushNowOrEnqueue(
        spaceId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
    ) {
        val api = remote ?: run {
            _status.value = NutritionSyncStatus.RemoteUnavailable
            return
        }
        try {
            api.upsert(spaceId, weekKey, dataKind, payload)
            outbox.removeFor(spaceId, weekKey, dataKind)
            refreshStatus(spaceId, weekKey)
        } catch (error: Exception) {
            logger.recordError(
                tag = TAG,
                message = "push_failed_enqueue",
                throwable = error,
                context = syncContext(spaceId, weekKey, "push", dataKind),
            )
            outbox.enqueue(
                PendingNutritionPush(
                    spaceId = spaceId,
                    weekKey = weekKey,
                    dataKind = dataKind,
                    payload = payload,
                    enqueuedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            refreshStatus(spaceId, weekKey)
        }
    }

    suspend fun flushPending(spaceId: String, weekKey: String) {
        val api = remote ?: run {
            _status.value = NutritionSyncStatus.RemoteUnavailable
            return
        }
        val pending = outbox.pendingFor(spaceId, weekKey)
        if (pending.isEmpty()) {
            refreshStatus(spaceId, weekKey)
            return
        }
        _status.value = NutritionSyncStatus.Syncing
        for (item in pending) {
            try {
                api.upsert(item.spaceId, item.weekKey, item.dataKind, item.payload)
                outbox.remove(item)
            } catch (error: Exception) {
                logger.recordError(
                    tag = TAG,
                    message = "flush_pending_failed",
                    throwable = error,
                    context = syncContext(spaceId, weekKey, "flush", item.dataKind),
                )
                refreshStatus(spaceId, weekKey)
                return
            }
        }
        refreshStatus(spaceId, weekKey)
    }

    fun markIdle() {
        _status.value = NutritionSyncStatus.Idle
    }

    fun markRemoteUnavailable() {
        _status.value = NutritionSyncStatus.RemoteUnavailable
    }

    private fun refreshStatus(spaceId: String, weekKey: String) {
        val pendingCount = outbox.pendingFor(spaceId, weekKey).size
        _status.value = when {
            remote == null -> NutritionSyncStatus.RemoteUnavailable
            pendingCount > 0 -> NutritionSyncStatus.PendingPush(pendingCount)
            else -> NutritionSyncStatus.Idle
        }
    }

    private fun markRemoteFailure(spaceId: String, weekKey: String) {
        val pendingCount = outbox.pendingFor(spaceId, weekKey).size
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
        spaceId: String,
        weekKey: String,
        operation: String,
        dataKind: String? = null,
    ): Map<String, String> = buildMap {
        put("operation", operation)
        put("space_id", spaceId)
        put("week_key", weekKey)
        dataKind?.let { put("data_kind", it) }
    }

    private companion object {
        const val TAG = "NutritionSyncEngine"
    }
}
