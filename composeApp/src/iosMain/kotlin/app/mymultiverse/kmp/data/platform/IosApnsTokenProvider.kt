package app.mymultiverse.kmp.data.platform

import app.mymultiverse.kmp.IosPushTokenBridge

object IosApnsTokenProvider {
    fun currentToken(): String? {
        val token = IosPushTokenBridge.currentToken() ?: return null
        if (token.startsWith("ios-stub")) return null
        return token
    }
}
