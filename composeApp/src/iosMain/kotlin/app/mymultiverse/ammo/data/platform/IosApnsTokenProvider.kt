package app.mymultiverse.ammo.data.platform

import app.mymultiverse.ammo.IosPushTokenBridge

object IosApnsTokenProvider {
    fun currentToken(): String? {
        val token = IosPushTokenBridge.currentToken() ?: return null
        if (token.startsWith("ios-stub")) return null
        return token
    }
}
