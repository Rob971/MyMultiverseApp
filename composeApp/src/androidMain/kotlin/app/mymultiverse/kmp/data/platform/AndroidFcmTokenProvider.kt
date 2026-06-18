package app.mymultiverse.kmp.data.platform

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object AndroidFcmTokenProvider {
    suspend fun getToken(context: Context): String? {
        return try {
            ensureFirebase(context)
            FirebaseMessaging.getInstance().token.await()
        } catch (_: Exception) {
            null
        }
    }

    private fun ensureFirebase(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }
}
