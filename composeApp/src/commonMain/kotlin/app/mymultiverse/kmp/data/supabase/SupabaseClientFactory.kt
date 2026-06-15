package app.mymultiverse.kmp.data.supabase

import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

internal object SupabaseClientFactory {
    private var sharedClient: SupabaseClient? = null

    fun getOrNull(settings: Settings): SupabaseClient? {
        if (SupabaseRuntimeFlags.disableClientCreation || SupabaseSecrets.ANON_KEY.isBlank()) return null
        sharedClient?.let { return it }

        return createSupabaseClient(
            supabaseUrl = SupabaseSecrets.URL,
            supabaseKey = SupabaseSecrets.ANON_KEY,
        ) {
            install(Auth) {
                scheme = AuthRedirectUrls.SCHEME
                host = "auth"
                sessionManager = SettingsSessionManager(settings)
            }
            install(Postgrest)
            install(Realtime)
        }.also { sharedClient = it }
    }
}
