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

    @Test
    fun fromThrowable_mapsOtpInvalid() {
        val code = AuthFailureCodes.fromThrowable(
            IllegalStateException("Invalid OTP"),
        )
        assertEquals(AuthFailureCodes.OTP_INVALID, code)
    }

    @Test
    fun fromThrowable_mapsOtpExpired() {
        val code = AuthFailureCodes.fromThrowable(
            IllegalStateException("Token has expired or is invalid"),
        )
        assertEquals(AuthFailureCodes.OTP_EXPIRED, code)
    }

    @Test
    fun fromThrowable_mapsOtpRateLimited() {
        val code = AuthFailureCodes.fromThrowable(
            IllegalStateException("Email rate limit exceeded"),
        )
        assertEquals(AuthFailureCodes.OTP_RATE_LIMITED, code)
    }
}
