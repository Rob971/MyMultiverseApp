package app.mymultiverse.kmp.data.platform

import app.mymultiverse.kmp.domain.platform.PushNotificationRegistrar
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
) : PushNotificationRegistrar {
    override suspend fun registerCurrentDeviceToken() {
        val token = tokenProvider() ?: return
        client.postgrest.rpc(
            "register_device_token",
            buildJsonObject {
                put("p_platform", platform)
                put("p_token", token)
            },
        )
    }
}

class NoOpPushNotificationRegistrar : PushNotificationRegistrar {
    override suspend fun registerCurrentDeviceToken() = Unit
}
