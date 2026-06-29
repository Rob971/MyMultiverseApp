package app.mymultiverse.ammo.domain.auth

import app.mymultiverse.ammo.domain.model.auth.AuthUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthUserPresentationTest {

    @Test
    fun resolvedDisplayName_stripsWrappingQuotes() {
        val user = AuthUser(
            id = "user-1",
            email = "roberto@example.com",
            displayName = "\"Roberto Cornano\"",
        )

        assertEquals("Roberto Cornano", user.resolvedDisplayName())
    }

    @Test
    fun resolvedDisplayName_stripsSingleQuotes() {
        val user = AuthUser(
            id = "user-1",
            email = "roberto@example.com",
            displayName = "'Roberto'",
        )

        assertEquals("Roberto", user.resolvedDisplayName())
    }

    @Test
    fun resolvedDisplayName_fallsBackToEmailLocalPart() {
        val user = AuthUser(
            id = "user-1",
            email = "roberto@example.com",
            displayName = null,
        )

        assertEquals("roberto", user.resolvedDisplayName())
    }

    @Test
    fun resolvedDisplayName_blankDisplayNameUsesEmail() {
        val user = AuthUser(
            id = "user-1",
            email = "roberto@example.com",
            displayName = "   ",
        )

        assertEquals("roberto", user.resolvedDisplayName())
    }

    @Test
    fun avatarInitials_usesFirstLettersOfTwoWordName() {
        val user = AuthUser(
            id = "user-1",
            email = "roberto@example.com",
            displayName = "Roberto Cornano",
        )

        assertEquals("RC", user.avatarInitials())
    }

    @Test
    fun sanitizeDisplayName_nullForBlank() {
        assertNull(sanitizeDisplayName("   "))
    }
}
