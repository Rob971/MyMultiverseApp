package app.mymultiverse.ammo.data.local.nutrition

import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PendingNutritionPush(
    val householdId: String,
    val weekKey: String,
    val dataKind: String,
    val payload: String,
    val enqueuedAtEpochMs: Long,
)

internal sealed class OutboxReadResult {
    data class Ok(val items: List<PendingNutritionPush>) : OutboxReadResult()

    data class Corrupted(
        val raw: String,
        val error: Throwable,
    ) : OutboxReadResult()
}

/**
 * Durable queue of nutrition payloads that could not be pushed to Supabase yet.
 */
class NutritionSyncOutbox(
    private val settings: Settings,
) {
    fun enqueue(item: PendingNutritionPush) {
        when (val state = read()) {
            is OutboxReadResult.Ok -> {
                val updated = state.items.filterNot {
                    it.householdId == item.householdId &&
                        it.weekKey == item.weekKey &&
                        it.dataKind == item.dataKind
                } + item
                persist(updated)
            }
            is OutboxReadResult.Corrupted -> {
                // Cannot merge with corrupt state; keep the new edit without wiping storage silently.
                persist(listOf(item))
            }
        }
    }

    fun peekAll(): List<PendingNutritionPush> =
        when (val state = read()) {
            is OutboxReadResult.Ok -> state.items
            is OutboxReadResult.Corrupted -> emptyList()
        }

    fun isCorrupted(): Boolean = read() is OutboxReadResult.Corrupted

    fun corruptionError(): Throwable? =
        (read() as? OutboxReadResult.Corrupted)?.error

    fun pendingFor(householdId: String, weekKey: String): List<PendingNutritionPush> =
        peekAll().filter { it.householdId == householdId && it.weekKey == weekKey }

    fun pendingForHousehold(householdId: String): List<PendingNutritionPush> =
        peekAll().filter { it.householdId == householdId }

    fun pendingFor(
        householdId: String,
        weekKey: String,
        dataKind: String,
    ): PendingNutritionPush? =
        pendingFor(householdId, weekKey).firstOrNull { it.dataKind == dataKind }

    fun remove(item: PendingNutritionPush) {
        when (val state = read()) {
            is OutboxReadResult.Ok -> persist(state.items.filterNot { it == item })
            is OutboxReadResult.Corrupted -> Unit
        }
    }

    fun removeFor(householdId: String, weekKey: String, dataKind: String) {
        when (val state = read()) {
            is OutboxReadResult.Ok -> {
                persist(
                    state.items.filterNot {
                        it.householdId == householdId &&
                            it.weekKey == weekKey &&
                            it.dataKind == dataKind
                    },
                )
            }
            is OutboxReadResult.Corrupted -> Unit
        }
    }

    fun clear() {
        settings.remove(NutritionStorageKeys.SYNC_OUTBOX)
    }

    private fun read(): OutboxReadResult {
        val raw = settings.getStringOrNull(NutritionStorageKeys.SYNC_OUTBOX) ?: return OutboxReadResult.Ok(emptyList())
        return runCatching {
            OutboxReadResult.Ok(Json.decodeFromString<List<PendingNutritionPush>>(raw))
        }.getOrElse { error ->
            OutboxReadResult.Corrupted(raw = raw, error = error)
        }
    }

    private fun persist(items: List<PendingNutritionPush>) {
        if (items.isEmpty()) {
            settings.remove(NutritionStorageKeys.SYNC_OUTBOX)
        } else {
            settings.putString(NutritionStorageKeys.SYNC_OUTBOX, Json.encodeToString(items))
        }
    }
}
