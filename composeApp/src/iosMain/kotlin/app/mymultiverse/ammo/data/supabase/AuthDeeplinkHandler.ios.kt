package app.mymultiverse.ammo.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL

internal actual suspend fun handleAuthDeeplink(client: SupabaseClient, url: String) {
    val nsUrl = NSURL(string = url)
    if (nsUrl.scheme != client.auth.config.scheme || nsUrl.host != client.auth.config.host) return

    val hasCode = nsUrl.query?.contains("code=") == true
    val hasFragment = nsUrl.fragment != null
    if (!hasCode && !hasFragment) return

    suspendCancellableCoroutine { continuation ->
        var completed = false
        fun complete(block: () -> Unit) {
            if (!completed) {
                completed = true
                block()
            }
        }

        client.handleDeeplinks(
            url = nsUrl,
            onSessionSuccess = { complete { continuation.resume(Unit) } },
            onError = { error -> complete { continuation.resumeWithException(error) } },
        )
    }
}
