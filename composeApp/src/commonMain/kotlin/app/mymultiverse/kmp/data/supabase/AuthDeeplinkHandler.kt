package app.mymultiverse.kmp.data.supabase

import io.github.jan.supabase.SupabaseClient

internal expect fun handleAuthDeeplink(client: SupabaseClient, url: String)
