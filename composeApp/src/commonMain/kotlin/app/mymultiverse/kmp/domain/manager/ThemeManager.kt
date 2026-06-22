package app.mymultiverse.kmp.domain.manager

import kotlinx.coroutines.flow.StateFlow

interface ThemeManager {
    val currentPreference: StateFlow<AppThemePreference>
    fun changeThemePreference(preference: AppThemePreference)
}
