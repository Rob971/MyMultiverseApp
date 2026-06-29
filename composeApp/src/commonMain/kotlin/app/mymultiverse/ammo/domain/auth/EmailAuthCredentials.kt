package app.mymultiverse.ammo.domain.auth

object EmailAuthCredentials {
    const val MIN_PASSWORD_LENGTH = 6

    fun validationError(
        email: String,
        password: String,
        isSignUp: Boolean,
    ): EmailAuthValidationError? {
        val trimmedEmail = email.trim()
        when {
            trimmedEmail.isBlank() || password.isBlank() -> return EmailAuthValidationError.MissingFields
            !isLikelyEmail(trimmedEmail) -> return EmailAuthValidationError.InvalidEmail
            isSignUp && password.length < MIN_PASSWORD_LENGTH -> return EmailAuthValidationError.WeakPassword
        }
        return null
    }

    private fun isLikelyEmail(value: String): Boolean {
        val atIndex = value.indexOf('@')
        return atIndex > 0 && value.indexOf('.', atIndex) > atIndex + 1
    }
}

enum class EmailAuthValidationError {
    MissingFields,
    InvalidEmail,
    WeakPassword,
}
