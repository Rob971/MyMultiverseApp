package app.mymultiverse.kmp.domain.model.sharing

enum class AddMemberResult {
    Added,
    InviteSent,
}

data class HouseholdInvite(
    val id: String,
    val householdId: String,
    val householdName: String,
    val email: String,
    val role: HouseholdMemberRole,
    val expiresAtEpochMillis: Long?,
)
