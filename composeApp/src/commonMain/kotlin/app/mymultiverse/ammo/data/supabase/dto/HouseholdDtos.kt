package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HouseholdRpcRow(
    @SerialName("household_id") val householdId: String,
    @SerialName("household_name") val householdName: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("owner_display_name") val ownerDisplayName: String? = null,
    val features: List<String>? = null,
)
