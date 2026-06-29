package app.mymultiverse.ammo.domain

/**
 * User-facing app version rules shared with Gradle and CI (see scripts/format-app-version.sh).
 */
object AppVersionFormatter {
    fun formatVersionName(name: String, prerelease: String? = null): String {
        val segment = prerelease?.trim().orEmpty()
        if (segment.isEmpty()) return name
        return "$name-$segment"
    }
}
