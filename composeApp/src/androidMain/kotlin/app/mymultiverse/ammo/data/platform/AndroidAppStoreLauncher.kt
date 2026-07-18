package app.mymultiverse.ammo.data.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import app.mymultiverse.ammo.domain.platform.AppStoreLauncher
import app.mymultiverse.ammo.domain.platform.ReleaseChannel

// Firebase App Distribution landing page for alpha testers (requires Firebase sign-in).
private const val FIREBASE_ALPHA_URL =
    "https://appdistribution.firebase.google.com/testerapps/1:37917280954:android:a0c28d6a257baf50f91083"

// Play Store Closed Testing opt-in page for beta testers.
private const val PLAY_BETA_URL =
    "https://play.google.com/apps/testing/app.mymultiverse.ammo"

class AndroidAppStoreLauncher(
    private val context: Context,
) : AppStoreLauncher {
    override fun openStoreListing(channel: ReleaseChannel) {
        val packageName = context.packageName
        when (channel) {
            ReleaseChannel.Alpha -> openUrl(FIREBASE_ALPHA_URL)
            ReleaseChannel.Beta -> openUrl(PLAY_BETA_URL)
            ReleaseChannel.Production -> openPlayStore(packageName)
        }
    }

    private fun openPlayStore(packageName: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }.onFailure {
            openUrl("https://play.google.com/store/apps/details?id=$packageName")
        }
    }

    private fun openUrl(url: String) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
