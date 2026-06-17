package app.mymultiverse.kmp.data.supabase

import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
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
                host = AuthRedirectUrls.HOST
                // PKCE puts ?code= in the query string; Android drops URL fragments from deeplinks.
                flowType = FlowType.PKCE
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
                autoSaveToStorage = true
                sessionManager = SettingsSessionManager(settings)
            }
            install(Postgrest)
            install(Realtime)
            install(Functions)
        }.also { sharedClient = it }
    }
}
