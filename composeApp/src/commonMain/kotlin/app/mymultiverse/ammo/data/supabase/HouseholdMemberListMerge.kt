package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole

internal fun mergeHouseholdPersonMembers(
    householdId: String,
    personMembers: List<HouseholdMember>,
    ownerId: String,
    ownerDisplayName: String,
    ownerAvatarUrl: String?,
    currentUserId: String?,
    currentUserMember: HouseholdMember?,
): List<HouseholdMember> {
    val ownerAlreadyListed = personMembers.any { it.referenceId == ownerId }
    val withOwner = if (ownerAlreadyListed) {
        personMembers
    } else {
        listOf(
            syntheticOwnerMember(
                householdId = householdId,
                ownerId = ownerId,
                ownerDisplayName = ownerDisplayName,
                avatarUrl = ownerAvatarUrl,
            ),
        ) + personMembers
    }

    if (currentUserId == null || withOwner.any { it.referenceId == currentUserId }) {
        return withOwner
    }

    return currentUserMember?.let { withOwner + it } ?: withOwner
}

internal fun syntheticOwnerMember(
    householdId: String,
    ownerId: String,
    ownerDisplayName: String,
    avatarUrl: String? = null,
): HouseholdMember =
    HouseholdMember(
        id = "$OWNER_MEMBER_PREFIX$ownerId",
        householdId = householdId,
        kind = HouseholdMemberKind.Person,
        displayName = ownerDisplayName,
        role = HouseholdMemberRole.Owner,
        referenceId = ownerId,
        avatarUrl = avatarUrl,
    )

internal const val OWNER_MEMBER_PREFIX = "owner-"
