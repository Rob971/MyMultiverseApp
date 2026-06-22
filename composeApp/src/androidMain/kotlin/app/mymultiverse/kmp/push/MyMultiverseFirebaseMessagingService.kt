package app.mymultiverse.kmp.push

import app.mymultiverse.kmp.push.HouseholdPushNotificationHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyMultiverseFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        PushTokenRefreshStore.save(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        HouseholdPushNotificationHandler.handleForegroundMessage(applicationContext, message)
    }
}
