package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite
import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMember
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.kmp.domain.sharing.emailsMatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeHouseholdCollaborationRepository : HouseholdCollaborationRepository {
    private val membersByHousehold = mutableMapOf<String, MutableStateFlow<List<HouseholdMember>>>()
    private val outboundInvitesByHousehold = mutableMapOf<String, MutableStateFlow<List<HouseholdInvite>>>()
    private val pendingInvites = MutableStateFlow<List<HouseholdInvite>>(emptyList())
    var inboundProfileEmail: String = "invitee@example.com"
    var addMemberFailure: Throwable? = null
    var addDependantFailure: Throwable? = null
    var emailsAlreadyInAnotherHousehold: Set<String> = emptySet()

    private fun createInvite(householdId: String, email: String, role: HouseholdMemberRole): HouseholdInvite =
        HouseholdInvite(
            id = "invite-${outboundInvitesFlow(householdId).value.size + 1}",
            householdId = householdId,
            householdName = "Test Household",
            email = email.lowercase(),
            role = role,
            expiresAtEpochMillis = null,
        )

    override fun observeMembers(householdId: String): Flow<List<HouseholdMember>> =
        membersFlow(householdId).asStateFlow()

    override fun observePendingInvites(): Flow<List<HouseholdInvite>> = pendingInvites.asStateFlow()

    override fun observeOutboundInvites(householdId: String): Flow<List<HouseholdInvite>> =
        outboundInvitesFlow(householdId).asStateFlow()

    override suspend fun refreshMembers(householdId: String, ownerId: String, ownerDisplayName: String) {
        val current = membersFlow(householdId).value.filterNot { it.id.startsWith("owner-") }
        membersFlow(householdId).value = listOf(
            HouseholdMember(
                id = "owner-$ownerId",
                householdId = householdId,
                kind = HouseholdMemberKind.Person,
                displayName = ownerDisplayName,
                role = HouseholdMemberRole.Owner,
                referenceId = ownerId,
            ),
        ) + current
    }

    var previewInviteResult: Result<HouseholdInvitePreview>? = null
    var previewInviteCalls: Int = 0
        private set
    var acceptInviteResult: Result<Unit>? = null
    var acceptInviteCalls: Int = 0
        private set

    override suspend fun previewInvite(token: String): Result<HouseholdInvitePreview> {
        previewInviteCalls += 1
        return previewInviteResult ?: Result.success(
            HouseholdInvitePreview(
                inviteId = "invite-preview",
                householdId = "household-1",
                householdName = "Test Household",
                inviterName = "Alex",
                inviteeEmail = inboundProfileEmail,
                role = HouseholdMemberRole.Editor,
                expiresAtEpochMillis = null,
            ),
        )
    }

    override suspend fun refreshPendingInvites() {
        pendingInvites.value = outboundInvitesByHousehold.values
            .flatMap { it.value }
            .filter { invite -> emailsMatch(invite.email, inboundProfileEmail) }
    }

    override suspend fun refreshOutboundInvites(householdId: String) {
        outboundInvitesFlow(householdId).value = outboundInvitesFlow(householdId).value
    }

    override suspend fun addMemberByEmail(
        householdId: String,
        email: String,
        role: HouseholdMemberRole,
    ): Result<AddMemberResult> {
        addMemberFailure?.let { return Result.failure(it) }
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("member_email_required"))
        if (emailsAlreadyInAnotherHousehold.any { emailsMatch(it, trimmed) }) {
            return Result.failure(IllegalStateException("invitee_household_already_active"))
        }

        val invite = createInvite(householdId, trimmed, role)
        outboundInvitesFlow(householdId).update { current ->
            current.filterNot { emailsMatch(it.email, trimmed) } + invite
        }
        return Result.success(AddMemberResult.InviteSent)
    }

    override suspend fun removeMember(memberId: String): Result<Unit> {
        membersByHousehold.values.forEach { flow ->
            flow.update { members -> members.filterNot { it.id == memberId } }
        }
        return Result.success(Unit)
    }

    override suspend fun acceptInvite(inviteId: String): Result<Unit> {
        acceptInviteCalls++
        acceptInviteResult?.let { return it }
        val invite = pendingInvites.value.firstOrNull { it.id == inviteId }
            ?: outboundInvitesByHousehold.values.flatMap { it.value }.firstOrNull { it.id == inviteId }
            ?: return Result.failure(IllegalStateException("invite_not_found"))

        outboundInvitesByHousehold.values.forEach { flow ->
            flow.update { invites -> invites.filterNot { it.id == inviteId } }
        }
        pendingInvites.update { invites -> invites.filterNot { it.id == inviteId } }

        val member = HouseholdMember(
            id = "member-${membersFlow(invite.householdId).value.size + 1}",
            householdId = invite.householdId,
            kind = HouseholdMemberKind.Person,
            displayName = invite.email,
            role = invite.role,
            referenceId = invite.email,
        )
        membersFlow(invite.householdId).update { current -> current + member }
        return Result.success(Unit)
    }

    override suspend fun declineInvite(inviteId: String): Result<Unit> {
        outboundInvitesByHousehold.values.forEach { flow ->
            flow.update { invites -> invites.filterNot { it.id == inviteId } }
        }
        pendingInvites.update { invites -> invites.filterNot { it.id == inviteId } }
        return Result.success(Unit)
    }

    override suspend fun addDependant(householdId: String, displayName: String): Result<Unit> {
        addDependantFailure?.let { return Result.failure(it) }
        val trimmed = displayName.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("dependant_name_required"))
        val member = HouseholdMember(
            id = "dependant-${membersFlow(householdId).value.size + 1}",
            householdId = householdId,
            kind = HouseholdMemberKind.Dependant,
            displayName = trimmed,
            role = HouseholdMemberRole.Viewer,
            referenceId = "dependant-${membersFlow(householdId).value.size + 1}",
        )
        membersFlow(householdId).update { current -> current + member }
        return Result.success(Unit)
    }

    override suspend fun removeDependant(dependantId: String): Result<Unit> = removeMember(dependantId)

    private fun membersFlow(householdId: String): MutableStateFlow<List<HouseholdMember>> =
        membersByHousehold.getOrPut(householdId) { MutableStateFlow(emptyList()) }

    private fun outboundInvitesFlow(householdId: String): MutableStateFlow<List<HouseholdInvite>> =
        outboundInvitesByHousehold.getOrPut(householdId) { MutableStateFlow(emptyList()) }

    fun latestOutboundInvite(householdId: String): HouseholdInvite? =
        outboundInvitesByHousehold[householdId]?.value?.lastOrNull()

    fun seedMember(
        householdId: String,
        member: HouseholdMember,
        ownerId: String,
        ownerDisplayName: String,
    ) {
        membersFlow(householdId).value = listOf(
            HouseholdMember(
                id = "owner-$ownerId",
                householdId = householdId,
                kind = HouseholdMemberKind.Person,
                displayName = ownerDisplayName,
                role = HouseholdMemberRole.Owner,
                referenceId = ownerId,
            ),
            member,
        )
    }
}
