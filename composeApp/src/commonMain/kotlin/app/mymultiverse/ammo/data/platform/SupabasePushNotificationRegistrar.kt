package app.mymultiverse.ammo.data.platform

import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages
import app.mymultiverse.ammo.domain.platform.PushNotificationRegistrar
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Registers the device push token with Supabase via [register_device_token] RPC.
 */
class SupabasePushNotificationRegistrar(
    private val client: SupabaseClient,
    private val platform: String,
    private val tokenProvider: suspend () -> String?,
    private val localeProvider: () -> String = { SupportedAppLanguages.DEFAULT_CODE },
) : PushNotificationRegistrar {
    override suspend fun registerCurrentDeviceToken() {
        val token = tokenProvider() ?: return
        client.postgrest.rpc(
            "register_device_token",
            buildJsonObject {
                put("p_platform", platform)
                put("p_token", token)
                put("p_app_locale", SupportedAppLanguages.normalize(localeProvider()))
            },
        )
    }
}

class NoOpPushNotificationRegistrar : PushNotificationRegistrar {
    override suspend fun registerCurrentDeviceToken() = Unit
}
