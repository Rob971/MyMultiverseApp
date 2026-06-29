package app.mymultiverse.ammo.push

import android.content.Intent
import app.mymultiverse.ammo.data.invite.InvitePushPayload

object InvitePushIntentDelivery {
    fun deliverIntentPayload(intent: Intent?) {
        val data = intent?.extras?.keySet()
            ?.mapNotNull { key -> intent.extras?.getString(key)?.let { value -> key to value } }
            ?.toMap()
            .orEmpty()
        if (data.isNotEmpty()) {
            InvitePushPayload.deliverFromPushData(data)
        }
    }
}
