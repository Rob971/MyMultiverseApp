package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberKind

/** Product guidance: a household is meant for cohabiting people (2 or more). */
const val HOUSEHOLD_RECOMMENDED_MIN_MEMBERS = 2

fun List<SpaceMember>.householdPeople(): List<SpaceMember> =
    filter { it.kind == SpaceMemberKind.Person }

fun householdMemberCount(members: List<SpaceMember>): Int =
    members.householdPeople().distinctBy { it.referenceId }.size

fun isHouseholdReadyForCollaboration(members: List<SpaceMember>): Boolean =
    householdMemberCount(members) >= HOUSEHOLD_RECOMMENDED_MIN_MEMBERS

fun canAddHouseholdMember(currentMembers: List<SpaceMember>): Boolean = true
