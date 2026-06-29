package app.mymultiverse.ammo.domain.model.sharing

sealed class AddMemberResult {
    data object Added : AddMemberResult()

    data class InviteSent(
        val inviteToken: String,
    ) : AddMemberResult()
}

data class HouseholdInvite(
    val id: String,
    val householdId: String,
    val householdName: String,
    val email: String,
    val role: HouseholdMemberRole,
    val expiresAtEpochMillis: Long?,
    val inviteToken: String? = null,
)
