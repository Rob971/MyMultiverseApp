package app.mymultiverse.kmp.domain.model.sharing

enum class AddMemberResult {
    Added,
    InviteSent,
}

data class GroupMember(
    val id: String,
    val groupId: String,
    val userId: String,
    val displayName: String,
)

data class SpaceInvite(
    val id: String,
    val spaceId: String,
    val spaceName: String,
    val email: String,
    val role: SpaceMemberRole,
    val expiresAtEpochMillis: Long?,
)
