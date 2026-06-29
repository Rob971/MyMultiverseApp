package app.mymultiverse.ammo.domain.auth

object EmailOtpCredentials {
    const val OTP_LENGTH = 6

    fun validationError(email: String, code: String): EmailOtpValidationError? {
        emailValidationError(email)?.let { return it }
        val trimmedCode = code.trim()
        when {
            trimmedCode.isBlank() -> return EmailOtpValidationError.MissingFields
            !isValidOtpCode(trimmedCode) -> return EmailOtpValidationError.InvalidCode
        }
        return null
    }

    fun emailValidationError(email: String): EmailOtpValidationError? {
        val trimmedEmail = email.trim()
        return when {
            trimmedEmail.isBlank() -> EmailOtpValidationError.MissingFields
            !isLikelyEmail(trimmedEmail) -> EmailOtpValidationError.InvalidEmail
            else -> null
        }
    }

    private fun isLikelyEmail(value: String): Boolean {
        val atIndex = value.indexOf('@')
        return atIndex > 0 && value.indexOf('.', atIndex) > atIndex + 1
    }

    private fun isValidOtpCode(value: String): Boolean =
        value.length == OTP_LENGTH && value.all { it.isDigit() }
}

enum class EmailOtpValidationError {
    MissingFields,
    InvalidEmail,
    InvalidCode,
}
