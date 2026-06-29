package app.mymultiverse.ammo.data.supabase

import android.content.Intent
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

internal actual suspend fun handleAuthDeeplink(client: SupabaseClient, url: String) {
    val uri = Uri.parse(url)
    if (uri.scheme != client.auth.config.scheme || uri.host != client.auth.config.host) return

    val hasSessionPayload = !uri.getQueryParameter("code").isNullOrBlank() ||
        !uri.fragment.isNullOrBlank()
    if (!hasSessionPayload) return

    suspendCancellableCoroutine { continuation ->
        var completed = false
        fun complete(block: () -> Unit) {
            if (!completed) {
                completed = true
                block()
            }
        }

        val intent = Intent(Intent.ACTION_VIEW, uri)
        client.handleDeeplinks(
            intent = intent,
            onSessionSuccess = { complete { continuation.resume(Unit) } },
            onError = { error -> complete { continuation.resumeWithException(error) } },
        )
    }
}
