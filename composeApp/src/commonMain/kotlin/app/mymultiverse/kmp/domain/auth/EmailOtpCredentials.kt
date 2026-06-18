package app.mymultiverse.kmp.domain.auth

object EmailOtpCredentials {
    const val OTP_LENGTH = 6

    fun validationError(email: String, code: String): EmailOtpValidationError? {
        val trimmedEmail = email.trim()
        val trimmedCode = code.trim()
        when {
            trimmedEmail.isBlank() || trimmedCode.isBlank() -> return EmailOtpValidationError.MissingFields
            !isLikelyEmail(trimmedEmail) -> return EmailOtpValidationError.InvalidEmail
            !isValidOtpCode(trimmedCode) -> return EmailOtpValidationError.InvalidCode
        }
        return null
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
