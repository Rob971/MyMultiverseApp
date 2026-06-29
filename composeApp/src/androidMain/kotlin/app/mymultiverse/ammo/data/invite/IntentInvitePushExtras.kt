package app.mymultiverse.ammo.data.invite

import android.content.Intent

fun Intent.extractInvitePushData(): Map<String, String>? {
    val bundle = extras ?: return null
    val data = bundle.keySet()
        .mapNotNull { key ->
            bundle.getString(key)?.let { value -> key to value }
        }
        .toMap()
    return data.takeIf { it.isNotEmpty() }
}

fun Intent.extractInvitePushRedirectUrl(): String? =
    extractInvitePushData()?.let(InvitePushPayload::inviteRedirectUrlFromData)
