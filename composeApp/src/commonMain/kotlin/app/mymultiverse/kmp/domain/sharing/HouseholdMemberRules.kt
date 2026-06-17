package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.HouseholdMember
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberKind

/** Product guidance: a household is meant for cohabiting people (2 or more). */
const val HOUSEHOLD_RECOMMENDED_MIN_MEMBERS = 2

fun List<HouseholdMember>.householdPeople(): List<HouseholdMember> =
    filter { it.kind == HouseholdMemberKind.Person }

fun householdMemberCount(members: List<HouseholdMember>): Int =
    members.householdPeople().distinctBy { it.referenceId }.size

fun isHouseholdReadyForCollaboration(members: List<HouseholdMember>): Boolean =
    householdMemberCount(members) >= HOUSEHOLD_RECOMMENDED_MIN_MEMBERS

fun canAddHouseholdMember(currentMembers: List<HouseholdMember>): Boolean = true
