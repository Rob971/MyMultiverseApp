package app.mymultiverse.kmp.data.collaboration

import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
import app.mymultiverse.kmp.presentation.di.FakeSpaceCollaborationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollaborationInviteFlowTest {

    @Test
    fun inviteAcceptance_addsMemberAndRefreshesHousehold() = runTest {
        val collaboration = FakeSpaceCollaborationRepository()
        val household = FakeHouseholdRepository()

        collaboration.inboundProfileEmail = "invite@example.com"
        collaboration.addMemberByEmail(
            spaceId = "household-space-1",
            email = "invite@example.com",
            role = SpaceMemberRole.Editor,
        )
        collaboration.refreshPendingInvites()

        val inviteId = collaboration.observePendingInvites().first().single().id
        val acceptResult = collaboration.acceptInvite(inviteId)
        val householdResult = household.ensureHousehold()

        assertTrue(acceptResult.isSuccess)
        assertTrue(householdResult.isSuccess)
        assertEquals("household-space-1", householdResult.getOrThrow().id)
    }
}
