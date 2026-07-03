package app.mymultiverse.ammo.domain.repository

import app.mymultiverse.ammo.domain.model.sharing.AddMemberResult
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvite
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import kotlinx.coroutines.flow.Flow

interface HouseholdCollaborationRepository {
    fun observeMembers(householdId: String): Flow<List<HouseholdMember>>

    fun observePendingInvites(): Flow<List<HouseholdInvite>>

    fun observeOutboundInvites(householdId: String): Flow<List<HouseholdInvite>>

    suspend fun refreshMembers(householdId: String, ownerId: String, ownerDisplayName: String)

    suspend fun refreshPendingInvites()

    suspend fun previewInvite(token: String): Result<HouseholdInvitePreview>

    suspend fun refreshOutboundInvites(householdId: String)

    suspend fun addMemberByEmail(
        householdId: String,
        email: String,
        role: HouseholdMemberRole = HouseholdMemberRole.Editor,
    ): Result<AddMemberResult>

    suspend fun removeMember(memberId: String): Result<Unit>

    suspend fun updateMemberRole(
        memberId: String,
        role: HouseholdMemberRole,
    ): Result<Unit>

    suspend fun acceptInvite(inviteId: String): Result<Unit>

    suspend fun declineInvite(inviteId: String): Result<Unit>

    suspend fun addDependant(householdId: String, displayName: String): Result<Unit>

    suspend fun removeDependant(dependantId: String): Result<Unit>

    suspend fun nudgePartnersToUpdateGroceryList(
        householdId: String,
        weekKey: String,
    ): Result<Unit>

    suspend fun updateMemberAvatar(
        householdId: String,
        member: HouseholdMember,
        imageBytes: ByteArray,
        contentType: String,
    ): Result<Unit>
}
