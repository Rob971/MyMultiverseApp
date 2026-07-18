package app.mymultiverse.ammo.domain.platform

/**
 * Opens the appropriate platform store or distribution page for users to get
 * the latest build for their [channel]:
 *  - [ReleaseChannel.Alpha]      → Firebase App Distribution (Android) / TestFlight (iOS)
 *  - [ReleaseChannel.Beta]       → Play Store Closed Testing (Android) / TestFlight (iOS)
 *  - [ReleaseChannel.Production] → Play Store (Android) / App Store (iOS)
 */
interface AppStoreLauncher {
    fun openStoreListing(channel: ReleaseChannel)
}
