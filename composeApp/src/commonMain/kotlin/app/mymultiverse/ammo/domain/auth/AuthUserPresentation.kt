package app.mymultiverse.ammo.domain.auth

import app.mymultiverse.ammo.domain.model.auth.AuthUser

/**
 * Resolves a short, user-facing name for greetings and labels.
 * Prefers profile display name, then the local part of the email.
 */
fun AuthUser.resolvedDisplayName(): String? =
    sanitizeDisplayName(displayName)
        ?: email
            ?.substringBefore("@")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::sanitizeDisplayName)

/** Strips whitespace and wrapping quotes from auth metadata display names. */
internal fun sanitizeDisplayName(raw: String?): String? {
    var value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    while (
        value.length >= 2 &&
        (
            (value.first() == '"' && value.last() == '"') ||
                (value.first() == '\'' && value.last() == '\'')
            )
    ) {
        value = value.substring(1, value.length - 1).trim()
    }
    return value.takeIf { it.isNotEmpty() }
}

/** Initials for avatar chips (up to two characters). */
fun AuthUser.avatarInitials(): String {
    val name = resolvedDisplayName().orEmpty()
    if (name.isEmpty()) return "?"
    val parts = name.split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> "${parts.first().first()}${parts[1].first()}".uppercase()
        parts.size == 1 && parts[0].length >= 2 -> parts[0].take(2).uppercase()
        parts.size == 1 -> parts[0].first().uppercase()
        else -> "?"
    }
}
