package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole

fun HouseholdMemberRole.canWriteHouseholdData(): Boolean =
    this == HouseholdMemberRole.Owner || this == HouseholdMemberRole.Editor

fun HouseholdMemberRole.canManageHouseholdMembers(): Boolean =
    this == HouseholdMemberRole.Owner
