package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMember
import app.mymultiverse.kmp.domain.repository.HouseholdCollaborationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnconfiguredHouseholdCollaborationRepository : HouseholdCollaborationRepository {
    private val emptyMembers = MutableStateFlow<List<HouseholdMember>>(emptyList())
    private val emptyInvites = MutableStateFlow<List<HouseholdInvite>>(emptyList())

    override fun observeMembers(householdId: String): Flow<List<HouseholdMember>> = emptyMembers.asStateFlow()

    override fun observePendingInvites(): Flow<List<HouseholdInvite>> = emptyInvites.asStateFlow()

    override fun observeOutboundInvites(householdId: String): Flow<List<HouseholdInvite>> = emptyInvites.asStateFlow()

    override suspend fun refreshMembers(householdId: String, ownerId: String, ownerDisplayName: String) = Unit

    override suspend fun refreshPendingInvites() = Unit

    override suspend fun refreshOutboundInvites(householdId: String) = Unit

    override suspend fun addMemberByEmail(
        householdId: String,
        email: String,
        role: app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole,
    ): Result<AddMemberResult> = Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun removeMember(memberId: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun acceptInvite(inviteId: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun declineInvite(inviteId: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun addDependant(householdId: String, displayName: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun removeDependant(dependantId: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))
}
