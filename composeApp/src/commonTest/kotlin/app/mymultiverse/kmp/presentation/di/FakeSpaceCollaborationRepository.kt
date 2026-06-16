package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import app.mymultiverse.kmp.domain.model.sharing.GroupMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberKind
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import app.mymultiverse.kmp.domain.sharing.activeOnly
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSpaceCollaborationRepository : SpaceCollaborationRepository {
    private val membersBySpace = mutableMapOf<String, MutableStateFlow<List<SpaceMember>>>()
    private val groupMembersByGroup = mutableMapOf<String, MutableStateFlow<List<GroupMember>>>()
    private val groups = MutableStateFlow<List<ContactGroup>>(emptyList())
    private val pendingInvites = MutableStateFlow<List<SpaceInvite>>(emptyList())

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> =
        membersFlow(spaceId).asStateFlow()

    override fun observeGroups(): Flow<List<ContactGroup>> = groups.asStateFlow()

    override fun observeGroupMembers(groupId: String): Flow<List<GroupMember>> =
        groupMembersFlow(groupId).asStateFlow()

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

    override suspend fun refreshGroups() = Unit

    override suspend fun refreshGroupMembers(groupId: String) = Unit

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

    override suspend fun addGroupToSpace(spaceId: String, groupId: String): Result<Unit> {
        val group = groups.value.firstOrNull { it.id == groupId }
            ?: return Result.failure(IllegalArgumentException("group_not_found"))
        val member = SpaceMember(
            id = "member-group-${membersFlow(spaceId).value.size + 1}",
            spaceId = spaceId,
            kind = SpaceMemberKind.Group,
            displayName = group.name,
            role = SpaceMemberRole.Editor,
            referenceId = groupId,
        )
        membersFlow(spaceId).update { it + member }
        return Result.success(Unit)
    }

    override suspend fun removeMember(memberId: String): Result<Unit> {
        membersBySpace.values.forEach { flow ->
            flow.update { members -> members.filterNot { it.id == memberId } }
        }
        return Result.success(Unit)
    }

    override suspend fun createGroup(
        name: String,
        lifecycle: GroupLifecycle,
        eventLabel: String?,
        startsAtEpochMillis: Long?,
        expiresAtEpochMillis: Long?,
    ): Result<ContactGroup> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("group_name_required"))
        if (lifecycle == GroupLifecycle.Event && expiresAtEpochMillis == null) {
            return Result.failure(IllegalArgumentException("event_expires_required"))
        }
        val created = ContactGroup(
            id = "group-${groups.value.size + 1}",
            name = trimmed,
            lifecycle = lifecycle,
            ownerId = "test-user",
            eventLabel = eventLabel,
            startsAtEpochMillis = startsAtEpochMillis,
            expiresAtEpochMillis = expiresAtEpochMillis,
        )
        groups.update { (it + created).activeOnly() }
        return Result.success(created)
    }

    override suspend fun addUserToGroup(groupId: String, email: String): Result<Unit> {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("member_email_required"))
        val member = GroupMember(
            id = "gm-${groupMembersFlow(groupId).value.size + 1}",
            groupId = groupId,
            userId = trimmed,
            displayName = trimmed,
        )
        groupMembersFlow(groupId).update { it + member }
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

    private fun groupMembersFlow(groupId: String): MutableStateFlow<List<GroupMember>> =
        groupMembersByGroup.getOrPut(groupId) { MutableStateFlow(emptyList()) }
}
