package app.mymultiverse.ammo.data.platform

import app.mymultiverse.ammo.domain.platform.AppStoreLauncher
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

private const val APP_STORE_URL = "https://apps.apple.com/app/id6738309004"

class IosAppStoreLauncher : AppStoreLauncher {
    override fun openStoreListing() {
        val url = NSURL.URLWithString(APP_STORE_URL) ?: return
        UIApplication.sharedApplication.openURL(url)
    }
}
