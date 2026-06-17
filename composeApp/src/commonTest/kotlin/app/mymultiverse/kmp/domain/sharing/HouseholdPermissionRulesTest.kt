package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HouseholdPermissionRulesTest {

    @Test
    fun ownerAndEditorCanWrite() {
        assertTrue(HouseholdMemberRole.Owner.canWriteHouseholdData())
        assertTrue(HouseholdMemberRole.Editor.canWriteHouseholdData())
    }

    @Test
    fun viewerCannotWrite() {
        assertFalse(HouseholdMemberRole.Viewer.canWriteHouseholdData())
    }

    @Test
    fun onlyOwnerCanManageMembers() {
        assertTrue(HouseholdMemberRole.Owner.canManageHouseholdMembers())
        assertFalse(HouseholdMemberRole.Editor.canManageHouseholdMembers())
        assertFalse(HouseholdMemberRole.Viewer.canManageHouseholdMembers())
    }
}
