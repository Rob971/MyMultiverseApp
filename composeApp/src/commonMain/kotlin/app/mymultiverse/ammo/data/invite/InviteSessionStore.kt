package app.mymultiverse.ammo.data.invite

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get

class InviteSessionStore(
    private val settings: Settings,
) {
    fun getPendingInviteToken(): String? =
        settings.getStringOrNull(KEY_PENDING_INVITE_TOKEN)?.takeIf { it.isNotBlank() }

    fun setPendingInviteToken(token: String) {
        settings.putString(KEY_PENDING_INVITE_TOKEN, token)
    }

    fun clearPendingInviteToken() {
        settings.remove(KEY_PENDING_INVITE_TOKEN)
    }

    private companion object {
        const val KEY_PENDING_INVITE_TOKEN = "pending_invite_token"
    }
}
