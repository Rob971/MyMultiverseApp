package app.mymultiverse.kmp.data.supabase

import android.content.Intent
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks

internal actual fun handleAuthDeeplink(client: SupabaseClient, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    client.handleDeeplinks(intent)
}
