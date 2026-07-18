package app.mymultiverse.ammo.data.tour

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductTourStoreTest {

    private fun store() = ProductTourStore(MapSettings())

    @Test
    fun hasSeenTour_returnsFalse_whenNeverMarked() {
        assertFalse(store().hasSeenTour("1.5.3"))
    }

    @Test
    fun hasSeenTour_returnsTrue_afterMarkTourSeen() {
        val store = store()
        store.markTourSeen("1.5.3")
        assertTrue(store.hasSeenTour("1.5.3"))
    }

    @Test
    fun hasSeenTour_isScopedToVersionKey_differentVersionNotSeen() {
        val store = store()
        store.markTourSeen("1.5.3")

        assertTrue(store.hasSeenTour("1.5.3"))
        assertFalse(store.hasSeenTour("1.6.0"))
    }

    @Test
    fun hasSeenTour_isScopedToVersionKey_multipleVersionsIndependent() {
        val store = store()
        store.markTourSeen("1.4.0")
        store.markTourSeen("1.5.3")

        assertTrue(store.hasSeenTour("1.4.0"))
        assertTrue(store.hasSeenTour("1.5.3"))
        assertFalse(store.hasSeenTour("1.6.0"))
    }

    @Test
    fun clearTourSeen_resetsSeenFlag() {
        val store = store()
        store.markTourSeen("1.5.3")
        assertTrue(store.hasSeenTour("1.5.3"))

        store.clearTourSeen("1.5.3")
        assertFalse(store.hasSeenTour("1.5.3"))
    }

    @Test
    fun clearTourSeen_doesNotAffectOtherVersions() {
        val store = store()
        store.markTourSeen("1.4.0")
        store.markTourSeen("1.5.3")

        store.clearTourSeen("1.4.0")

        assertFalse(store.hasSeenTour("1.4.0"))
        assertTrue(store.hasSeenTour("1.5.3"))
    }

    @Test
    fun markTourSeen_isIdempotent() {
        val store = store()
        store.markTourSeen("1.5.3")
        store.markTourSeen("1.5.3")

        assertTrue(store.hasSeenTour("1.5.3"))
    }
}
