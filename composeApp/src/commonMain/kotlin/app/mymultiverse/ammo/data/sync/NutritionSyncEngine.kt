package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.local.nutrition.PendingNutritionPush
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.remote.nutrition.PushResult
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
        applyRow: suspend (NutritionWeekDataRow) -> Unit,
    ): Boolean {
        val api = remote ?: run {
            _status.value = NutritionSyncStatus.RemoteUnavailable
            return false
        }
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
            markRemoteFailure(householdId, weekKey)
            return false
        }
        val fetched = rows.latestByDataKind()
        for (row in fetched) {
            applyRow(row)
        }
        logger.breadcrumb("sync_pull_ok rows=${fetched.size} week_key=$weekKey")
        refreshStatus(householdId, weekKey)
        return true
    }

    /**
     * Compare-and-swap push: updates the row only if the stored version matches.
     * Returns the server result. On success the outbox entry is removed.
     * On failure (Conflict or exception) the outbox is updated (existing entry
     * updated with the new payload, or a new entry enqueued).
     *
     * This is the single-attempt primitive. Callers implement the retry loop.
     */
    suspend fun pushCAS(
        householdId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
        expectedVersion: Long?,
    ): PushResult {
        val api = remote ?: run {
            _status.value = NutritionSyncStatus.RemoteUnavailable
            return PushResult.failed()
        }
        return try {
            val result = api.pushIfVersion(householdId, weekKey, dataKind, payload, expectedVersion)
            if (result.ok) {
                outbox.removeFor(householdId, weekKey, dataKind)
                logger.breadcrumb("sync_push_cas_ok kind=$dataKind week_key=$weekKey ver=$expectedVersion")
                refreshStatus(householdId, weekKey)
            } else {
                logger.breadcrumb("sync_push_cas_conflict kind=$dataKind week_key=$weekKey ver=$expectedVersion")
            }
            result
        } catch (e: CancellationException) {
            throw e
        } catch (error: Exception) {
            logger.recordError(
                tag = TAG,
                message = "push_cas_failed_enqueue",
                throwable = error,
                context = syncContext(householdId, weekKey, "push_cas", dataKind),
            )
            enqueue(householdId, weekKey, dataKind, payload)
            refreshStatus(householdId, weekKey)
            PushResult.failed()
        }
    }

    private suspend fun enqueue(householdId: String, weekKey: String, dataKind: String, payload: String) {
        outbox.enqueue(
            PendingNutritionPush(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = dataKind,
                payload = payload,
                enqueuedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            ),
        )
    }

    suspend fun pushNowOrEnqueue(
        householdId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
    ): Boolean {
        val api = remote ?: run {
            _status.value = NutritionSyncStatus.RemoteUnavailable
            return false
        }
        try {
            api.upsert(householdId, weekKey, dataKind, payload)
            outbox.removeFor(householdId, weekKey, dataKind)
            logger.breadcrumb("sync_push_ok kind=$dataKind week_key=$weekKey")
            refreshStatus(householdId, weekKey)
            return true
        } catch (e: CancellationException) {
            throw e
        } catch (error: Exception) {
            logger.recordError(
                tag = TAG,
                message = "push_failed_enqueue",
                throwable = error,
                context = syncContext(householdId, weekKey, "push", dataKind),
            )
            enqueue(householdId, weekKey, dataKind, payload)
            refreshStatus(householdId, weekKey)
            return false
        }
    }

    /**
     * Returns all pending outbox items without processing them.
     * Used by callers that need to route items through different code paths.
     */
    fun peekPending(householdId: String, weekKey: String): List<PendingNutritionPush> =
        outbox.pendingFor(householdId, weekKey)

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
    /**
     * Fetches a single nutrition row from the remote by dataKind.
     * Returns null if the row does not exist on the server.
     */
    suspend fun fetchRow(
        householdId: String,
        weekKey: String,
        dataKind: String,
    ): NutritionWeekDataRow? {
        val api = remote ?: return null
        return try {
            api.fetchWeek(householdId, weekKey)
                .firstOrNull { it.dataKind == dataKind }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    suspend fun enqueuePending(
        householdId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
    ) {
        enqueue(householdId, weekKey, dataKind, payload)
        refreshStatus(householdId, weekKey)
    }

    fun hasPending(householdId: String, weekKey: String, dataKind: String): Boolean =
        outbox.pendingFor(householdId, weekKey).any { it.dataKind == dataKind }

    fun discardPending(householdId: String, weekKey: String, dataKind: String) {
        outbox.removeFor(householdId, weekKey, dataKind)
        refreshStatus(householdId, weekKey)
    }

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
