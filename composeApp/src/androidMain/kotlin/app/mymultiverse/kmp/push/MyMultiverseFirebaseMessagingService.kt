package app.mymultiverse.kmp.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyMultiverseFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        PushTokenRefreshStore.save(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Notification payloads are shown by the system when the app is in the background.
    }
}
