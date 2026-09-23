package app.mymultiverse.ammo.data.platform

import android.content.Context
import android.content.pm.ApplicationInfo
import app.mymultiverse.ammo.domain.platform.AppStoreLauncher
import app.mymultiverse.ammo.domain.platform.ReleaseChannel
import com.google.firebase.appdistribution.FirebaseAppDistribution

/**
 * Firebase-aware AppStoreLauncher for tester (debug) builds. Lives in the
 * androidFirebase source set, compiled only when google-services.json exists,
 * so it can import the App Distribution SDK without leaking Firebase into
 * non-Firebase builds.
 */
class FirebaseAppStoreLauncher(
    private val context: Context,
) : AppStoreLauncher {
    private val fallback = AndroidAppStoreLauncher(context)

    override fun openStoreListing(channel: ReleaseChannel) {
        fallback.openStoreListing(channel)
    }

    override fun requestUpdate(channel: ReleaseChannel): Boolean {
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            FirebaseAppDistribution.getInstance().updateIfNewReleaseAvailable()
                .addOnFailureListener { e ->
                    android.util.Log.e("AppUpdate", "In-app update check failed", e)
                }
            return true
        }
        return false
    }
}