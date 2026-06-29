package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.data.supabase.dto.HouseholdDependantRow
import app.mymultiverse.ammo.data.supabase.dto.HouseholdRpcDecoder
import app.mymultiverse.ammo.data.supabase.dto.ProfileInsertRow
import app.mymultiverse.ammo.data.supabase.dto.ProfileRow
import app.mymultiverse.ammo.data.supabase.dto.HouseholdInviteInsertRow
import app.mymultiverse.ammo.data.supabase.dto.HouseholdInvitePendingUpdateRow
import app.mymultiverse.ammo.data.supabase.dto.HouseholdInviteRow
import app.mymultiverse.ammo.data.supabase.dto.HouseholdInviteUpdateRow
import app.mymultiverse.ammo.data.supabase.dto.HouseholdMemberInsertRow
import app.mymultiverse.ammo.data.supabase.dto.HouseholdMemberRow
import app.mymultiverse.ammo.data.supabase.dto.HouseholdRow
import app.mymultiverse.ammo.domain.model.sharing.AddMemberResult
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvite
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.ammo.domain.sharing.emailsMatch
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.ammo.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.ammo.domain.sharing.activeInvites
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

class SupabaseHouseholdCollaborationRepository(
    private val client: SupabaseClient,
) : HouseholdCollaborationRepository {

    private val membersByHousehold = mutableMapOf<String, MutableStateFlow<List<HouseholdMember>>>()
    private val outboundInvitesByHousehold = mutableMapOf<String, MutableStateFlow<List<HouseholdInvite>>>()
    private val pendingInvites = MutableStateFlow<List<HouseholdInvite>>(emptyList())

    override fun observeMembers(householdId: String): Flow<List<HouseholdMember>> =
        membersFlow(householdId).asStateFlow()

    override fun observePendingInvites(): Flow<List<HouseholdInvite>> = pendingInvites.asStateFlow()

    override fun observeOutboundInvites(householdId: String): Flow<List<HouseholdInvite>> =
        outboundInvitesFlow(householdId).asStateFlow()

    override suspend fun refreshMembers(householdId: String, ownerId: String, ownerDisplayName: String) {
        val rows = client.postgrest["household_members"]
            .select(Columns.ALL) {
                filter { eq("household_id", householdId) }
            }
            .decodeList<HouseholdMemberRow>()
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
            HouseholdMember(
                id = row.id,
                householdId = row.householdId,
                kind = HouseholdMemberKind.Person,
                displayName = profile?.displayName ?: profile?.email ?: userId,
                role = row.role.toHouseholdMemberRole(),
                referenceId = userId,
            )
        }

        val ownerAlreadyListed = mapped.any { it.referenceId == ownerId }
        val withOwner = if (ownerAlreadyListed) {
            mapped
        } else {
            listOf(ownerMember(householdId, ownerId, ownerDisplayName)) + mapped
        }

        val dependantRows = client.postgrest["household_dependants"]
            .select(Columns.ALL) {
                filter { eq("household_id", householdId) }
            }
            .decodeList<HouseholdDependantRow>()
            .filter { it.removedAt == null }

        val dependants = dependantRows.map { row ->
            HouseholdMember(
                id = row.id,
                householdId = row.householdId,
                kind = HouseholdMemberKind.Dependant,
                displayName = row.displayName,
                role = HouseholdMemberRole.Viewer,
                referenceId = row.id,
            )
        }

        membersFlow(householdId).value = (withOwner + dependants).sortedWith(
            compareByDescending<HouseholdMember> { it.role == HouseholdMemberRole.Owner }
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
            client.postgrest.rpc("list_my_pending_household_invites"),
        )

        pendingInvites.value = rows
            .map { row ->
                HouseholdInvite(
                    id = row.id,
                    householdId = row.householdId,
                    householdName = row.householdName,
                    email = row.email,
                    role = row.role.toHouseholdMemberRole(),
                    expiresAtEpochMillis = row.expiresAt?.toEpochMillis(),
                )
            }
            .activeInvites()
            .sortedBy { it.householdName.lowercase() }
    }

    override suspend fun previewInvite(token: String): Result<HouseholdInvitePreview> = runCatching {
        val trimmedToken = token.trim()
        require(trimmedToken.isNotEmpty()) { CollaborationErrorCodes.INVITE_TOKEN_REQUIRED }

        val row = HouseholdRpcDecoder.decodeInvitePreview(
            client.postgrest.rpc(
                "preview_household_invite",
                buildJsonObject { put("p_token", trimmedToken) },
            ),
        )

        HouseholdInvitePreview(
            inviteId = row.inviteId,
            householdId = row.householdId,
            householdName = row.householdName,
            inviterName = row.inviterName,
            inviteeEmail = row.inviteeEmail.trim().lowercase(),
            role = row.role.toHouseholdMemberRole(),
            expiresAtEpochMillis = row.expiresAt?.toEpochMillis(),
        )
    }

    override suspend fun refreshOutboundInvites(householdId: String) {
        val rows = client.postgrest["household_invites"]
            .select(Columns.ALL) {
                filter { eq("household_id", householdId) }
            }
            .decodeList<HouseholdInviteRow>()
            .filter { row -> row.acceptedAt == null && row.declinedAt == null }
            .map { it.toHouseholdInvite(householdName = null) }
            .activeInvites()
            .sortedBy { it.email.lowercase() }

        outboundInvitesFlow(householdId).value = rows
    }

    override suspend fun addMemberByEmail(
        householdId: String,
        email: String,
        role: HouseholdMemberRole,
    ): Result<AddMemberResult> = runCatching {
        val trimmed = email.trim()
        require(trimmed.isNotEmpty()) { CollaborationErrorCodes.MEMBER_EMAIL_REQUIRED }
        require(role != HouseholdMemberRole.Owner) { CollaborationErrorCodes.INSUFFICIENT_ROLE }

        val currentUserId = requireUserId()
        ensureProfile(currentUserId)

        val row = HouseholdRpcDecoder.decodeInviteResult(
            client.postgrest.rpc(
                "invite_household_member",
                buildJsonObject {
                    put("p_household_id", householdId)
                    put("p_email", trimmed)
                    put("p_role", role.wireName())
                },
            ),
        )

        when {
            row.result == "added" -> {
                refreshMembers(householdId, currentUserId, currentProfileDisplayName(currentUserId))
                AddMemberResult.Added
            }
            row.result == "invited" || !row.inviteId.isNullOrBlank() -> {
                refreshOutboundInvites(householdId)
                val token = row.inviteToken?.trim().orEmpty().ifEmpty {
                    outboundInvitesFlow(householdId).value
                        .firstOrNull { invite -> emailsMatch(invite.email, trimmed) }
                        ?.inviteToken
                        .orEmpty()
                }
                require(token.isNotEmpty()) { "invite_token_missing" }
                AddMemberResult.InviteSent(inviteToken = token)
            }
            else -> throw IllegalStateException("invite_household_member_unexpected_result")
        }
    }

    override suspend fun removeMember(memberId: String): Result<Unit> = runCatching {
        require(!memberId.startsWith(OWNER_MEMBER_PREFIX)) { "cannot_remove_owner" }

        client.postgrest["household_members"]
            .delete {
                filter { eq("id", memberId) }
            }
        membersByHousehold.values.forEach { flow ->
            flow.update { members -> members.filterNot { it.id == memberId } }
        }
    }

    override suspend fun updateMemberRole(
        memberId: String,
        role: HouseholdMemberRole,
    ): Result<Unit> = runCatching {
        client.postgrest.rpc(
            "update_household_member_role",
            buildJsonObject {
                put("p_member_id", memberId)
                put("p_role", role.wireName())
            },
        )
        membersByHousehold.values.forEach { flow ->
            flow.update { members ->
                members.map { member ->
                    if (member.id == memberId) member.copy(role = role) else member
                }
            }
        }
    }

    override suspend fun acceptInvite(inviteId: String): Result<Unit> = runCatching {
        val currentUserId = requireUserId()
        ensureProfile(currentUserId)
        val parameters = buildJsonObject { put("p_invite_id", inviteId) }
        client.postgrest.rpc("accept_household_invite", parameters)
        refreshPendingInvites()
    }

    override suspend fun declineInvite(inviteId: String): Result<Unit> = runCatching {
        client.postgrest["household_invites"]
            .update(
                HouseholdInviteUpdateRow(
                    declinedAt = Clock.System.now().toString(),
                ),
            ) {
                filter { eq("id", inviteId) }
            }
        refreshPendingInvites()
    }

    override suspend fun addDependant(householdId: String, displayName: String): Result<Unit> = runCatching {
        val trimmed = displayName.trim()
        require(trimmed.isNotEmpty()) { "dependant_name_required" }
        client.postgrest.rpc(
            "add_household_dependant",
            buildJsonObject {
                put("p_household_id", householdId)
                put("p_display_name", trimmed)
            },
        )
    }

    override suspend fun removeDependant(dependantId: String): Result<Unit> = runCatching {
        client.postgrest.rpc(
            "remove_household_dependant",
            buildJsonObject { put("p_dependant_id", dependantId) },
        )
        membersByHousehold.values.forEach { flow ->
            flow.update { members -> members.filterNot { it.id == dependantId } }
        }
    }

    private suspend fun sendOrRefreshInvite(
        householdId: String,
        email: String,
        role: HouseholdMemberRole,
        invitedBy: String,
    ) {
        val insertResult = runCatching {
            client.postgrest["household_invites"]
                .insert(
                    HouseholdInviteInsertRow(
                        householdId = householdId,
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
                HouseholdInvitePendingUpdateRow(
                    role = role.wireName(),
                    invitedBy = invitedBy,
                ),
            ) {
                filter {
                    eq("household_id", householdId)
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

    private suspend fun loadHouseholdNames(householdIds: List<String>): Map<String, String> {
        if (householdIds.isEmpty()) return emptyMap()
        return client.postgrest["households"]
            .select(Columns.ALL) {
                filter { isIn("id", householdIds) }
            }
            .decodeList<HouseholdRow>()
            .associateBy({ it.id }, { it.name })
    }

    private fun membersFlow(householdId: String): MutableStateFlow<List<HouseholdMember>> =
        membersByHousehold.getOrPut(householdId) { MutableStateFlow(emptyList()) }

    private fun outboundInvitesFlow(householdId: String): MutableStateFlow<List<HouseholdInvite>> =
        outboundInvitesByHousehold.getOrPut(householdId) { MutableStateFlow(emptyList()) }

    private suspend fun requireUserId(): String {
        client.auth.awaitInitialization()
        return client.auth.currentUserOrNull()?.id
            ?: client.auth.currentSessionOrNull()?.user?.id
            ?: throw IllegalStateException("auth_required")
    }

    private fun ownerMember(householdId: String, ownerId: String, ownerDisplayName: String): HouseholdMember =
        HouseholdMember(
            id = "$OWNER_MEMBER_PREFIX$ownerId",
            householdId = householdId,
            kind = HouseholdMemberKind.Person,
            displayName = ownerDisplayName,
            role = HouseholdMemberRole.Owner,
            referenceId = ownerId,
        )

    private fun HouseholdInviteRow.toHouseholdInvite(householdName: String?): HouseholdInvite =
        HouseholdInvite(
            id = id,
            householdId = householdId,
            householdName = householdName ?: householdId,
            email = email,
            role = role.toHouseholdMemberRole(),
            expiresAtEpochMillis = expiresAt?.toEpochMillis(),
            inviteToken = token?.trim()?.takeIf { it.isNotEmpty() },
        )

    private fun HouseholdMemberRole.wireName(): String =
        when (this) {
            HouseholdMemberRole.Owner -> "owner"
            HouseholdMemberRole.Admin -> "admin"
            HouseholdMemberRole.Viewer -> "viewer"
            HouseholdMemberRole.Editor -> "editor"
        }

    private fun String.toHouseholdMemberRole(): HouseholdMemberRole =
        when (this) {
            "owner" -> HouseholdMemberRole.Owner
            "admin" -> HouseholdMemberRole.Admin
            "viewer" -> HouseholdMemberRole.Viewer
            else -> HouseholdMemberRole.Editor
        }

    private fun String.toEpochMillis(): Long? =
        runCatching { kotlinx.datetime.Instant.parse(this).toEpochMilliseconds() }.getOrNull()

    private companion object {
        const val OWNER_MEMBER_PREFIX = "owner-"
    }
}
