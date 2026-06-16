package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnconfiguredSpaceCollaborationRepository : SpaceCollaborationRepository {
    private val emptyMembers = MutableStateFlow<List<SpaceMember>>(emptyList())
    private val emptyInvites = MutableStateFlow<List<SpaceInvite>>(emptyList())

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> = emptyMembers.asStateFlow()

    override fun observePendingInvites(): Flow<List<SpaceInvite>> = emptyInvites.asStateFlow()

    override fun observeOutboundInvites(spaceId: String): Flow<List<SpaceInvite>> = emptyInvites.asStateFlow()

    override suspend fun refreshMembers(spaceId: String, ownerId: String, ownerDisplayName: String) = Unit

    override suspend fun refreshPendingInvites() = Unit

    override suspend fun refreshOutboundInvites(spaceId: String) = Unit

    override suspend fun addMemberByEmail(
        spaceId: String,
        email: String,
        role: app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole,
    ): Result<AddMemberResult> = Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun removeMember(memberId: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun acceptInvite(inviteId: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun declineInvite(inviteId: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))
}
