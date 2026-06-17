package app.mymultiverse.kmp.data.collaboration

import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
import app.mymultiverse.kmp.presentation.di.FakeHouseholdCollaborationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollaborationInviteFlowTest {

    @Test
    fun inviteAcceptance_addsMemberAndRefreshesHousehold() = runTest {
        val collaboration = FakeHouseholdCollaborationRepository()
        val household = FakeHouseholdRepository()

        collaboration.inboundProfileEmail = "invite@example.com"
        collaboration.addMemberByEmail(
            householdId = "household-1",
            email = "invite@example.com",
            role = HouseholdMemberRole.Editor,
        )
        collaboration.refreshPendingInvites()

        val inviteId = collaboration.observePendingInvites().first().single().id
        val acceptResult = collaboration.acceptInvite(inviteId)
        val householdResult = household.ensureHousehold()

        assertTrue(acceptResult.isSuccess)
        assertTrue(householdResult.isSuccess)
        assertEquals("household-1", householdResult.getOrThrow().id)
    }
}
