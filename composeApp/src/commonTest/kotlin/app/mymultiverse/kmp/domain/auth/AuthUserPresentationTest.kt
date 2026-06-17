package app.mymultiverse.kmp.domain.auth

import app.mymultiverse.kmp.domain.model.auth.AuthUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthUserPresentationTest {

    @Test
    fun resolvedDisplayName_prefersDisplayName() {
        val user = AuthUser(id = "1", email = "a@b.c", displayName = "  Roberto  ")

        assertEquals("Roberto", user.resolvedDisplayName())
    }

    @Test
    fun resolvedDisplayName_fallsBackToEmailLocalPart() {
        val user = AuthUser(id = "1", email = "roberto@example.com", displayName = null)

        assertEquals("roberto", user.resolvedDisplayName())
    }

    @Test
    fun resolvedDisplayName_ignoresBlankDisplayNameAndUsesEmail() {
        val user = AuthUser(id = "1", email = "maria@example.com", displayName = "   ")

        assertEquals("maria", user.resolvedDisplayName())
    }

    @Test
    fun resolvedDisplayName_returnsNullWhenNoUsableName() {
        val user = AuthUser(id = "1", email = null, displayName = null)

        assertNull(user.resolvedDisplayName())
    }
}
