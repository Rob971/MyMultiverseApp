package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class HouseholdNameAvailabilityRpcRow(
    val available: Boolean,
    val reason: String? = null,
)

@Serializable
internal data class RenameHouseholdRpcRow(
    val householdId: String,
    val householdName: String,
    val features: List<String>? = null,
)
