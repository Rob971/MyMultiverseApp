package app.mymultiverse.ammo.domain.sharing

object HouseholdNameRules {
    const val MIN_LENGTH = 2
    const val MAX_LENGTH = 40

    fun collapseWhitespace(value: String): String =
        value.trim().replace(Regex("\\s+"), " ")

    /** Normalized form used for client-side availability preview (server is authoritative). */
    fun normalizeForUniqueness(name: String): String =
        collapseWhitespace(name).lowercase()

    fun validationError(name: String): HouseholdNameValidationError? {
        val collapsed = collapseWhitespace(name)
        return when {
            collapsed.isEmpty() -> HouseholdNameValidationError.Required
            collapsed.length < MIN_LENGTH -> HouseholdNameValidationError.TooShort
            collapsed.length > MAX_LENGTH -> HouseholdNameValidationError.TooLong
            else -> null
        }
    }
}

enum class HouseholdNameValidationError {
    Required,
    TooShort,
    TooLong,
}
