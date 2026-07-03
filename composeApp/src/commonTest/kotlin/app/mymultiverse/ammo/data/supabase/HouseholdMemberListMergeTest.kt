package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HouseholdMemberListMergeTest {

    @Test
    fun mergeHouseholdPersonMembers_addsSyntheticOwnerWhenMissing() {
        val editor = personMember(
            id = "member-2",
            referenceId = "editor-1",
            displayName = "Editor One",
            role = HouseholdMemberRole.Editor,
        )

        val merged = mergeHouseholdPersonMembers(
            householdId = "household-1",
            personMembers = listOf(editor),
            ownerId = "owner-1",
            ownerDisplayName = "Owner One",
            ownerAvatarUrl = null,
            currentUserId = "editor-1",
            currentUserMember = null,
        )

        assertEquals(2, merged.size)
        assertTrue(merged.any { it.referenceId == "owner-1" && it.role == HouseholdMemberRole.Owner })
        assertTrue(merged.any { it.referenceId == "editor-1" })
    }

    @Test
    fun mergeHouseholdPersonMembers_addsCurrentUserWhenMissingFromBulkQuery() {
        val owner = personMember(
            id = "member-1",
            referenceId = "owner-1",
            displayName = "Owner One",
            role = HouseholdMemberRole.Owner,
        )
        val currentUser = personMember(
            id = "member-2",
            referenceId = "editor-1",
            displayName = "Editor One",
            role = HouseholdMemberRole.Editor,
        )

        val merged = mergeHouseholdPersonMembers(
            householdId = "household-1",
            personMembers = listOf(owner),
            ownerId = "owner-1",
            ownerDisplayName = "Owner One",
            ownerAvatarUrl = null,
            currentUserId = "editor-1",
            currentUserMember = currentUser,
        )

        assertEquals(2, merged.size)
        assertTrue(merged.any { it.referenceId == "editor-1" })
    }

    @Test
    fun mergeHouseholdPersonMembers_doesNotDuplicateOwnerOrCurrentUser() {
        val owner = personMember(
            id = "member-1",
            referenceId = "owner-1",
            displayName = "Owner One",
            role = HouseholdMemberRole.Owner,
        )
        val editor = personMember(
            id = "member-2",
            referenceId = "editor-1",
            displayName = "Editor One",
            role = HouseholdMemberRole.Editor,
        )

        val merged = mergeHouseholdPersonMembers(
            householdId = "household-1",
            personMembers = listOf(owner, editor),
            ownerId = "owner-1",
            ownerDisplayName = "Owner One",
            ownerAvatarUrl = null,
            currentUserId = "editor-1",
            currentUserMember = editor,
        )

        assertEquals(2, merged.size)
    }

    private fun personMember(
        id: String,
        referenceId: String,
        displayName: String,
        role: HouseholdMemberRole,
    ): HouseholdMember =
        HouseholdMember(
            id = id,
            householdId = "household-1",
            kind = HouseholdMemberKind.Person,
            displayName = displayName,
            role = role,
            referenceId = referenceId,
        )
}
