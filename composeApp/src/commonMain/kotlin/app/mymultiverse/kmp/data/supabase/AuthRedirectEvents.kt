package app.mymultiverse.kmp.data.supabase

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthRedirectUrls {
    const val SCHEME = "app.mymultiverse.kmp"
    const val HOST = "auth"
    const val REDIRECT = "$SCHEME://$HOST"
}

object AuthRedirectEvents {
    private val _urls = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val urls: SharedFlow<String> = _urls.asSharedFlow()

    private var pendingUrl: String? = null

    fun emit(url: String) {
        if (!isAuthRedirect(url)) return
        pendingUrl = url
        _urls.tryEmit(url)
    }

    fun consumePending(): String? = pendingUrl.also { pendingUrl = null }

    fun isAuthRedirect(url: String): Boolean =
        url.startsWith("${AuthRedirectUrls.SCHEME}://${AuthRedirectUrls.HOST}")
}
