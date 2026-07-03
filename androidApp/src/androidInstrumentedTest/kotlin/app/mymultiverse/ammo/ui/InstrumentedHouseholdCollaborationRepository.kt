package app.mymultiverse.ammo.ui

import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvite
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.ammo.domain.model.sharing.AddMemberResult
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

    override suspend fun previewInvite(token: String): Result<app.mymultiverse.ammo.domain.model.sharing.HouseholdInvitePreview> =
        previewInviteResult ?: Result.failure(UnsupportedOperationException())

    var previewInviteResult: Result<app.mymultiverse.ammo.domain.model.sharing.HouseholdInvitePreview>? = null

    override suspend fun refreshOutboundInvites(householdId: String) = Unit

    override suspend fun addMemberByEmail(
        householdId: String,
        email: String,
        role: HouseholdMemberRole,
    ): Result<AddMemberResult> = Result.success(AddMemberResult.InviteSent(inviteToken = "instrumented-invite-token"))

    override suspend fun removeMember(memberId: String): Result<Unit> = Result.success(Unit)

    var lastRoleUpdate: Pair<String, HouseholdMemberRole>? = null

    override suspend fun updateMemberRole(
        memberId: String,
        role: HouseholdMemberRole,
    ): Result<Unit> {
        lastRoleUpdate = memberId to role
        membersByHousehold.forEach { (_, flow) ->
            flow.value = flow.value.map { member ->
                if (member.id == memberId) member.copy(role = role) else member
            }
        }
        return Result.success(Unit)
    }

    override suspend fun acceptInvite(inviteId: String): Result<Unit> = Result.success(Unit)

    override suspend fun declineInvite(inviteId: String): Result<Unit> = Result.success(Unit)

    override suspend fun addDependant(householdId: String, displayName: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun removeDependant(dependantId: String): Result<Unit> = Result.success(Unit)

    override suspend fun nudgePartnersToUpdateGroceryList(
        householdId: String,
        weekKey: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun updateMemberAvatar(
        householdId: String,
        member: HouseholdMember,
        imageBytes: ByteArray,
        contentType: String,
    ): Result<Unit> = Result.success(Unit)

    private fun membersFlow(householdId: String): MutableStateFlow<List<HouseholdMember>> =
        membersByHousehold.getOrPut(householdId) { MutableStateFlow(emptyList()) }
}
