package app.mymultiverse.ammo.presentation.di

import app.mymultiverse.ammo.domain.manager.AppThemePreference
import app.mymultiverse.ammo.domain.manager.AppThemePreferences
import app.mymultiverse.ammo.domain.manager.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeThemeManager(
    initial: AppThemePreference = AppThemePreferences.DEFAULT,
) : ThemeManager {
    private val _currentPreference = MutableStateFlow(initial)
    override val currentPreference: StateFlow<AppThemePreference> = _currentPreference.asStateFlow()

    override fun changeThemePreference(preference: AppThemePreference) {
        _currentPreference.value = preference
    }
}
