package app.mymultiverse.ammo.data.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import app.mymultiverse.ammo.domain.platform.AppStoreLauncher

class AndroidAppStoreLauncher(
    private val context: Context,
) : AppStoreLauncher {
    override fun openStoreListing() {
        val packageName = context.packageName
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }.onFailure {
            // Fall back to browser if Play Store app is not installed.
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
