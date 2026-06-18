package app.mymultiverse.kmp

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("PushTokenBridge")
object IosPushTokenBridge {
    private var latestToken: String? = null
    private var onTokenUpdated: (() -> Unit)? = null

    fun register(token: String) {
        latestToken = token
        onTokenUpdated?.invoke()
    }

    fun currentToken(): String? = latestToken

    internal fun setOnTokenUpdated(listener: (() -> Unit)?) {
        onTokenUpdated = listener
    }
}
