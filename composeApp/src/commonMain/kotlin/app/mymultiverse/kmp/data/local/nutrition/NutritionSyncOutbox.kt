package app.mymultiverse.kmp.data.local.nutrition

import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PendingNutritionPush(
    val spaceId: String,
    val weekKey: String,
    val dataKind: String,
    val payload: String,
    val enqueuedAtEpochMs: Long,
)

/**
 * Durable queue of nutrition payloads that could not be pushed to Supabase yet.
 */
class NutritionSyncOutbox(
    private val settings: Settings,
) {
    fun enqueue(item: PendingNutritionPush) {
        val updated = peekAll()
            .filterNot {
                it.spaceId == item.spaceId &&
                    it.weekKey == item.weekKey &&
                    it.dataKind == item.dataKind
            } + item
        persist(updated)
    }

    fun peekAll(): List<PendingNutritionPush> {
        val raw = settings.getStringOrNull(NutritionStorageKeys.SYNC_OUTBOX) ?: return emptyList()
        return runCatching {
            Json.decodeFromString<List<PendingNutritionPush>>(raw)
        }.getOrDefault(emptyList())
    }

    fun pendingFor(spaceId: String, weekKey: String): List<PendingNutritionPush> =
        peekAll().filter { it.spaceId == spaceId && it.weekKey == weekKey }

    fun remove(item: PendingNutritionPush) {
        persist(peekAll().filterNot { it == item })
    }

    fun removeFor(spaceId: String, weekKey: String, dataKind: String) {
        persist(
            peekAll().filterNot {
                it.spaceId == spaceId &&
                    it.weekKey == weekKey &&
                    it.dataKind == dataKind
            },
        )
    }

    fun clear() {
        settings.remove(NutritionStorageKeys.SYNC_OUTBOX)
    }

    private fun persist(items: List<PendingNutritionPush>) {
        if (items.isEmpty()) {
            settings.remove(NutritionStorageKeys.SYNC_OUTBOX)
        } else {
            settings.putString(NutritionStorageKeys.SYNC_OUTBOX, Json.encodeToString(items))
        }
    }
}
