package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HouseholdPermissionRulesTest {

    @Test
    fun ownerAndEditorCanWrite() {
        assertTrue(SpaceMemberRole.Owner.canWriteHouseholdData())
        assertTrue(SpaceMemberRole.Editor.canWriteHouseholdData())
    }

    @Test
    fun viewerCannotWrite() {
        assertFalse(SpaceMemberRole.Viewer.canWriteHouseholdData())
    }

    @Test
    fun onlyOwnerCanManageMembers() {
        assertTrue(SpaceMemberRole.Owner.canManageHouseholdMembers())
        assertFalse(SpaceMemberRole.Editor.canManageHouseholdMembers())
        assertFalse(SpaceMemberRole.Viewer.canManageHouseholdMembers())
    }
}
