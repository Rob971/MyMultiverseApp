package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnconfiguredSpaceCollaborationRepository : SpaceCollaborationRepository {
    private val emptyMembers = MutableStateFlow<List<SpaceMember>>(emptyList())
    private val emptyGroups = MutableStateFlow<List<ContactGroup>>(emptyList())

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> = emptyMembers.asStateFlow()

    override fun observeGroups(): Flow<List<ContactGroup>> = emptyGroups.asStateFlow()

    override suspend fun refreshMembers(spaceId: String) = Unit

    override suspend fun refreshGroups() = Unit

    override suspend fun addMemberByEmail(spaceId: String, email: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun addGroupToSpace(spaceId: String, groupId: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun removeMember(memberId: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun createGroup(
        name: String,
        lifecycle: GroupLifecycle,
        expiresAtEpochMillis: Long?,
    ): Result<ContactGroup> = Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun addUserToGroup(groupId: String, email: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))
}
