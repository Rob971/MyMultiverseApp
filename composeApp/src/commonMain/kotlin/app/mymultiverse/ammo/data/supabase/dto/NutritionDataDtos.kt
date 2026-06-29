package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NutritionWeekDataRow(
    @SerialName("household_id") val householdId: String,
    @SerialName("week_key") val weekKey: String,
    @SerialName("data_kind") val dataKind: String,
    val payload: String,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("updated_by") val updatedBy: String? = null,
)
