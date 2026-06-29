package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HouseholdMemberRow(
    val id: String,
    @SerialName("household_id") val householdId: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("group_id") val groupId: String? = null,
    val role: String,
)

@Serializable
data class HouseholdMemberInsertRow(
    @SerialName("household_id") val householdId: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("group_id") val groupId: String? = null,
    val role: String = "editor",
)

@Serializable
data class ProfileRow(
    val id: String,
    val email: String? = null,
    @SerialName("display_name") val displayName: String? = null,
)

@Serializable
data class HouseholdInviteRow(
    val id: String,
    @SerialName("household_id") val householdId: String,
    val email: String,
    val role: String,
    val token: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("accepted_at") val acceptedAt: String? = null,
    @SerialName("declined_at") val declinedAt: String? = null,
)

@Serializable
data class HouseholdInviteInsertRow(
    @SerialName("household_id") val householdId: String,
    val email: String,
    val role: String,
    @SerialName("invited_by") val invitedBy: String,
)

@Serializable
data class HouseholdInviteUpdateRow(
    @SerialName("declined_at") val declinedAt: String,
)

@Serializable
data class HouseholdInvitePendingUpdateRow(
    val role: String,
    @SerialName("invited_by") val invitedBy: String,
)

@Serializable
data class FindProfileByEmailParams(
    @SerialName("p_email") val email: String,
)

@Serializable
data class ProfileIdRow(
    val id: String? = null,
)
