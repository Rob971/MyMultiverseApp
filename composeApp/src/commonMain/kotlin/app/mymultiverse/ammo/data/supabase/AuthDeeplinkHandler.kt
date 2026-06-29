package app.mymultiverse.ammo.data.supabase

import io.github.jan.supabase.SupabaseClient

internal expect suspend fun handleAuthDeeplink(client: SupabaseClient, url: String)
