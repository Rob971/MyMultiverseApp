package app.mymultiverse.kmp.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SharingSpaceRow(
    val id: String,
    val topic: String,
    val name: String,
    @SerialName("owner_id") val ownerId: String,
)

@Serializable
data class SharingSpaceInsertRow(
    val topic: String,
    val name: String,
    @SerialName("owner_id") val ownerId: String,
)

@Serializable
data class NutritionFeatureRow(
    @SerialName("space_id") val spaceId: String,
    val feature: String,
)

@Serializable
data class NutritionFeatureInsertRow(
    @SerialName("space_id") val spaceId: String,
    val feature: String,
)

@Serializable
data class ProfileInsertRow(
    val id: String,
    val email: String? = null,
    @SerialName("display_name") val displayName: String? = null,
)
