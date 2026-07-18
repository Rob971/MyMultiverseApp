package app.mymultiverse.ammo.domain.sharing

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind

/** Initials for member avatar chips (up to two characters). */
fun memberAvatarInitials(displayName: String): String {
    val name = displayName.trim()
    if (name.isEmpty()) return "?"
    val parts = name.split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> "${parts.first().first()}${parts[1].first()}".uppercase()
        parts.size == 1 && parts[0].length >= 2 -> parts[0].take(2).uppercase()
        parts.size == 1 -> parts[0].first().uppercase()
        else -> "?"
    }
}

/**
 * Returns true when the current user is allowed to upload a new photo for [member].
 *
 * Rules:
 * - **Person member**: only the person themselves can change their own profile photo.
 *   Owner/admin roles do NOT grant edit rights over another person's picture —
 *   both the storage RLS (`profiles/{auth.uid()}` folder) and the data layer
 *   enforce this; the UI must match.
 * - **Dependant**: any member who can write household data (owner, admin, editor)
 *   may manage a dependant's photo, consistent with the storage RLS function
 *   `can_upload_dependant_avatar` → `household_member_can_write_nutrition`.
 * - **Group**: never editable (no photo concept for groups).
 *
 * @param canWriteHouseholdData true when the current user's role is Owner, Admin, or Editor.
 */
fun canEditMemberAvatar(
    member: HouseholdMember,
    currentUserId: String?,
    canWriteHouseholdData: Boolean,
): Boolean {
    if (currentUserId == null) return false
    return when (member.kind) {
        HouseholdMemberKind.Person -> member.referenceId == currentUserId
        HouseholdMemberKind.Dependant -> canWriteHouseholdData
        HouseholdMemberKind.Group -> false
    }
}
