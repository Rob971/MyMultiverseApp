package app.mymultiverse.ammo.domain.sharing

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemberAvatarPresentationTest {

    @Test
    fun memberAvatarInitials_usesFirstLettersOfTwoWordName() {
        assertEquals("RC", memberAvatarInitials("Rosa Costa"))
    }

    @Test
    fun canEditMemberAvatar_allowsSelfForPersonMember() {
        val member = personMember(referenceId = "user-1")
        assertTrue(canEditMemberAvatar(member, currentUserId = "user-1", canManageMembers = false))
    }

    @Test
    fun canEditMemberAvatar_deniesOtherPersonMembers() {
        val member = personMember(referenceId = "user-2")
        assertFalse(canEditMemberAvatar(member, currentUserId = "user-1", canManageMembers = true))
    }

    @Test
    fun canEditMemberAvatar_allowsManagersForDependants() {
        val member = dependantMember()
        assertTrue(canEditMemberAvatar(member, currentUserId = "user-1", canManageMembers = true))
        assertFalse(canEditMemberAvatar(member, currentUserId = "user-1", canManageMembers = false))
    }

    private fun personMember(referenceId: String): HouseholdMember =
        HouseholdMember(
            id = "member-$referenceId",
            householdId = "household-1",
            kind = HouseholdMemberKind.Person,
            displayName = "Alex",
            role = HouseholdMemberRole.Editor,
            referenceId = referenceId,
        )

    private fun dependantMember(): HouseholdMember =
        HouseholdMember(
            id = "dependant-1",
            householdId = "household-1",
            kind = HouseholdMemberKind.Dependant,
            displayName = "Mia",
            role = HouseholdMemberRole.Viewer,
            referenceId = "dependant-1",
        )
}
