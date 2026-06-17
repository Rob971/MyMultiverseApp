package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole

fun SpaceMemberRole.canWriteHouseholdData(): Boolean =
    this == SpaceMemberRole.Owner || this == SpaceMemberRole.Editor

fun SpaceMemberRole.canManageHouseholdMembers(): Boolean =
    this == SpaceMemberRole.Owner
