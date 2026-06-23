package app.mymultiverse.kmp.presentation.navigation

import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.presentation.invite.InviteJoinAcceptState

/**
 * Resolves the post-authentication destination.
 *
 * When an invite deep link is pending, [HouseholdSetup] is skipped once membership becomes active
 * (invite accept flow activates the household session).
 */
fun resolvePostAuthRoute(
    membership: HouseholdMembershipStatus,
    pendingInviteToken: String?,
    acceptState: InviteJoinAcceptState,
): AppRoute? {
    if (!pendingInviteToken.isNullOrBlank()) {
        if (acceptState is InviteJoinAcceptState.Accepting) return null
        return when (membership) {
            is HouseholdMembershipStatus.Active ->
                AppRoute.Dashboard(householdId = membership.household.id)
            HouseholdMembershipStatus.Loading -> null
            HouseholdMembershipStatus.None,
            is HouseholdMembershipStatus.Error,
            -> null
        }
    }

    return when (membership) {
        HouseholdMembershipStatus.Loading -> null
        is HouseholdMembershipStatus.Active ->
            AppRoute.Dashboard(householdId = membership.household.id)
        HouseholdMembershipStatus.None -> AppRoute.HouseholdSetup
        is HouseholdMembershipStatus.Error -> AppRoute.HouseholdSetup
    }
}

fun shouldBlockAuthenticatedShell(
    membership: HouseholdMembershipStatus,
    pendingInviteToken: String?,
    acceptState: InviteJoinAcceptState,
): Boolean {
    if (!pendingInviteToken.isNullOrBlank()) {
        return membership !is HouseholdMembershipStatus.Active
    }
    if (acceptState is InviteJoinAcceptState.Accepting) return true
    if (membership is HouseholdMembershipStatus.Loading) return true
    return false
}
