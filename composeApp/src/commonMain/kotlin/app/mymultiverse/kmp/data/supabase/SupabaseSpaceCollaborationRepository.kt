package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.data.supabase.dto.HouseholdRpcDecoder
import app.mymultiverse.kmp.data.supabase.dto.ProfileInsertRow
import app.mymultiverse.kmp.data.supabase.dto.ProfileRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceInviteInsertRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceInvitePendingUpdateRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceInviteRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceInviteUpdateRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceMemberInsertRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceMemberRow
import app.mymultiverse.kmp.data.supabase.dto.SharingSpaceRow
import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberKind
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import app.mymultiverse.kmp.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.kmp.domain.sharing.activeInvites
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseSpaceCollaborationRepository(
    private val client: SupabaseClient,
) : SpaceCollaborationRepository {

    private val membersBySpace = mutableMapOf<String, MutableStateFlow<List<SpaceMember>>>()
    private val outboundInvitesBySpace = mutableMapOf<String, MutableStateFlow<List<SpaceInvite>>>()
    private val pendingInvites = MutableStateFlow<List<SpaceInvite>>(emptyList())

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> =
        membersFlow(spaceId).asStateFlow()

    override fun observePendingInvites(): Flow<List<SpaceInvite>> = pendingInvites.asStateFlow()

    override fun observeOutboundInvites(spaceId: String): Flow<List<SpaceInvite>> =
        outboundInvitesFlow(spaceId).asStateFlow()

    override suspend fun refreshMembers(spaceId: String, ownerId: String, ownerDisplayName: String) {
        val rows = client.postgrest["household_members"]
            .select(Columns.ALL) {
                filter { eq("household_id", spaceId) }
            }
            .decodeList<SpaceMemberRow>()
            .filter { it.userId != null }

        val profileIds = rows.mapNotNull { it.userId }.distinct()
        val profiles = if (profileIds.isEmpty()) {
            emptyMap()
        } else {
            client.postgrest["profiles"]
                .select(Columns.ALL) {
                    filter { isIn("id", profileIds) }
                }
                .decodeList<ProfileRow>()
                .associateBy { it.id }
        }

        val mapped = rows.map { row ->
            val userId = requireNotNull(row.userId)
            val profile = profiles[userId]
            SpaceMember(
                id = row.id,
                spaceId = row.spaceId,
                kind = SpaceMemberKind.Person,
                displayName = profile?.displayName ?: profile?.email ?: userId,
                role = row.role.toSpaceMemberRole(),
                referenceId = userId,
            )
        }

        val ownerAlreadyListed = mapped.any { it.referenceId == ownerId }
        val withOwner = if (ownerAlreadyListed) {
            mapped
        } else {
            listOf(ownerMember(spaceId, ownerId, ownerDisplayName)) + mapped
        }

        membersFlow(spaceId).value = withOwner.sortedWith(
            compareByDescending<SpaceMember> { it.role == SpaceMemberRole.Owner }
                .thenBy { it.displayName.lowercase() },
        )
    }

    override suspend fun refreshPendingInvites() {
        val currentUserId = runCatching { requireUserId() }.getOrNull() ?: run {
            pendingInvites.value = emptyList()
            return
        }
        ensureProfile(currentUserId)

        val rows = HouseholdRpcDecoder.decodePendingInvites(
            client.postgrest.rpc("list_my_pending_space_invites"),
        )

        pendingInvites.value = rows
            .map { row ->
                SpaceInvite(
                    id = row.id,
                    spaceId = row.spaceId,
                    spaceName = row.spaceName,
                    email = row.email,
                    role = row.role.toSpaceMemberRole(),
                    expiresAtEpochMillis = row.expiresAt?.toEpochMillis(),
                )
            }
            .activeInvites()
            .sortedBy { it.spaceName.lowercase() }
    }

    override suspend fun refreshOutboundInvites(spaceId: String) {
        val rows = client.postgrest["household_invites"]
            .select(Columns.ALL) {
                filter { eq("household_id", spaceId) }
            }
            .decodeList<SpaceInviteRow>()
            .filter { row -> row.acceptedAt == null && row.declinedAt == null }
            .map { it.toSpaceInvite(spaceName = null) }
            .activeInvites()
            .sortedBy { it.email.lowercase() }

        outboundInvitesFlow(spaceId).value = rows
    }

    override suspend fun addMemberByEmail(
        spaceId: String,
        email: String,
        role: SpaceMemberRole,
    ): Result<AddMemberResult> = runCatching {
        val trimmed = email.trim()
        require(trimmed.isNotEmpty()) { CollaborationErrorCodes.MEMBER_EMAIL_REQUIRED }
        require(role != SpaceMemberRole.Owner) { CollaborationErrorCodes.INSUFFICIENT_ROLE }

        val currentUserId = requireUserId()
        ensureProfile(currentUserId)

        val row = HouseholdRpcDecoder.decodeInviteResult(
            client.postgrest.rpc(
                "invite_space_member",
                buildJsonObject {
                    put("p_space_id", spaceId)
                    put("p_email", trimmed)
                    put("p_role", role.wireName())
                },
            ),
        )

        when (row.result) {
            "invited" -> {
                refreshOutboundInvites(spaceId)
                AddMemberResult.InviteSent
            }
            "added" -> {
                refreshMembers(spaceId, currentUserId, currentProfileDisplayName(currentUserId))
                AddMemberResult.Added
            }
            else -> throw IllegalStateException("invite_space_member_unexpected_result")
        }
    }

    override suspend fun removeMember(memberId: String): Result<Unit> = runCatching {
        require(!memberId.startsWith(OWNER_MEMBER_PREFIX)) { "cannot_remove_owner" }

        client.postgrest["household_members"]
            .delete {
                filter { eq("id", memberId) }
            }
        membersBySpace.values.forEach { flow ->
            flow.update { members -> members.filterNot { it.id == memberId } }
        }
    }

    override suspend fun acceptInvite(inviteId: String): Result<Unit> = runCatching {
        val currentUserId = requireUserId()
        ensureProfile(currentUserId)
        val parameters = buildJsonObject { put("p_invite_id", inviteId) }
        client.postgrest.rpc("accept_space_invite", parameters)
        refreshPendingInvites()
    }

    override suspend fun declineInvite(inviteId: String): Result<Unit> = runCatching {
        client.postgrest["household_invites"]
            .update(
                SpaceInviteUpdateRow(
                    declinedAt = Clock.System.now().toString(),
                ),
            ) {
                filter { eq("id", inviteId) }
            }
        refreshPendingInvites()
    }

    private suspend fun sendOrRefreshInvite(
        spaceId: String,
        email: String,
        role: SpaceMemberRole,
        invitedBy: String,
    ) {
        val insertResult = runCatching {
            client.postgrest["household_invites"]
                .insert(
                    SpaceInviteInsertRow(
                        spaceId = spaceId,
                        email = email,
                        role = role.wireName(),
                        invitedBy = invitedBy,
                    ),
                )
        }
        if (insertResult.isSuccess) return

        val message = insertResult.exceptionOrNull()?.message.orEmpty().lowercase()
        if (!message.contains("duplicate") && !message.contains("23505") && !message.contains("unique")) {
            throw insertResult.exceptionOrNull() ?: IllegalStateException("invite_insert_failed")
        }

        client.postgrest["household_invites"]
            .update(
                SpaceInvitePendingUpdateRow(
                    role = role.wireName(),
                    invitedBy = invitedBy,
                ),
            ) {
                filter {
                    eq("household_id", spaceId)
                    eq("email", email)
                }
            }
    }

    private suspend fun findProfileIdByEmail(email: String): String? {
        val parameters = buildJsonObject { put("p_email", email) }
        return client.postgrest
            .rpc("find_profile_id_by_email", parameters)
            .decodeSingleOrNull()
    }

    private suspend fun resolveProfileEmail(): String? {
        client.auth.awaitInitialization()
        val userId = requireUserId()
        ensureProfile(userId)
        val profileEmail = currentProfile()?.email?.trim().orEmpty()
        if (profileEmail.isNotEmpty()) return profileEmail

        return client.auth.currentUserOrNull()?.email?.trim()
            ?: client.auth.currentSessionOrNull()?.user?.email?.trim()
    }

    private suspend fun currentProfile(): ProfileRow? {
        val userId = client.auth.currentUserOrNull()?.id
            ?: client.auth.currentSessionOrNull()?.user?.id
            ?: return null
        return client.postgrest["profiles"]
            .select(Columns.ALL) {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<ProfileRow>()
    }

    private suspend fun currentProfileDisplayName(userId: String): String {
        val profile = currentProfile()
        return profile?.displayName?.takeIf { it.isNotBlank() }
            ?: profile?.email?.takeIf { it.isNotBlank() }
            ?: userId
    }

    private suspend fun ensureProfile(userId: String) {
        val rpcResult = runCatching { client.postgrest.rpc("ensure_current_profile") }
        if (rpcResult.isSuccess) return

        val email = client.auth.currentUserOrNull()?.email
            ?: client.auth.currentSessionOrNull()?.user?.email
        client.postgrest["profiles"]
            .upsert(
                ProfileInsertRow(
                    id = userId,
                    email = email,
                    displayName = email?.substringBefore("@"),
                ),
            ) {
                onConflict = "id"
            }
    }

    private suspend fun loadSpaceNames(spaceIds: List<String>): Map<String, String> {
        if (spaceIds.isEmpty()) return emptyMap()
        return client.postgrest["households"]
            .select(Columns.ALL) {
                filter { isIn("id", spaceIds) }
            }
            .decodeList<SharingSpaceRow>()
            .associateBy({ it.id }, { it.name })
    }

    private fun membersFlow(spaceId: String): MutableStateFlow<List<SpaceMember>> =
        membersBySpace.getOrPut(spaceId) { MutableStateFlow(emptyList()) }

    private fun outboundInvitesFlow(spaceId: String): MutableStateFlow<List<SpaceInvite>> =
        outboundInvitesBySpace.getOrPut(spaceId) { MutableStateFlow(emptyList()) }

    private suspend fun requireUserId(): String {
        client.auth.awaitInitialization()
        return client.auth.currentUserOrNull()?.id
            ?: client.auth.currentSessionOrNull()?.user?.id
            ?: throw IllegalStateException("auth_required")
    }

    private fun ownerMember(spaceId: String, ownerId: String, ownerDisplayName: String): SpaceMember =
        SpaceMember(
            id = "$OWNER_MEMBER_PREFIX$ownerId",
            spaceId = spaceId,
            kind = SpaceMemberKind.Person,
            displayName = ownerDisplayName,
            role = SpaceMemberRole.Owner,
            referenceId = ownerId,
        )

    private fun SpaceInviteRow.toSpaceInvite(spaceName: String?): SpaceInvite =
        SpaceInvite(
            id = id,
            spaceId = spaceId,
            spaceName = spaceName ?: spaceId,
            email = email,
            role = role.toSpaceMemberRole(),
            expiresAtEpochMillis = expiresAt?.toEpochMillis(),
        )

    private fun SpaceMemberRole.wireName(): String =
        when (this) {
            SpaceMemberRole.Owner -> "owner"
            SpaceMemberRole.Viewer -> "viewer"
            SpaceMemberRole.Editor -> "editor"
        }

    private fun String.toSpaceMemberRole(): SpaceMemberRole =
        when (this) {
            "owner" -> SpaceMemberRole.Owner
            "viewer" -> SpaceMemberRole.Viewer
            else -> SpaceMemberRole.Editor
        }

    private fun String.toEpochMillis(): Long? =
        runCatching { kotlinx.datetime.Instant.parse(this).toEpochMilliseconds() }.getOrNull()

    private companion object {
        const val OWNER_MEMBER_PREFIX = "owner-"
    }
}
