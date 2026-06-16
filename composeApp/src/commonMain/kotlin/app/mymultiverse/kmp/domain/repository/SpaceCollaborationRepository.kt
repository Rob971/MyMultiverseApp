package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import app.mymultiverse.kmp.domain.model.sharing.GroupMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import kotlinx.coroutines.flow.Flow

interface SpaceCollaborationRepository {
    fun observeMembers(spaceId: String): Flow<List<SpaceMember>>

    fun observeGroups(): Flow<List<ContactGroup>>

    fun observeGroupMembers(groupId: String): Flow<List<GroupMember>>

    fun observePendingInvites(): Flow<List<SpaceInvite>>

    suspend fun refreshMembers(spaceId: String, ownerId: String, ownerDisplayName: String)

    suspend fun refreshGroups()

    suspend fun refreshGroupMembers(groupId: String)

    suspend fun refreshPendingInvites()

    suspend fun addMemberByEmail(
        spaceId: String,
        email: String,
        role: SpaceMemberRole = SpaceMemberRole.Editor,
    ): Result<AddMemberResult>

    suspend fun addGroupToSpace(spaceId: String, groupId: String): Result<Unit>

    suspend fun removeMember(memberId: String): Result<Unit>

    suspend fun createGroup(
        name: String,
        lifecycle: GroupLifecycle,
        eventLabel: String? = null,
        startsAtEpochMillis: Long? = null,
        expiresAtEpochMillis: Long? = null,
    ): Result<ContactGroup>

    suspend fun addUserToGroup(groupId: String, email: String): Result<Unit>

    suspend fun acceptInvite(inviteId: String): Result<Unit>

    suspend fun declineInvite(inviteId: String): Result<Unit>
}
