package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.data.supabase.dto.ProfileInsertRow
import app.mymultiverse.kmp.data.supabase.dto.ProfileRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceInviteInsertRow
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
    private val pendingInvites = MutableStateFlow<List<SpaceInvite>>(emptyList())

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> =
        membersFlow(spaceId).asStateFlow()

    override fun observePendingInvites(): Flow<List<SpaceInvite>> = pendingInvites.asStateFlow()

    override suspend fun refreshMembers(spaceId: String, ownerId: String, ownerDisplayName: String) {
        val rows = client.postgrest["space_members"]
            .select(Columns.ALL) {
                filter { eq("space_id", spaceId) }
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
        runCatching {
            val profile = currentProfile() ?: run {
                pendingInvites.value = emptyList()
                return
            }
            val email = profile.email?.trim()?.lowercase().orEmpty()
            if (email.isEmpty()) {
                pendingInvites.value = emptyList()
                return
            }

            val rows = client.postgrest["space_invites"]
                .select(Columns.ALL) {
                    filter { eq("email", email) }
                }
                .decodeList<SpaceInviteRow>()
                .filter { row -> row.acceptedAt == null && row.declinedAt == null }

            val spaceIds = rows.map { it.spaceId }.distinct()
            val spaceNames = if (spaceIds.isEmpty()) {
                emptyMap()
            } else {
                client.postgrest["sharing_spaces"]
                    .select(Columns.ALL) {
                        filter { isIn("id", spaceIds) }
                    }
                    .decodeList<SharingSpaceRow>()
                    .associateBy({ it.id }, { it.name })
            }

            pendingInvites.value = rows.map { row ->
                SpaceInvite(
                    id = row.id,
                    spaceId = row.spaceId,
                    spaceName = spaceNames[row.spaceId] ?: row.spaceId,
                    email = row.email,
                    role = row.role.toSpaceMemberRole(),
                    expiresAtEpochMillis = row.expiresAt?.toEpochMillis(),
                )
            }.sortedBy { it.spaceName.lowercase() }
        }
    }

    override suspend fun addMemberByEmail(
        spaceId: String,
        email: String,
        role: SpaceMemberRole,
    ): Result<AddMemberResult> = runCatching {
        val trimmed = email.trim()
        require(trimmed.isNotEmpty()) { "member_email_required" }

        val profileId = findProfileIdByEmail(trimmed)
        val currentUserId = requireUserId()
        ensureProfile(currentUserId)

        if (profileId == null) {
            client.postgrest["space_invites"]
                .insert(
                    SpaceInviteInsertRow(
                        spaceId = spaceId,
                        email = trimmed.lowercase(),
                        role = role.wireName(),
                        invitedBy = currentUserId,
                    ),
                )
            return@runCatching AddMemberResult.InviteSent
        }

        if (profileId == currentUserId) {
            throw IllegalArgumentException("member_cannot_add_self")
        }

        client.postgrest["space_members"]
            .insert(
                SpaceMemberInsertRow(
                    spaceId = spaceId,
                    userId = profileId,
                    role = role.wireName(),
                ),
            )

        AddMemberResult.Added
    }

    override suspend fun removeMember(memberId: String): Result<Unit> = runCatching {
        require(!memberId.startsWith(OWNER_MEMBER_PREFIX)) { "cannot_remove_owner" }

        client.postgrest["space_members"]
            .delete {
                filter { eq("id", memberId) }
            }
        membersBySpace.values.forEach { flow ->
            flow.update { members -> members.filterNot { it.id == memberId } }
        }
    }

    override suspend fun acceptInvite(inviteId: String): Result<Unit> = runCatching {
        val parameters = buildJsonObject { put("p_invite_id", inviteId) }
        client.postgrest.rpc("accept_space_invite", parameters)
        refreshPendingInvites()
    }

    override suspend fun declineInvite(inviteId: String): Result<Unit> = runCatching {
        client.postgrest["space_invites"]
            .update(
                SpaceInviteUpdateRow(
                    declinedAt = Clock.System.now().toString(),
                ),
            ) {
                filter { eq("id", inviteId) }
            }
        refreshPendingInvites()
    }

    private suspend fun findProfileIdByEmail(email: String): String? {
        val parameters = buildJsonObject { put("p_email", email) }
        return client.postgrest
            .rpc("find_profile_id_by_email", parameters)
            .decodeSingleOrNull()
    }

    private suspend fun currentProfile(): ProfileRow? {
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        return client.postgrest["profiles"]
            .select(Columns.ALL) {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<ProfileRow>()
    }

    private suspend fun ensureProfile(userId: String) {
        val rpcResult = runCatching { client.postgrest.rpc("ensure_current_profile") }
        if (rpcResult.isSuccess) return

        val email = client.auth.currentUserOrNull()?.email
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

    private fun membersFlow(spaceId: String): MutableStateFlow<List<SpaceMember>> =
        membersBySpace.getOrPut(spaceId) { MutableStateFlow(emptyList()) }

    private suspend fun requireUserId(): String {
        client.auth.awaitInitialization()
        return client.auth.currentUserOrNull()?.id
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
