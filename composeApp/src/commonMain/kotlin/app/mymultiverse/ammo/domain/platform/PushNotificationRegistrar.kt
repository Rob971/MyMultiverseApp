package app.mymultiverse.ammo.domain.platform

/**
 * Registers FCM/APNs device tokens with the backend after authentication.
 */
interface PushNotificationRegistrar {
    suspend fun registerCurrentDeviceToken()
}
