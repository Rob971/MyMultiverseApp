package app.mymultiverse.ammo.data.home

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeWeekPlanNudgeStoreTest {

    @Test
    fun dismissedWeekKey_isScopedToHouseholdAndCanBeCleared() {
        val store = HomeWeekPlanNudgeStore(MapSettings())

        assertNull(store.dismissedWeekKey("household-a"))

        store.setDismissed("household-a", "2026-W40")

        assertEquals("2026-W40", store.dismissedWeekKey("household-a"))
        assertNull(store.dismissedWeekKey("household-b"))

        store.clearDismissed("household-a")

        assertNull(store.dismissedWeekKey("household-a"))
    }
}