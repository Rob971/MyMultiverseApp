package app.mymultiverse.kmp.data.supabase

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthRedirectUrls {
    const val SCHEME = "app.mymultiverse.kmp"
    const val REDIRECT = "$SCHEME://auth/callback"
}

object AuthRedirectEvents {
    private val _urls = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val urls: SharedFlow<String> = _urls.asSharedFlow()

    fun emit(url: String) {
        _urls.tryEmit(url)
    }
}
