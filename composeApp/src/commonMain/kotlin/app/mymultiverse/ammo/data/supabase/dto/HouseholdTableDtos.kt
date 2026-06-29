package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HouseholdRow(
    val id: String,
    val topic: String,
    val name: String,
    @SerialName("owner_id") val ownerId: String,
)

@Serializable
data class HouseholdInsertRow(
    val topic: String,
    val name: String,
    @SerialName("owner_id") val ownerId: String,
)

@Serializable
data class NutritionFeatureRow(
    @SerialName("household_id") val householdId: String,
    val feature: String,
)

@Serializable
data class NutritionFeatureInsertRow(
    @SerialName("household_id") val householdId: String,
    val feature: String,
)

@Serializable
data class ProfileInsertRow(
    val id: String,
    val email: String? = null,
    @SerialName("display_name") val displayName: String? = null,
)
