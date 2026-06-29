package app.mymultiverse.ammo.data.invite

object InvitePushPayload {
    const val TYPE_HOUSEHOLD_INVITE = "household_invite"
    const val TYPE_MEMBER_JOINED = "household_member_joined"
    const val KEY_TYPE = "type"
    const val KEY_INVITE_TOKEN = "invite_token"
    const val KEY_HOUSEHOLD_ID = "household_id"
    const val KEY_MEMBER_NAME = "member_name"

    fun inviteTokenFromData(data: Map<String, String>): String? {
        if (data[KEY_TYPE] != TYPE_HOUSEHOLD_INVITE) return null
        return data[KEY_INVITE_TOKEN]?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun memberJoinedHouseholdIdFromData(data: Map<String, String>): String? {
        if (data[KEY_TYPE] != TYPE_MEMBER_JOINED) return null
        return data[KEY_HOUSEHOLD_ID]?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun inviteRedirectUrlFromData(data: Map<String, String>): String? {
        val token = inviteTokenFromData(data) ?: return null
        return InviteRedirectUrls.build(token)
    }

    fun deliverFromPushData(data: Map<String, String>) {
        inviteRedirectUrlFromData(data)?.let(InviteRedirectEvents::emit)
        memberJoinedHouseholdIdFromData(data)?.let(HouseholdPushEvents::emitMemberJoined)
    }
}
