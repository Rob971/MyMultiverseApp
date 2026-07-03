package app.mymultiverse.ammo.data.supabase

internal const val DELETED_PROFILE_DISPLAY_NAME = "Deleted user"

internal fun isDeletedProfileDisplayName(displayName: String?): Boolean =
    displayName?.trim()?.equals(DELETED_PROFILE_DISPLAY_NAME, ignoreCase = true) == true

/**
 * Resolves a person label from profile storage, treating the account-deletion sentinel
 * as absent so active users see their auth/email name again.
 */
internal fun resolvedProfileLabel(
    displayName: String?,
    email: String?,
    userId: String,
    authDisplayName: String? = null,
): String {
    if (!isDeletedProfileDisplayName(displayName)) {
        displayName?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    }
    authDisplayName?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    email?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    return userId
}
