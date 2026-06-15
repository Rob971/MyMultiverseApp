package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
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
    private val groups = MutableStateFlow<List<ContactGroup>>(emptyList())

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> =
        membersFlow(spaceId).asStateFlow()

    override fun observeGroups(): Flow<List<ContactGroup>> = groups.asStateFlow()

    override suspend fun refreshMembers(spaceId: String) = Unit

    override suspend fun refreshGroups() = Unit

    override suspend fun addMemberByEmail(spaceId: String, email: String): Result<Unit> {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("member_email_required"))

        val member = SpaceMember(
            id = "member-${membersFlow(spaceId).value.size + 1}",
            spaceId = spaceId,
            kind = SpaceMemberKind.Person,
            displayName = trimmed,
            role = SpaceMemberRole.Editor,
            referenceId = trimmed,
        )
        membersFlow(spaceId).update { it + member }
        return Result.success(Unit)
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
        expiresAtEpochMillis: Long?,
    ): Result<ContactGroup> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("group_name_required"))
        val created = ContactGroup(
            id = "group-${groups.value.size + 1}",
            name = trimmed,
            lifecycle = lifecycle,
            ownerId = "test-user",
            expiresAtEpochMillis = expiresAtEpochMillis,
        )
        groups.update { it + created }
        return Result.success(created)
    }

    override suspend fun addUserToGroup(groupId: String, email: String): Result<Unit> =
        Result.success(Unit)

    private fun membersFlow(spaceId: String): MutableStateFlow<List<SpaceMember>> =
        membersBySpace.getOrPut(spaceId) { MutableStateFlow(emptyList()) }
}
