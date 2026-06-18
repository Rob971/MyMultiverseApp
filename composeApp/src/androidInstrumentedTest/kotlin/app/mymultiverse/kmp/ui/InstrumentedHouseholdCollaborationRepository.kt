package app.mymultiverse.kmp.ui

import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMember
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InstrumentedHouseholdCollaborationRepository : HouseholdCollaborationRepository {
    private val membersByHousehold = mutableMapOf<String, MutableStateFlow<List<HouseholdMember>>>()
    private val outboundInvites = MutableStateFlow<List<HouseholdInvite>>(emptyList())
    private val pendingInvites = MutableStateFlow<List<HouseholdInvite>>(emptyList())

    fun seedMembers(
        householdId: String,
        ownerId: String,
        ownerDisplayName: String,
        members: List<HouseholdMember>,
    ) {
        membersFlow(householdId).value = listOf(
            HouseholdMember(
                id = "owner-$ownerId",
                householdId = householdId,
                kind = HouseholdMemberKind.Person,
                displayName = ownerDisplayName,
                role = HouseholdMemberRole.Owner,
                referenceId = ownerId,
            ),
        ) + members
    }

    override fun observeMembers(householdId: String): Flow<List<HouseholdMember>> =
        membersFlow(householdId).asStateFlow()

    override fun observePendingInvites(): Flow<List<HouseholdInvite>> = pendingInvites.asStateFlow()

    override fun observeOutboundInvites(householdId: String): Flow<List<HouseholdInvite>> =
        outboundInvites.asStateFlow()

    override suspend fun refreshMembers(householdId: String, ownerId: String, ownerDisplayName: String) = Unit

    override suspend fun refreshPendingInvites() = Unit

    override suspend fun previewInvite(token: String): Result<app.mymultiverse.kmp.domain.model.sharing.HouseholdInvitePreview> =
        Result.failure(UnsupportedOperationException())

    override suspend fun refreshOutboundInvites(householdId: String) = Unit

    override suspend fun addMemberByEmail(
        householdId: String,
        email: String,
        role: HouseholdMemberRole,
    ): Result<AddMemberResult> = Result.success(AddMemberResult.InviteSent)

    override suspend fun removeMember(memberId: String): Result<Unit> = Result.success(Unit)

    override suspend fun acceptInvite(inviteId: String): Result<Unit> = Result.success(Unit)

    override suspend fun declineInvite(inviteId: String): Result<Unit> = Result.success(Unit)

    override suspend fun addDependant(householdId: String, displayName: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun removeDependant(dependantId: String): Result<Unit> = Result.success(Unit)

    private fun membersFlow(householdId: String): MutableStateFlow<List<HouseholdMember>> =
        membersByHousehold.getOrPut(householdId) { MutableStateFlow(emptyList()) }
}
