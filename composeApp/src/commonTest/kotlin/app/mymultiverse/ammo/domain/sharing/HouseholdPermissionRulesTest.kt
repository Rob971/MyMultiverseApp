package app.mymultiverse.ammo.domain.sharing

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HouseholdPermissionRulesTest {

    @Test
    fun writeAccess_includesAdminAndEditor() {
        assertTrue(HouseholdMemberRole.Owner.canWriteHouseholdData())
        assertTrue(HouseholdMemberRole.Admin.canWriteHouseholdData())
        assertTrue(HouseholdMemberRole.Editor.canWriteHouseholdData())
        assertFalse(HouseholdMemberRole.Viewer.canWriteHouseholdData())
    }

    @Test
    fun manageMembers_includesOwnerAndAdmin() {
        assertTrue(HouseholdMemberRole.Owner.canManageHouseholdMembers())
        assertTrue(HouseholdMemberRole.Admin.canManageHouseholdMembers())
        assertFalse(HouseholdMemberRole.Editor.canManageHouseholdMembers())
    }

    @Test
    fun adminCannotAssignAdminOrChangeOwner() {
        assertTrue(HouseholdMemberRole.Owner.canChangeRoleOf(HouseholdMemberRole.Editor))
        assertTrue(HouseholdMemberRole.Owner.canAssignAdminRole())
        assertFalse(HouseholdMemberRole.Admin.canChangeRoleOf(HouseholdMemberRole.Admin))
        assertFalse(HouseholdMemberRole.Admin.canAssignAdminRole())
        assertTrue(HouseholdMemberRole.Admin.canChangeRoleOf(HouseholdMemberRole.Viewer))
    }
}
