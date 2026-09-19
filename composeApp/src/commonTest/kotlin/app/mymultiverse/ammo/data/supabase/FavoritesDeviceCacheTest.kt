package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.model.nutrition.FavoriteDish
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FavoritesDeviceCacheTest {

    private fun authenticated(userId: String) = AuthState.Authenticated(
        AuthUser(id = userId, email = "$userId@example.com", displayName = "Test"),
    )

    private fun dish(label: String) = FavoriteDish(label = label, normalisedLabel = label.trim().lowercase())

    @Test
    fun accountSwitch_showsEachAccountsOwnCachedFavorites_acrossRestart() {
        val settings = MapSettings()
        // Prior sessions persisted each account's favorites under its own key.
        FavoritesDeviceCache(settings).apply {
            showAccount(authenticated("user-a"))
            commit("user-a") { listOf(dish("Pasta")) }
        }
        FavoritesDeviceCache(settings).apply {
            showAccount(authenticated("user-b"))
            commit("user-b") { listOf(dish("Pizza")) }
        }

        // A fresh cache ("restart"), signed in as B: must show B's list, never A's.
        val restart = FavoritesDeviceCache(settings)
        restart.showAccount(authenticated("user-b"))
        assertEquals(listOf("Pizza"), restart.favorites.value.map { it.label })

        // Switching to A in the same process must swap to A's list.
        restart.showAccount(authenticated("user-a"))
        assertEquals(listOf("Pasta"), restart.favorites.value.map { it.label })
    }

    @Test
    fun signOut_clearsFavorites() {
        val cache = FavoritesDeviceCache(MapSettings())
        cache.showAccount(authenticated("user-a"))
        cache.commit("user-a") { listOf(dish("Pasta")) }
        assertEquals(listOf("Pasta"), cache.favorites.value.map { it.label })

        cache.showAccount(AuthState.Unauthenticated)

        assertTrue(cache.favorites.value.isEmpty())
        assertNull(cache.currentAccountId)
    }

    @Test
    fun inFlightCommit_fromPreviousAccount_isDropped() {
        val settings = MapSettings()
        val cache = FavoritesDeviceCache(settings)
        cache.showAccount(authenticated("user-a"))

        // The account switches to B while A's write is still in flight.
        cache.showAccount(authenticated("user-b"))

        // A's late write must not overwrite B's (empty) list or persist under B's key.
        cache.commit("user-a") { listOf(dish("Pasta")) }

        assertTrue(cache.favorites.value.isEmpty(), "A's late write must not land in B's list")
        assertNull(settings.getStringOrNull(FavoritesDeviceCache.cacheKey("user-b")))
    }
}