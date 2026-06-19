package app.mymultiverse.kmp.presentation.screens.household

sealed interface InviteActionMessage {
    data object AcceptFailed : InviteActionMessage
    data object EmailMismatch : InviteActionMessage
    data class Joined(val householdName: String) : InviteActionMessage
}

data class SwitchHouseholdPrompt(
    val inviteId: String,
    val invitedHouseholdName: String,
    val currentHouseholdName: String,
)
