package app.mymultiverse.kmp.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

internal object SupabaseClientFactory {
    private val sharedClient: SupabaseClient? by lazy {
        if (SupabaseSecrets.ANON_KEY.isBlank()) return@lazy null

        createSupabaseClient(
            supabaseUrl = SupabaseConfig.URL,
            supabaseKey = SupabaseSecrets.ANON_KEY,
        ) {
            install(Auth) {
                scheme = AuthRedirectUrls.SCHEME
                host = "auth"
            }
            install(Postgrest)
            install(Realtime)
        }
    }

    fun getOrNull(): SupabaseClient? = sharedClient

    fun createOrNull(): SupabaseClient? = getOrNull()
}
