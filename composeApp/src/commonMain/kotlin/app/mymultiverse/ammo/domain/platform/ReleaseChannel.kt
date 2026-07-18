package app.mymultiverse.ammo.domain.platform

/**
 * Represents the distribution channel a build was stamped for.
 * Derived from [AppBuildInfo.VERSION_NAME] which CI stamps as:
 *  - "X.Y.Z-alpha.N"  → [Alpha]  (Firebase App Distribution, debug APK)
 *  - "X.Y.Z-beta.N"   → [Beta]   (Play Store Closed Testing, signed AAB)
 *  - "X.Y.Z"          → [Production] (Play Store / App Store, signed AAB)
 */
sealed class ReleaseChannel {
    data object Alpha : ReleaseChannel()
    data object Beta : ReleaseChannel()
    data object Production : ReleaseChannel()

    companion object {
        /**
         * Parses the channel from the formatted [versionName] (e.g. "1.6.0-alpha.42").
         * Matches the suffix format produced by [app.mymultiverse.ammo.domain.AppVersionFormatter].
         */
        fun fromVersionName(versionName: String): ReleaseChannel = when {
            versionName.contains("-alpha.") -> Alpha
            versionName.contains("-beta.") -> Beta
            else -> Production
        }
    }
}
