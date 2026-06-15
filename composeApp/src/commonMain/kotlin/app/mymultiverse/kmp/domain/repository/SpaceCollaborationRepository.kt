package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import kotlinx.coroutines.flow.Flow

interface SpaceCollaborationRepository {
    fun observeMembers(spaceId: String): Flow<List<SpaceMember>>

    fun observeGroups(): Flow<List<ContactGroup>>

    suspend fun refreshMembers(spaceId: String)

    suspend fun refreshGroups()

    suspend fun addMemberByEmail(spaceId: String, email: String): Result<Unit>

    suspend fun addGroupToSpace(spaceId: String, groupId: String): Result<Unit>

    suspend fun removeMember(memberId: String): Result<Unit>

    suspend fun createGroup(
        name: String,
        lifecycle: GroupLifecycle,
        expiresAtEpochMillis: Long? = null,
    ): Result<ContactGroup>

    suspend fun addUserToGroup(groupId: String, email: String): Result<Unit>
}
