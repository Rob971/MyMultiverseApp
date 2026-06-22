package app.mymultiverse.kmp.data.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object AndroidNotificationChannels {
    const val HOUSEHOLD_INVITES = "household_invites"
    const val HOUSEHOLD_UPDATES = "household_updates"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                HOUSEHOLD_INVITES,
                "Household invites",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Notifications when someone invites you to a household"
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                HOUSEHOLD_UPDATES,
                "Household updates",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Notifications when household members join or collaborate"
            },
        )
    }
}
