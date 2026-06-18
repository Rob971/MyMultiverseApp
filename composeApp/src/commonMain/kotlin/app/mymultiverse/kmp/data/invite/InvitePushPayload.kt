package app.mymultiverse.kmp.data.invite

object InvitePushPayload {
    const val TYPE_HOUSEHOLD_INVITE = "household_invite"
    const val KEY_TYPE = "type"
    const val KEY_INVITE_TOKEN = "invite_token"

    fun inviteTokenFromData(data: Map<String, String>): String? {
        if (data[KEY_TYPE] != TYPE_HOUSEHOLD_INVITE) return null
        return data[KEY_INVITE_TOKEN]?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun inviteRedirectUrlFromData(data: Map<String, String>): String? {
        val token = inviteTokenFromData(data) ?: return null
        return InviteRedirectUrls.build(token)
    }
}
