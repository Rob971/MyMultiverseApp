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
            InvitePushPayload.TYPE_GROCERY_LIST_NUDGE -> {
                val nudgerName = data[InvitePushPayload.KEY_NUDGER_NAME].orEmpty().ifBlank { "Someone" }
                "$nudgerName is heading to the store"
            }
            InvitePushPayload.TYPE_GROCERY_ITEM_ADDED -> "Grocery list updated"
            InvitePushPayload.TYPE_MEAL_PLAN_ITEM_ADDED -> "Meal plan updated"
            else -> null
        }

    private fun defaultBody(data: Map<String, String>): String? =
        when (data[InvitePushPayload.KEY_TYPE]) {
            InvitePushPayload.TYPE_HOUSEHOLD_INVITE -> "Open Ammò to accept"
            InvitePushPayload.TYPE_MEMBER_JOINED -> "Open Ammò to see your household"
            InvitePushPayload.TYPE_GROCERY_LIST_NUDGE ->
                "Add anything missing to the grocery list before they shop"
            InvitePushPayload.TYPE_GROCERY_ITEM_ADDED -> {
                val actorName = data[InvitePushPayload.KEY_ACTOR_NAME].orEmpty().ifBlank { "Someone" }
                val itemLabel = data[InvitePushPayload.KEY_ITEM_LABEL].orEmpty()
                val addedCount = data[InvitePushPayload.KEY_ADDED_COUNT]?.toIntOrNull() ?: 1
                if (addedCount > 1) {
                    "$actorName added $addedCount items to the grocery list"
                } else {
                    "$actorName added $itemLabel to the grocery list"
                }
            }
            InvitePushPayload.TYPE_MEAL_PLAN_ITEM_ADDED -> {
                val actorName = data[InvitePushPayload.KEY_ACTOR_NAME].orEmpty().ifBlank { "Someone" }
                val addedCount = data[InvitePushPayload.KEY_ADDED_COUNT]?.toIntOrNull() ?: 1
                if (addedCount > 1) {
                    "$actorName added $addedCount meals to the plan"
                } else {
                    val itemLabel = data[InvitePushPayload.KEY_ITEM_LABEL].orEmpty()
                    "$actorName added $itemLabel to the meal plan"
                }
            }
            else -> null
        }

    private fun notificationIdFor(data: Map<String, String>): Int =
        when (data[InvitePushPayload.KEY_TYPE]) {
            InvitePushPayload.TYPE_MEMBER_JOINED ->
                data[InvitePushPayload.KEY_HOUSEHOLD_ID].hashCode()
            InvitePushPayload.TYPE_GROCERY_LIST_NUDGE ->
                data[InvitePushPayload.KEY_HOUSEHOLD_ID].hashCode() xor data[InvitePushPayload.KEY_WEEK_KEY].hashCode()
            InvitePushPayload.TYPE_GROCERY_ITEM_ADDED ->
                data[InvitePushPayload.KEY_HOUSEHOLD_ID].hashCode() xor
                    (data[InvitePushPayload.KEY_ITEM_LABEL].hashCode())
            InvitePushPayload.TYPE_MEAL_PLAN_ITEM_ADDED ->
                data[InvitePushPayload.KEY_HOUSEHOLD_ID].hashCode() xor
                    (data[InvitePushPayload.KEY_DAY_INDEX].hashCode())
            else ->
                data[InvitePushPayload.KEY_INVITE_TOKEN]?.hashCode()
                    ?: data.hashCode()
        }
}
