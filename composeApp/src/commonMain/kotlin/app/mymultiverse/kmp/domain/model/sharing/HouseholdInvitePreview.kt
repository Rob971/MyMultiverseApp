package app.mymultiverse.kmp.domain.model.sharing

data class HouseholdInvitePreview(
    val inviteId: String,
    val householdId: String,
    val householdName: String,
    val inviterName: String,
    val inviteeEmail: String,
    val role: HouseholdMemberRole,
    val expiresAtEpochMillis: Long?,
)
