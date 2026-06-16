package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.data.supabase.dto.ContactGroupInsertRow
import app.mymultiverse.kmp.data.supabase.dto.ContactGroupRow
import app.mymultiverse.kmp.data.supabase.dto.GroupMemberInsertRow
import app.mymultiverse.kmp.data.supabase.dto.GroupMemberRow
import app.mymultiverse.kmp.data.supabase.dto.ProfileRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceInviteInsertRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceInviteRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceInviteUpdateRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceMemberInsertRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceMemberRow
import app.mymultiverse.kmp.data.supabase.dto.SharingSpaceRow
import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import app.mymultiverse.kmp.domain.model.sharing.GroupMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberKind
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import app.mymultiverse.kmp.domain.sharing.activeOnly
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseSpaceCollaborationRepository(
    private val client: SupabaseClient,
) : SpaceCollaborationRepository {

    private val membersBySpace = mutableMapOf<String, MutableStateFlow<List<SpaceMember>>>()
    private val groupMembersByGroup = mutableMapOf<String, MutableStateFlow<List<GroupMember>>>()
    private val groups = MutableStateFlow<List<ContactGroup>>(emptyList())
    private val pendingInvites = MutableStateFlow<List<SpaceInvite>>(emptyList())

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> =
        membersFlow(spaceId).asStateFlow()

    override fun observeGroups(): Flow<List<ContactGroup>> = groups.asStateFlow()

    override fun observeGroupMembers(groupId: String): Flow<List<GroupMember>> =
        groupMembersFlow(groupId).asStateFlow()

    override fun observePendingInvites(): Flow<List<SpaceInvite>> = pendingInvites.asStateFlow()

    override suspend fun refreshMembers(spaceId: String, ownerId: String, ownerDisplayName: String) {
        val rows = client.postgrest["space_members"]
            .select(Columns.ALL) {
                filter { eq("space_id", spaceId) }
            }
            .decodeList<SpaceMemberRow>()

        val profileIds = rows.mapNotNull { it.userId }.distinct()
        val groupIds = rows.mapNotNull { it.groupId }.distinct()

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

        val groupNames = if (groupIds.isEmpty()) {
            emptyMap()
        } else {
            client.postgrest["contact_groups"]
                .select(Columns.ALL) {
                    filter { isIn("id", groupIds) }
                }
                .decodeList<ContactGroupRow>()
                .associateBy({ it.id }, { it.name })
        }

        val mapped = rows.map { row ->
            when {
                row.userId != null -> {
                    val profile = profiles[row.userId]
                    SpaceMember(
                        id = row.id,
                        spaceId = row.spaceId,
                        kind = SpaceMemberKind.Person,
                        displayName = profile?.displayName ?: profile?.email ?: row.userId,
                        role = row.role.toSpaceMemberRole(),
                        referenceId = row.userId,
                    )
                }
                row.groupId != null -> {
                    SpaceMember(
                        id = row.id,
                        spaceId = row.spaceId,
                        kind = SpaceMemberKind.Group,
                        displayName = groupNames[row.groupId] ?: row.groupId,
                        role = row.role.toSpaceMemberRole(),
                        referenceId = row.groupId,
                    )
                }
                else -> error("invalid_space_member")
            }
        }

        val ownerAlreadyListed = mapped.any { it.referenceId == ownerId && it.kind == SpaceMemberKind.Person }
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

    override suspend fun refreshGroups() {
        runCatching {
            client.postgrest.rpc("archive_expired_contact_groups")
        }

        val rows = client.postgrest["contact_groups"]
            .select(Columns.ALL)
            .decodeList<ContactGroupRow>()

        groups.value = rows
            .map { it.toContactGroup() }
            .activeOnly()
            .sortedBy { it.name.lowercase() }
    }

    override suspend fun refreshGroupMembers(groupId: String) {
        val rows = client.postgrest["group_members"]
            .select(Columns.ALL) {
                filter { eq("group_id", groupId) }
            }
            .decodeList<GroupMemberRow>()

        val profileIds = rows.map { it.userId }.distinct()
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

        groupMembersFlow(groupId).value = rows.map { row ->
            val profile = profiles[row.userId]
            GroupMember(
                id = row.id,
                groupId = row.groupId,
                userId = row.userId,
                displayName = profile?.displayName ?: profile?.email ?: row.userId,
            )
        }.sortedBy { it.displayName.lowercase() }
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
        // Keep the last known invites when the network call fails.
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

    override suspend fun addGroupToSpace(spaceId: String, groupId: String): Result<Unit> = runCatching {
        client.postgrest["space_members"]
            .insert(
                SpaceMemberInsertRow(
                    spaceId = spaceId,
                    groupId = groupId,
                    role = SpaceMemberRole.Editor.wireName(),
                ),
            )
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

    override suspend fun createGroup(
        name: String,
        lifecycle: GroupLifecycle,
        eventLabel: String?,
        startsAtEpochMillis: Long?,
        expiresAtEpochMillis: Long?,
    ): Result<ContactGroup> = runCatching {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "group_name_required" }
        if (lifecycle == GroupLifecycle.Event) {
            require(expiresAtEpochMillis != null) { "event_expires_required" }
        }

        val created = client.postgrest["contact_groups"]
            .insert(
                ContactGroupInsertRow(
                    name = trimmed,
                    lifecycle = lifecycle.wireName(),
                    ownerId = requireUserId(),
                    eventLabel = eventLabel?.trim()?.takeIf { it.isNotEmpty() },
                    startsAt = startsAtEpochMillis?.toIsoString(),
                    expiresAt = expiresAtEpochMillis?.toIsoString(),
                ),
            ) {
                select(Columns.ALL)
            }
            .decodeSingle<ContactGroupRow>()

        refreshGroups()
        created.toContactGroup()
    }

    override suspend fun addUserToGroup(groupId: String, email: String): Result<Unit> = runCatching {
        val trimmed = email.trim()
        require(trimmed.isNotEmpty()) { "member_email_required" }

        val profileId = findProfileIdByEmail(trimmed)
            ?: throw IllegalArgumentException("member_email_not_found")

        client.postgrest["group_members"]
            .insert(
                GroupMemberInsertRow(
                    groupId = groupId,
                    userId = profileId,
                ),
            )
        refreshGroupMembers(groupId)
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

    private fun membersFlow(spaceId: String): MutableStateFlow<List<SpaceMember>> =
        membersBySpace.getOrPut(spaceId) { MutableStateFlow(emptyList()) }

    private fun groupMembersFlow(groupId: String): MutableStateFlow<List<GroupMember>> =
        groupMembersByGroup.getOrPut(groupId) { MutableStateFlow(emptyList()) }

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

    private fun GroupLifecycle.wireName(): String =
        when (this) {
            GroupLifecycle.Persistent -> "persistent"
            GroupLifecycle.Event -> "event"
        }

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

    private fun ContactGroupRow.toContactGroup(): ContactGroup =
        ContactGroup(
            id = id,
            name = name,
            lifecycle = when (lifecycle) {
                "event" -> GroupLifecycle.Event
                else -> GroupLifecycle.Persistent
            },
            ownerId = ownerId,
            eventLabel = eventLabel,
            startsAtEpochMillis = startsAt?.toEpochMillis(),
            expiresAtEpochMillis = expiresAt?.toEpochMillis(),
        )

    private fun Long.toIsoString(): String =
        Instant.fromEpochMilliseconds(this).toString()

    private fun String.toEpochMillis(): Long? =
        runCatching { Instant.parse(this).toEpochMilliseconds() }.getOrNull()

    private companion object {
        const val OWNER_MEMBER_PREFIX = "owner-"
    }
}
