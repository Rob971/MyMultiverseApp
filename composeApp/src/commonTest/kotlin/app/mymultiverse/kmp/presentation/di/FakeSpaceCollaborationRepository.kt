package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberKind
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import app.mymultiverse.kmp.domain.sharing.emailsMatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSpaceCollaborationRepository : SpaceCollaborationRepository {
    private val membersBySpace = mutableMapOf<String, MutableStateFlow<List<SpaceMember>>>()
    private val outboundInvitesBySpace = mutableMapOf<String, MutableStateFlow<List<SpaceInvite>>>()
    private val pendingInvites = MutableStateFlow<List<SpaceInvite>>(emptyList())
    var inboundProfileEmail: String = "invitee@example.com"
    var addMemberFailure: Throwable? = null
    var emailsAlreadyInAnotherHousehold: Set<String> = emptySet()

    private fun createInvite(spaceId: String, email: String, role: SpaceMemberRole): SpaceInvite =
        SpaceInvite(
            id = "invite-${outboundInvitesFlow(spaceId).value.size + 1}",
            spaceId = spaceId,
            spaceName = "Test Space",
            email = email.lowercase(),
            role = role,
            expiresAtEpochMillis = null,
        )

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> =
        membersFlow(spaceId).asStateFlow()

    override fun observePendingInvites(): Flow<List<SpaceInvite>> = pendingInvites.asStateFlow()

    override fun observeOutboundInvites(spaceId: String): Flow<List<SpaceInvite>> =
        outboundInvitesFlow(spaceId).asStateFlow()

    override suspend fun refreshMembers(spaceId: String, ownerId: String, ownerDisplayName: String) {
        val current = membersFlow(spaceId).value.filterNot { it.id.startsWith("owner-") }
        membersFlow(spaceId).value = listOf(
            SpaceMember(
                id = "owner-$ownerId",
                spaceId = spaceId,
                kind = SpaceMemberKind.Person,
                displayName = ownerDisplayName,
                role = SpaceMemberRole.Owner,
                referenceId = ownerId,
            ),
        ) + current
    }

    override suspend fun refreshPendingInvites() {
        pendingInvites.value = outboundInvitesBySpace.values
            .flatMap { it.value }
            .filter { invite -> emailsMatch(invite.email, inboundProfileEmail) }
    }

    override suspend fun refreshOutboundInvites(spaceId: String) {
        outboundInvitesFlow(spaceId).value = outboundInvitesFlow(spaceId).value
    }

    override suspend fun addMemberByEmail(
        spaceId: String,
        email: String,
        role: SpaceMemberRole,
    ): Result<AddMemberResult> {
        addMemberFailure?.let { return Result.failure(it) }
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("member_email_required"))
        if (emailsAlreadyInAnotherHousehold.any { emailsMatch(it, trimmed) }) {
            return Result.failure(IllegalStateException("invitee_household_already_active"))
        }

        val invite = createInvite(spaceId, trimmed, role)
        outboundInvitesFlow(spaceId).update { current ->
            current.filterNot { emailsMatch(it.email, trimmed) } + invite
        }
        return Result.success(AddMemberResult.InviteSent)
    }

    override suspend fun removeMember(memberId: String): Result<Unit> {
        membersBySpace.values.forEach { flow ->
            flow.update { members -> members.filterNot { it.id == memberId } }
        }
        return Result.success(Unit)
    }

    override suspend fun acceptInvite(inviteId: String): Result<Unit> {
        val invite = pendingInvites.value.firstOrNull { it.id == inviteId }
            ?: outboundInvitesBySpace.values.flatMap { it.value }.firstOrNull { it.id == inviteId }
            ?: return Result.failure(IllegalStateException("invite_not_found"))

        outboundInvitesBySpace.values.forEach { flow ->
            flow.update { invites -> invites.filterNot { it.id == inviteId } }
        }
        pendingInvites.update { invites -> invites.filterNot { it.id == inviteId } }

        val member = SpaceMember(
            id = "member-${membersFlow(invite.spaceId).value.size + 1}",
            spaceId = invite.spaceId,
            kind = SpaceMemberKind.Person,
            displayName = invite.email,
            role = invite.role,
            referenceId = invite.email,
        )
        membersFlow(invite.spaceId).update { current -> current + member }
        return Result.success(Unit)
    }

    override suspend fun declineInvite(inviteId: String): Result<Unit> {
        outboundInvitesBySpace.values.forEach { flow ->
            flow.update { invites -> invites.filterNot { it.id == inviteId } }
        }
        pendingInvites.update { invites -> invites.filterNot { it.id == inviteId } }
        return Result.success(Unit)
    }

    private fun membersFlow(spaceId: String): MutableStateFlow<List<SpaceMember>> =
        membersBySpace.getOrPut(spaceId) { MutableStateFlow(emptyList()) }

    private fun outboundInvitesFlow(spaceId: String): MutableStateFlow<List<SpaceInvite>> =
        outboundInvitesBySpace.getOrPut(spaceId) { MutableStateFlow(emptyList()) }

    fun seedMember(
        spaceId: String,
        member: SpaceMember,
        ownerId: String,
        ownerDisplayName: String,
    ) {
        membersFlow(spaceId).value = listOf(
            SpaceMember(
                id = "owner-$ownerId",
                spaceId = spaceId,
                kind = SpaceMemberKind.Person,
                displayName = ownerDisplayName,
                role = SpaceMemberRole.Owner,
                referenceId = ownerId,
            ),
            member,
        )
    }
}
