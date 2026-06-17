package app.mymultiverse.kmp.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PendingSpaceInviteRpcRow(
    val id: String,
    @SerialName("space_id") val spaceId: String,
    @SerialName("space_name") val spaceName: String,
    val email: String,
    val role: String,
    @SerialName("expires_at") val expiresAt: String? = null,
)
