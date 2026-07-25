package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.math.min

/**
 * Subscribes to [nutrition_household_week_data] changes for a single household/week and
 * forwards remote edits to the active [OfflineFirstNutritionRepository].
 *
 * Reconnects with exponential backoff and triggers [onReconnect] after a drop so the
 * active repository can pull a full catch-up snapshot.
 */
class NutritionHouseholdRealtimeSync(
    private val client: SupabaseClient,
    private val scope: CoroutineScope,
    private val logger: AppLogger,
) {
    private var subscriptionJob: Job? = null

    fun start(
        householdId: String,
        weekKey: String,
        onUpdate: suspend (NutritionWeekDataRow) -> Unit,
        onReconnect: suspend () -> Unit = {},
    ) {
        stop()
        subscriptionJob = scope.launch {
            var reconnectDelayMs = RECONNECT_INITIAL_MS
            var hasConnectedBefore = false
            while (isActive) {
                val channel = client.channel("nutrition-$householdId-$weekKey")
                try {
                    val collector = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                        table = "nutrition_household_week_data"
                        filter("household_id", FilterOperator.EQ, householdId)
                        filter("week_key", FilterOperator.EQ, weekKey)
                    }.onEach { action ->
                        val record = when (action) {
                            is PostgresAction.Delete -> return@onEach
                            is PostgresAction.Insert -> action.record
                            is PostgresAction.Update -> action.record
                            is PostgresAction.Select -> action.record
                        }
                        val row = record.decodeNutritionWeekDataRow() ?: return@onEach
                        val currentUserId = client.auth.currentUserOrNull()?.id
                        if (row.updatedBy != null && row.updatedBy == currentUserId) return@onEach
                        onUpdate(row)
                    }.launchIn(this)

                    channel.subscribe()
                    reconnectDelayMs = RECONNECT_INITIAL_MS
                    if (hasConnectedBefore) {
                        logger.breadcrumb("sync_realtime_reconnected week_key=$weekKey")
                        onReconnect()
                    }
                    hasConnectedBefore = true
                    collector.join()
                } catch (e: CancellationException) {
                    throw e
                } catch (error: Exception) {
                    logger.recordError(
                        tag = TAG,
                        message = "realtime_subscription_failed",
                        throwable = error,
                        context = mapOf(
                            "household_id" to householdId,
                            "week_key" to weekKey,
                        ),
                    )
                } finally {
                    runCatching { channel.unsubscribe() }
                }
                if (!isActive) break
                logger.breadcrumb("sync_realtime_retry delay_ms=$reconnectDelayMs week_key=$weekKey")
                delay(reconnectDelayMs)
                reconnectDelayMs = min(reconnectDelayMs * 2, RECONNECT_MAX_MS)
            }
        }
    }

    fun stop() {
        subscriptionJob?.cancel()
        subscriptionJob = null
    }

    private companion object {
        const val TAG = "NutritionHouseholdRealtimeSync"
        const val RECONNECT_INITIAL_MS = 1_000L
        const val RECONNECT_MAX_MS = 30_000L
    }
}

private fun JsonObject.decodeNutritionWeekDataRow(): NutritionWeekDataRow? =
    runCatching {
        Json.decodeFromJsonElement(NutritionWeekDataRow.serializer(), this)
    }.getOrNull()
