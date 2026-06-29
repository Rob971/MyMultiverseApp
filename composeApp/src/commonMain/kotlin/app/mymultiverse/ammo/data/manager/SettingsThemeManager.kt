package app.mymultiverse.ammo.data.manager

import app.mymultiverse.ammo.domain.manager.AppThemePreference
import app.mymultiverse.ammo.domain.manager.AppThemePreferences
import app.mymultiverse.ammo.domain.manager.ThemeManager
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsThemeManager(
    private val settings: Settings,
) : ThemeManager {
    private val persisted = readPersisted()
    private val _currentPreference = MutableStateFlow(persisted)
    override val currentPreference: StateFlow<AppThemePreference> = _currentPreference.asStateFlow()

    override fun changeThemePreference(preference: AppThemePreference) {
        if (preference == _currentPreference.value) return
        settings.putString(AppThemePreferences.SETTINGS_KEY, AppThemePreferences.storageValue(preference))
        _currentPreference.value = preference
    }

    private fun readPersisted(): AppThemePreference {
        val stored = settings.getString(
            AppThemePreferences.SETTINGS_KEY,
            AppThemePreferences.storageValue(AppThemePreferences.DEFAULT),
        )
        return AppThemePreferences.normalize(stored)
    }
}
