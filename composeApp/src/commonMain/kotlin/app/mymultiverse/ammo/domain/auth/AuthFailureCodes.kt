package app.mymultiverse.ammo.domain.auth

object AuthFailureCodes {
    const val EMAIL_CONFIRMATION_REQUIRED = "email_confirmation_required"
    const val INVALID_CREDENTIALS = "invalid_credentials"
    const val INVALID_EMAIL = "invalid_email"
    const val WEAK_PASSWORD = "weak_password"
    const val USER_ALREADY_EXISTS = "user_already_exists"
    const val EMAIL_NOT_CONFIRMED = "email_not_confirmed"
    const val SIGN_UP_DISABLED = "sign_up_disabled"
    const val OTP_INVALID = "otp_invalid"
    const val OTP_EXPIRED = "otp_expired"
    const val OTP_RATE_LIMITED = "otp_rate_limited"
    const val GENERIC = "generic"

    fun fromThrowable(throwable: Throwable): String {
        if (throwable.message == EMAIL_CONFIRMATION_REQUIRED) return EMAIL_CONFIRMATION_REQUIRED

        val message = buildString {
            append(throwable.message.orEmpty())
            throwable.cause?.message?.let { cause ->
                if (isNotEmpty()) append(' ')
                append(cause)
            }
        }.lowercase()

        return when {
            message.contains("invalid login credentials") ||
                message.contains("invalid_credentials") ||
                message == "sign_in_failed" -> INVALID_CREDENTIALS
            message.contains("email not confirmed") ||
                message.contains("email_not_confirmed") -> EMAIL_NOT_CONFIRMED
            message.contains("user already registered") ||
                message.contains("already been registered") ||
                message.contains("user_already_exists") -> USER_ALREADY_EXISTS
            message.contains("password") && (
                message.contains("weak") ||
                    message.contains("short") ||
                    message.contains("at least")
                ) -> WEAK_PASSWORD
            message.contains("invalid email") ||
                message.contains("unable to validate email") -> INVALID_EMAIL
            message.contains("signup_disabled") ||
                message.contains("signups not allowed") -> SIGN_UP_DISABLED
            message.contains("otp expired") ||
                message.contains("token has expired") ||
                message.contains("otp_expired") -> OTP_EXPIRED
            message.contains("rate limit") ||
                message.contains("too many requests") ||
                message.contains("otp_rate_limited") -> OTP_RATE_LIMITED
            message.contains("invalid otp") ||
                message.contains("otp invalid") ||
                message.contains("otp_invalid") ||
                message.contains("invalid token") -> OTP_INVALID
            else -> GENERIC
        }
    }
}
