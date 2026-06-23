package app.mymultiverse.kmp.presentation.navigation

import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembership
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.presentation.invite.InviteJoinAcceptState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnboardingRouteResolverTest {

    private val activeHousehold = Household(
        id = "household-1",
        name = "Our household",
        ownerId = "owner-1",
        ownerDisplayName = "Owner",
        nutritionFeatures = emptySet(),
    )

    private val activeMembership = HouseholdMembershipStatus.Active(
        membership = HouseholdMembership(
            household = activeHousehold,
            role = HouseholdMemberRole.Owner,
        ),
    )

    @Test
    fun resolvePostAuthRoute_withoutInviteAndNoMembership_returnsHouseholdSetup() {
        val route = resolvePostAuthRoute(
            membership = HouseholdMembershipStatus.None,
            pendingInviteToken = null,
            acceptState = InviteJoinAcceptState.Idle,
        )

        assertEquals(AppRoute.HouseholdSetup, route)
    }

    @Test
    fun resolvePostAuthRoute_withActiveMembership_returnsDashboard() {
        val route = resolvePostAuthRoute(
            membership = activeMembership,
            pendingInviteToken = null,
            acceptState = InviteJoinAcceptState.Idle,
        )

        assertEquals(AppRoute.Dashboard(householdId = "household-1"), route)
    }

    @Test
    fun resolvePostAuthRoute_withPendingInviteAndNoMembership_waitsForAccept() {
        val route = resolvePostAuthRoute(
            membership = HouseholdMembershipStatus.None,
            pendingInviteToken = "invite-token",
            acceptState = InviteJoinAcceptState.Idle,
        )

        assertNull(route)
        assertTrue(
            shouldBlockAuthenticatedShell(
                membership = HouseholdMembershipStatus.None,
                pendingInviteToken = "invite-token",
                acceptState = InviteJoinAcceptState.Idle,
            ),
        )
    }

    @Test
    fun resolvePostAuthRoute_withPendingInviteAndActiveMembership_bypassesHouseholdSetup() {
        val route = resolvePostAuthRoute(
            membership = activeMembership,
            pendingInviteToken = "invite-token",
            acceptState = InviteJoinAcceptState.Succeeded(householdName = "Our household"),
        )

        assertEquals(AppRoute.Dashboard(householdId = "household-1"), route)
        assertFalse(
            shouldBlockAuthenticatedShell(
                membership = activeMembership,
                pendingInviteToken = "invite-token",
                acceptState = InviteJoinAcceptState.Succeeded(householdName = "Our household"),
            ),
        )
    }
}
