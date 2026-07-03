package app.mymultiverse.ammo.domain.sharing

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

fun canEditMemberAvatar(
    member: app.mymultiverse.ammo.domain.model.sharing.HouseholdMember,
    currentUserId: String?,
    canManageMembers: Boolean,
): Boolean {
    if (currentUserId == null) return false
    return when (member.kind) {
        app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind.Person ->
            member.referenceId == currentUserId
        app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind.Dependant ->
            canManageMembers
        app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind.Group -> false
    }
}
