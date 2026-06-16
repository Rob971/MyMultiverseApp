package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberKind
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSpaceCollaborationRepository : SpaceCollaborationRepository {
    private val membersBySpace = mutableMapOf<String, MutableStateFlow<List<SpaceMember>>>()
    private val pendingInvites = MutableStateFlow<List<SpaceInvite>>(emptyList())

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> =
        membersFlow(spaceId).asStateFlow()

    override fun observePendingInvites(): Flow<List<SpaceInvite>> = pendingInvites.asStateFlow()

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

    override suspend fun refreshPendingInvites() = Unit

    override suspend fun addMemberByEmail(
        spaceId: String,
        email: String,
        role: SpaceMemberRole,
    ): Result<AddMemberResult> {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("member_email_required"))
        if (trimmed.contains("invite")) {
            pendingInvites.update {
                it + SpaceInvite(
                    id = "invite-${it.size + 1}",
                    spaceId = spaceId,
                    spaceName = "Test Space",
                    email = trimmed,
                    role = role,
                    expiresAtEpochMillis = null,
                )
            }
            return Result.success(AddMemberResult.InviteSent)
        }

        val member = SpaceMember(
            id = "member-${membersFlow(spaceId).value.size + 1}",
            spaceId = spaceId,
            kind = SpaceMemberKind.Person,
            displayName = trimmed,
            role = role,
            referenceId = trimmed,
        )
        membersFlow(spaceId).update { current -> current + member }
        return Result.success(AddMemberResult.Added)
    }

    override suspend fun removeMember(memberId: String): Result<Unit> {
        membersBySpace.values.forEach { flow ->
            flow.update { members -> members.filterNot { it.id == memberId } }
        }
        return Result.success(Unit)
    }

    override suspend fun acceptInvite(inviteId: String): Result<Unit> {
        pendingInvites.update { invites -> invites.filterNot { it.id == inviteId } }
        return Result.success(Unit)
    }

    override suspend fun declineInvite(inviteId: String): Result<Unit> {
        pendingInvites.update { invites -> invites.filterNot { it.id == inviteId } }
        return Result.success(Unit)
    }

    private fun membersFlow(spaceId: String): MutableStateFlow<List<SpaceMember>> =
        membersBySpace.getOrPut(spaceId) { MutableStateFlow(emptyList()) }
}
