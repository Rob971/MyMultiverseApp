package app.mymultiverse.ammo.data.supabase

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal data class HouseholdCrudRpcRequest(
    val function: String,
    val parameters: JsonObject,
)

internal fun householdMemberRemovalRequest(memberId: String): HouseholdCrudRpcRequest {
    require(memberId.isNotBlank()) { "member_not_found" }
    require(!memberId.startsWith(OWNER_MEMBER_PREFIX)) { "cannot_remove_owner" }

    return HouseholdCrudRpcRequest(
        function = "remove_household_member",
        parameters = buildJsonObject { put("p_member_id", memberId) },
    )
}

internal fun householdInviteDeclineRequest(inviteId: String): HouseholdCrudRpcRequest {
    require(inviteId.isNotBlank()) { "invite_not_found" }

    return HouseholdCrudRpcRequest(
        function = "decline_household_invite",
        parameters = buildJsonObject { put("p_invite_id", inviteId) },
    )
}
