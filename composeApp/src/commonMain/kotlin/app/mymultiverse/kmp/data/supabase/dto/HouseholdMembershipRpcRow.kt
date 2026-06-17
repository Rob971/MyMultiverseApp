package app.mymultiverse.kmp.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HouseholdMembershipRpcRow(
    val status: String,
    @SerialName("space_id") val spaceId: String? = null,
    @SerialName("space_name") val spaceName: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("owner_display_name") val ownerDisplayName: String? = null,
    val role: String? = null,
    val features: List<String>? = null,
)
