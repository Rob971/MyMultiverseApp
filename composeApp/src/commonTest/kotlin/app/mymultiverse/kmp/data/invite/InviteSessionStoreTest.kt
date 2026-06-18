package app.mymultiverse.kmp.data.invite

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InviteSessionStoreTest {

    @Test
    fun pendingInviteToken_roundTripsThroughSettings() {
        val store = InviteSessionStore(MapSettings())

        assertNull(store.getPendingInviteToken())

        store.setPendingInviteToken("invite-token-abc")
        assertEquals("invite-token-abc", store.getPendingInviteToken())

        store.clearPendingInviteToken()
        assertNull(store.getPendingInviteToken())
    }

    @Test
    fun pendingInviteToken_treatsBlankAsAbsent() {
        val store = InviteSessionStore(MapSettings())

        store.setPendingInviteToken("   ")
        assertNull(store.getPendingInviteToken())
    }
}
