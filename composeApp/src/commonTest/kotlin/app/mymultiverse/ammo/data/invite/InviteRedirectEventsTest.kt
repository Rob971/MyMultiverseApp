package app.mymultiverse.ammo.data.invite

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InviteRedirectEventsTest {

    @BeforeTest
    fun clearPendingState() {
        InviteRedirectEvents.consumePending()
    }

    @Test
    fun emit_storesPendingUrlUntilConsumed() {
        val url = InviteRedirectUrls.build("pending-token")
        InviteRedirectEvents.emit(url)

        assertEquals(url, InviteRedirectEvents.consumePending())
        assertNull(InviteRedirectEvents.consumePending())
    }

    @Test
    fun emit_publishesOnFlow() = runTest {
        val url = InviteRedirectUrls.build("flow-token")
        val collected = async { InviteRedirectEvents.urls.first() }
        yield()

        InviteRedirectEvents.emit(url)

        assertEquals(url, collected.await())
    }

    @Test
    fun emit_ignoresNonInviteUrls() {
        InviteRedirectEvents.emit("app.mymultiverse.ammo://auth/callback")
        assertNull(InviteRedirectEvents.consumePending())
    }
}
