package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole

fun HouseholdMemberRole.canWriteHouseholdData(): Boolean =
    this == HouseholdMemberRole.Owner ||
        this == HouseholdMemberRole.Admin ||
        this == HouseholdMemberRole.Editor

fun HouseholdMemberRole.canManageHouseholdMembers(): Boolean =
    this == HouseholdMemberRole.Owner || this == HouseholdMemberRole.Admin

fun HouseholdMemberRole.canRenameHousehold(): Boolean =
    canManageHouseholdMembers()

fun HouseholdMemberRole.canAssignAdminRole(): Boolean =
    this == HouseholdMemberRole.Owner

fun HouseholdMemberRole.canChangeRoleOf(target: HouseholdMemberRole): Boolean =
    when (this) {
        HouseholdMemberRole.Owner -> target != HouseholdMemberRole.Owner
        HouseholdMemberRole.Admin ->
            target == HouseholdMemberRole.Editor || target == HouseholdMemberRole.Viewer
        else -> false
    }

fun HouseholdMemberRole.canRemoveMember(target: HouseholdMemberRole): Boolean =
    canChangeRoleOf(target)

fun HouseholdMemberRole.canInviteWithRole(inviteRole: HouseholdMemberRole): Boolean =
    when (inviteRole) {
        HouseholdMemberRole.Owner -> false
        HouseholdMemberRole.Admin -> canAssignAdminRole()
        HouseholdMemberRole.Editor, HouseholdMemberRole.Viewer -> canManageHouseholdMembers()
    }
