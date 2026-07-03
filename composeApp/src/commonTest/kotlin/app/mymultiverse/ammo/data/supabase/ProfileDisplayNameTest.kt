package app.mymultiverse.ammo.data.supabase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileDisplayNameTest {

    @Test
    fun isDeletedProfileDisplayName_matchesSentinelCaseInsensitively() {
        assertTrue(isDeletedProfileDisplayName("Deleted user"))
        assertTrue(isDeletedProfileDisplayName(" deleted user "))
        assertFalse(isDeletedProfileDisplayName("Roberto Cornano"))
    }

    @Test
    fun resolvedProfileLabel_ignoresDeletedSentinelAndUsesAuthName() {
        assertEquals(
            "Roberto Cornano",
            resolvedProfileLabel(
                displayName = DELETED_PROFILE_DISPLAY_NAME,
                email = "roberto@example.com",
                userId = "user-1",
                authDisplayName = "Roberto Cornano",
            ),
        )
    }

    @Test
    fun resolvedProfileLabel_fallsBackToEmailWhenSentinelWithoutAuthName() {
        assertEquals(
            "roberto@example.com",
            resolvedProfileLabel(
                displayName = DELETED_PROFILE_DISPLAY_NAME,
                email = "roberto@example.com",
                userId = "user-1",
            ),
        )
    }

    @Test
    fun resolvedProfileLabel_prefersStoredDisplayName() {
        assertEquals(
            "Carola",
            resolvedProfileLabel(
                displayName = "Carola",
                email = "carola@example.com",
                userId = "user-2",
                authDisplayName = "Different",
            ),
        )
    }
}
