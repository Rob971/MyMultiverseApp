package app.mymultiverse.ammo.data.platform

import app.mymultiverse.ammo.domain.platform.AppStoreLauncher
import app.mymultiverse.ammo.domain.platform.ReleaseChannel
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

// TestFlight page for alpha and beta testers.
private const val TESTFLIGHT_URL = "https://testflight.apple.com/join/Kh5Pj8mA"

// App Store production listing.
private const val APP_STORE_URL = "https://apps.apple.com/app/id6738309004"

class IosAppStoreLauncher : AppStoreLauncher {
    @Suppress("DEPRECATION")
    override fun openStoreListing(channel: ReleaseChannel) {
        val urlString = when (channel) {
            ReleaseChannel.Alpha, ReleaseChannel.Beta -> TESTFLIGHT_URL
            ReleaseChannel.Production -> APP_STORE_URL
        }
        val url = NSURL.URLWithString(urlString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }
}
