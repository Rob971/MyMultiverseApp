package app.mymultiverse.ammo.push

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.mymultiverse.ammo.data.invite.InvitePushPayload
import app.mymultiverse.ammo.data.platform.AndroidNotificationChannels
import app.mymultiverse.ammo.shared.R
import com.google.firebase.messaging.RemoteMessage

object HouseholdPushNotificationHandler {
    fun handleForegroundMessage(context: Context, message: RemoteMessage) {
        val data = message.data
        if (data.isEmpty()) return

        InvitePushPayload.deliverFromPushData(data)

        val title = message.notification?.title
            ?: defaultTitle(data)
            ?: return
        val body = message.notification?.body
            ?: defaultBody(data)
            ?: return

        showNotification(
            context = context,
            notificationId = notificationIdFor(data),
            title = title,
            body = body,
            data = data,
        )
    }

    fun deliverIntentPayload(intent: Intent?) {
        val data = intent?.extras?.keySet()
            ?.mapNotNull { key -> intent.extras?.getString(key)?.let { value -> key to value } }
            ?.toMap()
            .orEmpty()
        if (data.isNotEmpty()) {
            InvitePushPayload.deliverFromPushData(data)
        }
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        val launchIntent = Intent().apply {
            setClassName(context.packageName, "app.mymultiverse.ammo.MainActivity")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, AndroidNotificationChannels.HOUSEHOLD_UPDATES)
            .setSmallIcon(R.drawable.ic_stat_household)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun defaultTitle(data: Map<String, String>): String? =
        when (data[InvitePushPayload.KEY_TYPE]) {
            InvitePushPayload.TYPE_HOUSEHOLD_INVITE -> "Household invite"
            InvitePushPayload.TYPE_MEMBER_JOINED -> {
                val memberName = data[InvitePushPayload.KEY_MEMBER_NAME].orEmpty().ifBlank { "Someone" }
                "$memberName joined"
            }
            else -> null
        }

    private fun defaultBody(data: Map<String, String>): String? =
        when (data[InvitePushPayload.KEY_TYPE]) {
            InvitePushPayload.TYPE_HOUSEHOLD_INVITE -> "Open Ammò to accept"
            InvitePushPayload.TYPE_MEMBER_JOINED -> "Open Ammò to see your household"
            else -> null
        }

    private fun notificationIdFor(data: Map<String, String>): Int =
        when (data[InvitePushPayload.KEY_TYPE]) {
            InvitePushPayload.TYPE_MEMBER_JOINED ->
                data[InvitePushPayload.KEY_HOUSEHOLD_ID].hashCode()
            else ->
                data[InvitePushPayload.KEY_INVITE_TOKEN]?.hashCode()
                    ?: data.hashCode()
        }
}
