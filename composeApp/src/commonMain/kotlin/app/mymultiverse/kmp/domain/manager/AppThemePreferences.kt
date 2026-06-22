package app.mymultiverse.kmp.domain.manager

object AppThemePreferences {
    const val SETTINGS_KEY = "app_theme_preference"
    val DEFAULT = AppThemePreference.SYSTEM

    fun normalize(stored: String): AppThemePreference =
        when (stored.lowercase()) {
            "light" -> AppThemePreference.LIGHT
            "dark" -> AppThemePreference.DARK
            else -> AppThemePreference.SYSTEM
        }

    fun storageValue(preference: AppThemePreference): String =
        when (preference) {
            AppThemePreference.SYSTEM -> "system"
            AppThemePreference.LIGHT -> "light"
            AppThemePreference.DARK -> "dark"
        }

    fun resolveDarkTheme(preference: AppThemePreference, systemDark: Boolean): Boolean =
        when (preference) {
            AppThemePreference.SYSTEM -> systemDark
            AppThemePreference.LIGHT -> false
            AppThemePreference.DARK -> true
        }
}
