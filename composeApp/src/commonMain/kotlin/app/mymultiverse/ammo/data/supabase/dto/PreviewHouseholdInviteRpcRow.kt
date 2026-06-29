package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreviewHouseholdInviteRpcRow(
    @SerialName("invite_id") val inviteId: String,
    @SerialName("household_id") val householdId: String,
    @SerialName("household_name") val householdName: String,
    @SerialName("inviter_name") val inviterName: String,
    @SerialName("invitee_email") val inviteeEmail: String,
    val role: String,
    @SerialName("expires_at") val expiresAt: String? = null,
)
