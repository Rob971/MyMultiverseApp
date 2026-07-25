package app.mymultiverse.ammo.domain.sharing

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind

/** Initials for member avatar chips (up to two characters). */
fun memberAvatarInitials(displayName: String): String {
    val name = displayName.trim()
    if (name.isEmpty()) return "?"
    val parts = name.split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> twoPartInitials(parts[0], parts[1])
        parts.size == 1 -> singlePartInitials(parts[0])
        else -> "?"
    }
}

private fun twoPartInitials(first: String, second: String): String {
    val initialA = meaningfulInitialChar(first)
    val initialB = meaningfulInitialChar(second)
    return when {
        initialA != null && initialB != null -> "${initialA}${initialB}".uppercase()
        initialA != null -> initialA.uppercaseChar().toString()
        initialB != null -> initialB.uppercaseChar().toString()
        else -> "?"
    }
}

private fun singlePartInitials(token: String): String {
    val letters = token.filter { it.isLetter() }
    return when {
        letters.length >= 2 -> letters.take(2).uppercase()
        letters.length == 1 -> letters.uppercase()
        token.length >= 2 -> token.take(2).uppercase()
        token.isNotEmpty() -> token.first().uppercaseChar().toString()
        else -> "?"
    }
}

private fun meaningfulInitialChar(token: String): Char? =
    token.firstOrNull { it.isLetter() } ?: token.firstOrNull()

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
