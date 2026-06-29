package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InviteHouseholdMemberRpcRow(
    val result: String? = null,
    @SerialName("invite_token") val inviteToken: String? = null,
    @SerialName("invite_id") val inviteId: String? = null,
)
