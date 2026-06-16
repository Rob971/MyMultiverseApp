package app.mymultiverse.kmp.domain.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EmailAuthCredentialsTest {

    @Test
    fun validationError_rejectsBlankFields() {
        assertEquals(
            EmailAuthValidationError.MissingFields,
            EmailAuthCredentials.validationError("", "secret", isSignUp = false),
        )
    }

    @Test
    fun validationError_rejectsInvalidEmail() {
        assertEquals(
            EmailAuthValidationError.InvalidEmail,
            EmailAuthCredentials.validationError("not-an-email", "secret123", isSignUp = false),
        )
    }

    @Test
    fun validationError_rejectsShortPasswordOnSignUp() {
        assertEquals(
            EmailAuthValidationError.WeakPassword,
            EmailAuthCredentials.validationError("user@example.com", "12345", isSignUp = true),
        )
    }

    @Test
    fun validationError_allowsShortPasswordOnSignIn() {
        assertNull(
            EmailAuthCredentials.validationError("user@example.com", "12345", isSignUp = false),
        )
    }

    @Test
    fun validationError_acceptsValidSignUpCredentials() {
        assertNull(
            EmailAuthCredentials.validationError("user@example.com", "secret123", isSignUp = true),
        )
    }
}
