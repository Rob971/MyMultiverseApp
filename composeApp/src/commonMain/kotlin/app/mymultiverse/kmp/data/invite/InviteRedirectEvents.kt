package app.mymultiverse.kmp.data.invite

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object InviteRedirectEvents {
    private val _urls = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val urls: SharedFlow<String> = _urls.asSharedFlow()

    private var pendingUrl: String? = null

    fun emit(url: String) {
        if (!InviteRedirectUrls.isInviteRedirect(url)) return
        pendingUrl = url
        _urls.tryEmit(url)
    }

    fun consumePending(): String? = pendingUrl.also { pendingUrl = null }
}
