package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HouseholdMembershipRpcRow(
    val status: String,
    @SerialName("household_id") val householdId: String? = null,
    @SerialName("household_name") val householdName: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("owner_display_name") val ownerDisplayName: String? = null,
    val role: String? = null,
    val features: List<String>? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
