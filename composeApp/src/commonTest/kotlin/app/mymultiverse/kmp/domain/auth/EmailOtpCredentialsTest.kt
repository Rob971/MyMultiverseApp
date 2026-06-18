package app.mymultiverse.kmp.domain.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EmailOtpCredentialsTest {

    @Test
    fun validationError_rejectsBlankFields() {
        assertEquals(
            EmailOtpValidationError.MissingFields,
            EmailOtpCredentials.validationError("", "123456"),
        )
        assertEquals(
            EmailOtpValidationError.MissingFields,
            EmailOtpCredentials.validationError("user@example.com", ""),
        )
    }

    @Test
    fun validationError_rejectsInvalidEmail() {
        assertEquals(
            EmailOtpValidationError.InvalidEmail,
            EmailOtpCredentials.validationError("not-an-email", "123456"),
        )
    }

    @Test
    fun validationError_rejectsNonSixDigitCode() {
        assertEquals(
            EmailOtpValidationError.InvalidCode,
            EmailOtpCredentials.validationError("user@example.com", "12345"),
        )
        assertEquals(
            EmailOtpValidationError.InvalidCode,
            EmailOtpCredentials.validationError("user@example.com", "1234567"),
        )
        assertEquals(
            EmailOtpValidationError.InvalidCode,
            EmailOtpCredentials.validationError("user@example.com", "12a456"),
        )
    }

    @Test
    fun validationError_acceptsValidCredentials() {
        assertNull(
            EmailOtpCredentials.validationError("user@example.com", "123456"),
        )
    }
}
