package app.mymultiverse.kmp.domain

/**
 * User-facing app version rules shared with Gradle and CI (see scripts/format-app-version.sh).
 */
object AppVersionFormatter {
    fun formatVersionName(lts: String, candidate: Int): String {
        if (candidate <= 0) return lts
        val parts = lts.split(".")
        val major = parts.getOrElse(0) { "0" }
        val minor = parts.getOrElse(1) { "0" }
        return "$major.$minor.$candidate"
    }
}
