package app.mymultiverse.kmp.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HouseholdRpcRow(
    @SerialName("space_id") val spaceId: String,
    @SerialName("space_name") val spaceName: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("owner_display_name") val ownerDisplayName: String? = null,
    val features: List<String>? = null,
)
