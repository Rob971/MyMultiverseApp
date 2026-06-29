package app.mymultiverse.ammo.data.supabase

import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient

internal class SupabaseClientHolder(
    val client: SupabaseClient?,
) {
    companion object {
        fun create(settings: Settings): SupabaseClientHolder =
            SupabaseClientHolder(SupabaseClientFactory.getOrNull(settings))
    }
}
