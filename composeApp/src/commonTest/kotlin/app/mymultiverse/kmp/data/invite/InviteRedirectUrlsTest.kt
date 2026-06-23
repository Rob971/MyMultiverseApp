package app.mymultiverse.kmp.data.invite

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InviteRedirectUrlsTest {

    @Test
    fun build_producesInviteDeepLinkWithTokenQuery() {
        assertEquals(
            "app.mymultiverse.kmp://invite?token=abc-123",
            InviteRedirectUrls.build("abc-123"),
        )
    }

    @Test
    fun buildHttps_producesVerifiedAppLinkUrl() {
        assertEquals(
            "https://mymultiverse.app/invite?token=abc-123",
            InviteRedirectUrls.buildHttps("abc-123"),
        )
    }

    @Test
    fun parseToken_returnsTokenFromInviteUrl() {
        val url = InviteRedirectUrls.build("invite-token-uuid")
        assertEquals("invite-token-uuid", InviteRedirectUrls.parseToken(url))
    }

    @Test
    fun parseToken_returnsTokenFromHttpsInviteUrl() {
        val url = InviteRedirectUrls.buildHttps("invite-token-uuid")
        assertEquals("invite-token-uuid", InviteRedirectUrls.parseToken(url))
    }

    @Test
    fun parseToken_returnsTokenFromSupabaseInviteOpenUrl() {
        val url = "https://abc.supabase.co/functions/v1/invite-open?token=edge-token"
        assertEquals("edge-token", InviteRedirectUrls.parseToken(url))
    }

    @Test
    fun build_encodesTokenForDeepLink() {
        val url = InviteRedirectUrls.build("abc 123")
        assertEquals("app.mymultiverse.kmp://invite?token=abc%20123", url)
        assertEquals("abc 123", InviteRedirectUrls.parseToken(url))
    }

    @Test
    fun parseToken_returnsNullForNonInviteUrl() {
        assertNull(InviteRedirectUrls.parseToken("app.mymultiverse.kmp://auth"))
        assertNull(InviteRedirectUrls.parseToken("https://example.com/invite?token=abc"))
    }

    @Test
    fun parseToken_returnsNullWhenTokenMissing() {
        assertNull(InviteRedirectUrls.parseToken("app.mymultiverse.kmp://invite"))
        assertNull(InviteRedirectUrls.parseToken("app.mymultiverse.kmp://invite?other=value"))
        assertNull(InviteRedirectUrls.parseToken("https://mymultiverse.app/invite"))
    }

    @Test
    fun isInviteRedirect_matchesCustomSchemeAndHttpsHosts() {
        assertTrue(InviteRedirectUrls.isInviteRedirect("app.mymultiverse.kmp://invite?token=abc"))
        assertTrue(InviteRedirectUrls.isInviteRedirect("app.mymultiverse.kmp://invite"))
        assertTrue(InviteRedirectUrls.isInviteRedirect("https://mymultiverse.app/invite?token=abc"))
        assertTrue(
            InviteRedirectUrls.isInviteRedirect(
                "https://abc.supabase.co/functions/v1/invite-open?token=abc",
            ),
        )
        assertFalse(InviteRedirectUrls.isInviteRedirect("app.mymultiverse.kmp://auth"))
        assertFalse(InviteRedirectUrls.isInviteRedirect("https://evil.com/invite?token=abc"))
        assertFalse(InviteRedirectUrls.isInviteRedirect("https://mymultiverse.app/other?token=abc"))
    }
}
