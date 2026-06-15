package app.mymultiverse.kmp.data.supabase

import io.github.jan.supabase.SupabaseClient

internal class SupabaseClientHolder(
    val client: SupabaseClient?,
) {
    companion object {
        fun create(): SupabaseClientHolder = SupabaseClientHolder(SupabaseClientFactory.getOrNull())
    }
}
