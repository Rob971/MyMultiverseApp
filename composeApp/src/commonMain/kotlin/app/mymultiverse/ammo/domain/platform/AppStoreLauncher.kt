package app.mymultiverse.ammo.domain.platform

/**
 * Opens the platform app store listing so users can check for and install updates.
 * Android opens the Play Store; iOS opens the App Store.
 */
interface AppStoreLauncher {
    fun openStoreListing()
}
