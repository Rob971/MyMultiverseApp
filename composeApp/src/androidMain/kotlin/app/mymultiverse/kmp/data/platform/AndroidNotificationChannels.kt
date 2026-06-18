package app.mymultiverse.kmp.data.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object AndroidNotificationChannels {
    const val HOUSEHOLD_INVITES = "household_invites"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            HOUSEHOLD_INVITES,
            "Household invites",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Notifications when someone invites you to a household"
        }
        manager.createNotificationChannel(channel)
    }
}
