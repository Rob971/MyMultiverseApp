package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.HouseholdMember
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HouseholdMemberRulesTest {

    @Test
    fun householdPeople_ignoresLegacyGroupRows() {
        val members = listOf(
            person("owner", "Owner"),
            person("mate", "Roommate"),
            HouseholdMember(
                id = "legacy-group",
                householdId = "household-1",
                kind = HouseholdMemberKind.Group,
                displayName = "Old group",
                role = HouseholdMemberRole.Editor,
                referenceId = "group-1",
            ),
        )

        assertEquals(2, members.householdPeople().size)
        assertEquals(2, householdMemberCount(members))
    }

    @Test
    fun isHouseholdReadyForCollaboration_requiresAtLeastTwoPeople() {
        val ownerOnly = listOf(person("owner", "Owner"))
        val pair = ownerOnly + person("partner", "Partner")

        assertFalse(isHouseholdReadyForCollaboration(ownerOnly))
        assertTrue(isHouseholdReadyForCollaboration(pair))
    }

    @Test
    fun householdMemberCount_supportsUnlimitedMembers() {
        val many = buildList {
            add(person("owner", "Owner"))
            repeat(20) { index ->
                add(person("user-$index", "Member $index"))
            }
        }

        assertEquals(21, householdMemberCount(many))
        assertTrue(canAddHouseholdMember(many))
    }

    private fun person(id: String, name: String): HouseholdMember =
        HouseholdMember(
            id = "member-$id",
            householdId = "household-1",
            kind = HouseholdMemberKind.Person,
            displayName = name,
            role = if (id == "owner") HouseholdMemberRole.Owner else HouseholdMemberRole.Editor,
            referenceId = id,
        )
}
