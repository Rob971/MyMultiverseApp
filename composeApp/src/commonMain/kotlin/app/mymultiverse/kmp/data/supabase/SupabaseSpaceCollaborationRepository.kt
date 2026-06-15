package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.data.supabase.dto.ContactGroupInsertRow
import app.mymultiverse.kmp.data.supabase.dto.ContactGroupRow
import app.mymultiverse.kmp.data.supabase.dto.GroupMemberInsertRow
import app.mymultiverse.kmp.data.supabase.dto.ProfileRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceMemberInsertRow
import app.mymultiverse.kmp.data.supabase.dto.SpaceMemberRow
import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
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
import kotlinx.datetime.Instant
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseSpaceCollaborationRepository(
    private val client: SupabaseClient,
) : SpaceCollaborationRepository {

    private val membersBySpace = mutableMapOf<String, MutableStateFlow<List<SpaceMember>>>()
    private val groups = MutableStateFlow<List<ContactGroup>>(emptyList())

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> =
        membersFlow(spaceId).asStateFlow()

    override fun observeGroups(): Flow<List<ContactGroup>> = groups.asStateFlow()

    override suspend fun refreshMembers(spaceId: String) {
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

        membersFlow(spaceId).value = mapped.sortedBy { it.displayName.lowercase() }
    }

    override suspend fun refreshGroups() {
        val rows = client.postgrest["contact_groups"]
            .select(Columns.ALL)
            .decodeList<ContactGroupRow>()

        groups.value = rows.map { it.toContactGroup() }.sortedBy { it.name.lowercase() }
    }

    override suspend fun addMemberByEmail(spaceId: String, email: String): Result<Unit> = runCatching {
        val trimmed = email.trim()
        require(trimmed.isNotEmpty()) { "member_email_required" }

        val profileId = findProfileIdByEmail(trimmed)
            ?: throw IllegalArgumentException("member_email_not_found")

        val currentUserId = requireUserId()
        if (profileId == currentUserId) {
            throw IllegalArgumentException("member_cannot_add_self")
        }

        client.postgrest["space_members"]
            .insert(
                SpaceMemberInsertRow(
                    spaceId = spaceId,
                    userId = profileId,
                    role = "editor",
                ),
            )

        refreshMembers(spaceId)
    }

    override suspend fun addGroupToSpace(spaceId: String, groupId: String): Result<Unit> = runCatching {
        client.postgrest["space_members"]
            .insert(
                SpaceMemberInsertRow(
                    spaceId = spaceId,
                    groupId = groupId,
                    role = "editor",
                ),
            )
        refreshMembers(spaceId)
    }

    override suspend fun removeMember(memberId: String): Result<Unit> = runCatching {
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
        expiresAtEpochMillis: Long?,
    ): Result<ContactGroup> = runCatching {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "group_name_required" }

        val created = client.postgrest["contact_groups"]
            .insert(
                ContactGroupInsertRow(
                    name = trimmed,
                    lifecycle = lifecycle.wireName(),
                    ownerId = requireUserId(),
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
    }

    private suspend fun findProfileIdByEmail(email: String): String? {
        val parameters = buildJsonObject { put("p_email", email) }
        return client.postgrest
            .rpc("find_profile_id_by_email", parameters)
            .decodeSingleOrNull()
    }

    private fun membersFlow(spaceId: String): MutableStateFlow<List<SpaceMember>> =
        membersBySpace.getOrPut(spaceId) { MutableStateFlow(emptyList()) }

    private suspend fun requireUserId(): String {
        client.auth.awaitInitialization()
        return client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("auth_required")
    }

    private fun GroupLifecycle.wireName(): String =
        when (this) {
            GroupLifecycle.Persistent -> "persistent"
            GroupLifecycle.Event -> "event"
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
            expiresAtEpochMillis = expiresAt?.toEpochMillis(),
        )

    private fun Long.toIsoString(): String =
        Instant.fromEpochMilliseconds(this).toString()

    private fun String.toEpochMillis(): Long? =
        runCatching { Instant.parse(this).toEpochMilliseconds() }.getOrNull()
}
