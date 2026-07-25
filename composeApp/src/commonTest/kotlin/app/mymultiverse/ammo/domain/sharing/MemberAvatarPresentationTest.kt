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
    fun memberAvatarInitials_singleWordNameUsesFirstTwoLetters() {
        assertEquals("CA", memberAvatarInitials("Carola"))
    }

    @Test
    fun memberAvatarInitials_handlesApostropheNames() {
        assertEquals("OB", memberAvatarInitials("O'Brien"))
    }

    @Test
    fun memberAvatarInitials_handlesHyphenatedSingleWord() {
        assertEquals("JE", memberAvatarInitials("Jean-Luc"))
    }

    @Test
    fun memberAvatarInitials_handlesArabicNames() {
        assertEquals("مع", memberAvatarInitials("محمد علي"))
    }

    @Test
    fun memberAvatarInitials_emptyNameReturnsQuestionMark() {
        assertEquals("?", memberAvatarInitials("   "))
    }

    // ── Person members ────────────────────────────────────────────────────────

    @Test
    fun canEditMemberAvatar_allowsSelfForPersonMember() {
        val member = personMember(referenceId = "user-1")
        assertTrue(canEditMemberAvatar(member, currentUserId = "user-1", canWriteHouseholdData = false))
    }

    @Test
    fun canEditMemberAvatar_allowsSelfRegardlessOfWriteRole() {
        val member = personMember(referenceId = "user-1")
        // Self can always edit their own photo, regardless of whether they have write-data rights.
        assertTrue(canEditMemberAvatar(member, currentUserId = "user-1", canWriteHouseholdData = true))
    }

    @Test
    fun canEditMemberAvatar_deniesManagersEditingOtherPersonsProfile() {
        // Owners and admins (canWriteHouseholdData = true) must NOT be able to edit
        // another person's profile photo — storage RLS enforces profiles/{auth.uid()} only.
        val member = personMember(referenceId = "user-2")
        assertFalse(canEditMemberAvatar(member, currentUserId = "user-1", canWriteHouseholdData = true))
    }

    @Test
    fun canEditMemberAvatar_deniesNonManagersForOtherPersonMembers() {
        val member = personMember(referenceId = "user-2")
        assertFalse(canEditMemberAvatar(member, currentUserId = "user-1", canWriteHouseholdData = false))
    }

    @Test
    fun canEditMemberAvatar_deniesNullCurrentUser() {
        val member = personMember(referenceId = "user-1")
        assertFalse(canEditMemberAvatar(member, currentUserId = null, canWriteHouseholdData = true))
    }

    // ── Dependant members ────────────────────────────────────────────────────

    @Test
    fun canEditMemberAvatar_allowsWriteRoleForDependants() {
        val member = dependantMember()
        // Owner, admin, and editor all have canWriteHouseholdData = true.
        assertTrue(canEditMemberAvatar(member, currentUserId = "user-1", canWriteHouseholdData = true))
    }

    @Test
    fun canEditMemberAvatar_deniesGroupMembers() {
        val member = HouseholdMember(
            id = "group-1",
            householdId = "household-1",
            kind = HouseholdMemberKind.Group,
            displayName = "Everyone",
            role = HouseholdMemberRole.Viewer,
            referenceId = "group-1",
        )
        assertFalse(canEditMemberAvatar(member, currentUserId = "user-1", canWriteHouseholdData = true))
    }

    @Test
    fun canEditMemberAvatar_deniesViewerForDependants() {
        val member = dependantMember()
        // Viewers have canWriteHouseholdData = false.
        assertFalse(canEditMemberAvatar(member, currentUserId = "user-1", canWriteHouseholdData = false))
    }

    @Test
    fun canEditMemberAvatar_adminCanEditDependantButNotOtherAdult() {
        val dependant = dependantMember()
        val otherAdult = personMember(referenceId = "user-2")
        assertTrue(canEditMemberAvatar(dependant, currentUserId = "admin-1", canWriteHouseholdData = true))
        assertFalse(canEditMemberAvatar(otherAdult, currentUserId = "admin-1", canWriteHouseholdData = true))
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
