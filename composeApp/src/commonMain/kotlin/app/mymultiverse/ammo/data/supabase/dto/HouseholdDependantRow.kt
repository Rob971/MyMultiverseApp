package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HouseholdDependantRow(
    val id: String,
    @SerialName("household_id") val householdId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("removed_at") val removedAt: String? = null,
)
