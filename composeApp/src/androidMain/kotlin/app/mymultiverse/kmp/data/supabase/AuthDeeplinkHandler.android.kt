package app.mymultiverse.kmp.data.supabase

import io.github.jan.supabase.SupabaseClient

internal actual fun handleAuthDeeplink(client: SupabaseClient, url: String) {
    // Android delivers OAuth callbacks via Intent in MainActivity.handleDeeplinks(intent).
}
