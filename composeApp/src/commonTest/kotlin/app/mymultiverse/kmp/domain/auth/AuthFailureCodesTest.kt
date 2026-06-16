package app.mymultiverse.kmp.domain.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class AuthFailureCodesTest {

    @Test
    fun fromThrowable_mapsInvalidLoginCredentials() {
        val code = AuthFailureCodes.fromThrowable(
            IllegalStateException("Invalid login credentials"),
        )
        assertEquals(AuthFailureCodes.INVALID_CREDENTIALS, code)
    }

    @Test
    fun fromThrowable_mapsEmailNotConfirmed() {
        val code = AuthFailureCodes.fromThrowable(
            IllegalStateException("Email not confirmed"),
        )
        assertEquals(AuthFailureCodes.EMAIL_NOT_CONFIRMED, code)
    }

    @Test
    fun fromThrowable_mapsUserAlreadyExists() {
        val code = AuthFailureCodes.fromThrowable(
            IllegalStateException("User already registered"),
        )
        assertEquals(AuthFailureCodes.USER_ALREADY_EXISTS, code)
    }

    @Test
    fun fromThrowable_mapsEmailConfirmationRequired() {
        val code = AuthFailureCodes.fromThrowable(
            IllegalStateException(AuthFailureCodes.EMAIL_CONFIRMATION_REQUIRED),
        )
        assertEquals(AuthFailureCodes.EMAIL_CONFIRMATION_REQUIRED, code)
    }
}
