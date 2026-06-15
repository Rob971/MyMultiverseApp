package app.mymultiverse.kmp.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks

internal actual fun handleAuthDeeplink(client: SupabaseClient, url: String) {
    client.auth.handleDeeplinks(url)
}
