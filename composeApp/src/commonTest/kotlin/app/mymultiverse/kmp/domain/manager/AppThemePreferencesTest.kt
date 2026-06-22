package app.mymultiverse.kmp.domain.manager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppThemePreferencesTest {
    @Test
    fun normalize_mapsKnownValues() {
        assertEquals(AppThemePreference.LIGHT, AppThemePreferences.normalize("light"))
        assertEquals(AppThemePreference.DARK, AppThemePreferences.normalize("DARK"))
        assertEquals(AppThemePreference.SYSTEM, AppThemePreferences.normalize("system"))
        assertEquals(AppThemePreference.SYSTEM, AppThemePreferences.normalize("unknown"))
    }

    @Test
    fun resolveDarkTheme_respectsPreferenceAndSystem() {
        assertTrue(AppThemePreferences.resolveDarkTheme(AppThemePreference.DARK, systemDark = false))
        assertFalse(AppThemePreferences.resolveDarkTheme(AppThemePreference.LIGHT, systemDark = true))
        assertTrue(AppThemePreferences.resolveDarkTheme(AppThemePreference.SYSTEM, systemDark = true))
        assertFalse(AppThemePreferences.resolveDarkTheme(AppThemePreference.SYSTEM, systemDark = false))
    }

    @Test
    fun storageValue_roundTripsThroughNormalize() {
        AppThemePreference.entries.forEach { preference ->
            assertEquals(
                preference,
                AppThemePreferences.normalize(AppThemePreferences.storageValue(preference)),
            )
        }
    }
}
