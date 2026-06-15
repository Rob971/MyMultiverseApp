package app.mymultiverse.kmp.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpaceMemberRow(
    val id: String,
    @SerialName("space_id") val spaceId: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("group_id") val groupId: String? = null,
    val role: String,
)

@Serializable
data class SpaceMemberInsertRow(
    @SerialName("space_id") val spaceId: String,
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
data class ContactGroupRow(
    val id: String,
    val name: String,
    val lifecycle: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("expires_at") val expiresAt: String? = null,
)

@Serializable
data class ContactGroupInsertRow(
    val name: String,
    val lifecycle: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("expires_at") val expiresAt: String? = null,
)

@Serializable
data class GroupMemberInsertRow(
    @SerialName("group_id") val groupId: String,
    @SerialName("user_id") val userId: String,
)

@Serializable
data class FindProfileByEmailParams(
    @SerialName("p_email") val email: String,
)

@Serializable
data class ProfileIdRow(
    val id: String? = null,
)
