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
    fun adminCanChangeNonOwnerRolesButCannotAssignAdmin() {
        assertTrue(HouseholdMemberRole.Owner.canChangeRoleOf(HouseholdMemberRole.Editor))
        assertTrue(HouseholdMemberRole.Owner.canAssignAdminRole())
        assertTrue(HouseholdMemberRole.Admin.canChangeRoleOf(HouseholdMemberRole.Admin))
        assertTrue(HouseholdMemberRole.Admin.canChangeRoleOf(HouseholdMemberRole.Editor))
        assertTrue(HouseholdMemberRole.Admin.canChangeRoleOf(HouseholdMemberRole.Viewer))
        assertFalse(HouseholdMemberRole.Admin.canChangeRoleOf(HouseholdMemberRole.Owner))
        assertFalse(HouseholdMemberRole.Admin.canAssignAdminRole())
        assertFalse(HouseholdMemberRole.Admin.canInviteWithRole(HouseholdMemberRole.Admin))
    }
}
