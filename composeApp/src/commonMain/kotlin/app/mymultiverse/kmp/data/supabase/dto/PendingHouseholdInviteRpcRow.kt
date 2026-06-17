package app.mymultiverse.kmp.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PendingHouseholdInviteRpcRow(
    val id: String,
    @SerialName("household_id") val householdId: String,
    @SerialName("household_name") val householdName: String,
    val email: String,
    val role: String,
    @SerialName("expires_at") val expiresAt: String? = null,
)
