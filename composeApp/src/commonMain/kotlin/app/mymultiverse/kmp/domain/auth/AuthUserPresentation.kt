package app.mymultiverse.kmp.domain.auth

import app.mymultiverse.kmp.domain.model.auth.AuthUser

/**
 * Resolves a short, user-facing name for greetings and labels.
 * Prefers profile display name, then the local part of the email.
 */
fun AuthUser.resolvedDisplayName(): String? {
    displayName?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    return email
        ?.substringBefore("@")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}
