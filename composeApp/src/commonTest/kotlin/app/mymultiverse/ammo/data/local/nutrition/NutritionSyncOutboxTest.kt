package app.mymultiverse.ammo.data.local.nutrition

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NutritionSyncOutboxTest {

    @Test
    fun enqueue_replacesSameKindForHouseholdAndWeek() {
        val outbox = NutritionSyncOutbox(MapSettings())
        val first = PendingNutritionPush("s1", "2025-W24", "grocery", "a", 1L)
        val second = PendingNutritionPush("s1", "2025-W24", "grocery", "b", 2L)

        outbox.enqueue(first)
        outbox.enqueue(second)

        assertEquals(1, outbox.peekAll().size)
        assertEquals("b", outbox.peekAll().single().payload)
    }

    @Test
    fun remove_clearsMatchingItem() {
        val outbox = NutritionSyncOutbox(MapSettings())
        val item = PendingNutritionPush("s1", "2025-W24", "meal_plan", "plan", 1L)
        outbox.enqueue(item)

        outbox.remove(item)

        assertTrue(outbox.peekAll().isEmpty())
    }

    @Test
    fun removeFor_clearsOnlyMatchingHouseholdWeekAndKind() {
        val outbox = NutritionSyncOutbox(MapSettings())
        outbox.enqueue(PendingNutritionPush("s1", "2025-W24", "grocery", "old", 1L))
        outbox.enqueue(PendingNutritionPush("s1", "2025-W24", "meal_plan", "plan", 2L))
        outbox.enqueue(PendingNutritionPush("s2", "2025-W24", "grocery", "other", 3L))

        outbox.removeFor("s1", "2025-W24", "grocery")

        assertEquals(
            listOf("plan", "other"),
            outbox.peekAll().map { it.payload },
        )
    }

    @Test
    fun pendingForHousehold_returnsAllWeekKeysForHousehold() {
        val outbox = NutritionSyncOutbox(MapSettings())
        outbox.enqueue(PendingNutritionPush("s1", "2025-W24", "grocery", "a", 1L))
        outbox.enqueue(PendingNutritionPush("s1", "2025-W25", "meal_plan", "b", 2L))
        outbox.enqueue(PendingNutritionPush("s2", "2025-W24", "grocery", "c", 3L))

        assertEquals(2, outbox.pendingForHousehold("s1").size)
        assertEquals(listOf("2025-W24", "2025-W25"), outbox.pendingForHousehold("s1").map { it.weekKey }.sorted())
    }

    @Test
    fun corruptedOutbox_doesNotAllowRemoveToWipeUnknownEntries() {
        val settings = MapSettings()
        settings.putString(NutritionStorageKeys.SYNC_OUTBOX, "{not-json")
        val outbox = NutritionSyncOutbox(settings)
        val item = PendingNutritionPush("s1", "2025-W24", "grocery", "new", 1L)

        assertTrue(outbox.isCorrupted())
        outbox.remove(PendingNutritionPush("s1", "2025-W24", "grocery", "ghost", 9L))
        outbox.enqueue(item)

        assertEquals(listOf("new"), outbox.peekAll().map { it.payload })
        assertFalse(outbox.isCorrupted())
    }
}
