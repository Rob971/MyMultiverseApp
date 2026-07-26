package app.mymultiverse.ammo.data.supabase

import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HouseholdMemberRemovalRpcTest {

    @Test
    fun request_usesRemovalRpcAndMemberIdParameter() {
        val request = householdMemberRemovalRequest("member-123")

        assertEquals("remove_household_member", request.function)
        assertEquals("member-123", request.parameters.getValue("p_member_id").jsonPrimitive.content)
    }

    @Test
    fun request_rejectsSyntheticOwnerMember() {
        assertFailsWith<IllegalArgumentException> {
            householdMemberRemovalRequest("owner-user-123")
        }
    }

    @Test
    fun request_rejectsBlankMemberId() {
        assertFailsWith<IllegalArgumentException> {
            householdMemberRemovalRequest(" ")
        }
    }

    @Test
    fun declineRequest_usesSecuredRpcAndInviteIdParameter() {
        val request = householdInviteDeclineRequest("invite-123")

        assertEquals("decline_household_invite", request.function)
        assertEquals("invite-123", request.parameters.getValue("p_invite_id").jsonPrimitive.content)
    }

    @Test
    fun declineRequest_rejectsBlankInviteId() {
        assertFailsWith<IllegalArgumentException> {
            householdInviteDeclineRequest("")
        }
    }
}
