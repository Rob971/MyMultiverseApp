package app.mymultiverse.ammo.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AmmoFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        PushTokenRefreshStore.save(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        HouseholdPushNotificationHandler.handleForegroundMessage(applicationContext, message)
    }
}
