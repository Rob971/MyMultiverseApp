package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Subscribes to [nutrition_household_week_data] changes for a single household/week and
 * forwards remote edits to the active [OfflineFirstNutritionRepository].
 */
class NutritionHouseholdRealtimeSync(
    private val client: SupabaseClient,
    private val scope: CoroutineScope,
) {
    private var subscriptionJob: Job? = null

    fun start(
        householdId: String,
        weekKey: String,
        onUpdate: suspend (NutritionWeekDataRow) -> Unit,
    ) {
        stop()
        subscriptionJob = scope.launch {
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
                collector.join()
            } finally {
                channel.unsubscribe()
            }
        }
    }

    fun stop() {
        subscriptionJob?.cancel()
        subscriptionJob = null
    }
}

private fun JsonObject.decodeNutritionWeekDataRow(): NutritionWeekDataRow? =
    runCatching {
        Json.decodeFromJsonElement(NutritionWeekDataRow.serializer(), this)
    }.getOrNull()
