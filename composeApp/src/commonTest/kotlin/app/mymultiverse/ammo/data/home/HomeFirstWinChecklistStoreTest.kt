package app.mymultiverse.ammo.data.home

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeFirstWinChecklistStoreTest {

    @Test
    fun dismissedState_isScopedToHouseholdAndCanBeCleared() {
        val store = HomeFirstWinChecklistStore(MapSettings())

        assertFalse(store.isDismissed("household-a"))

        store.setDismissed("household-a")

        assertTrue(store.isDismissed("household-a"))
        assertFalse(store.isDismissed("household-b"))

        store.clearDismissed("household-a")

        assertFalse(store.isDismissed("household-a"))
    }
}