package app.mymultiverse.kmp.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import platform.Foundation.NSURL

internal actual fun handleAuthDeeplink(client: SupabaseClient, url: String) {
    client.handleDeeplinks(NSURL(string = url))
}
