package app.mymultiverse.kmp.domain.sharing

/**
 * Suggests a default household name from the user's profile (last name preferred)
 * or email local part when creating a household during onboarding.
 */
object HouseholdDefaultName {
    fun suggest(displayName: String?, email: String?): String {
        lastNameFromDisplayName(displayName)?.let { return it }
        return localPartFromEmail(email)
    }

    private fun lastNameFromDisplayName(displayName: String?): String? {
        val trimmed = displayName?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return trimmed
            .split(Regex("\\s+"))
            .lastOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun localPartFromEmail(email: String?): String {
        val localPart = email
            ?.substringBefore('@')
            ?.trim()
            .orEmpty()
        if (localPart.isEmpty()) return ""
        return localPart.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecaseChar() else char
        }
    }
}
