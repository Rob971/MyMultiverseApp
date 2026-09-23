package app.mymultiverse.ammo.presentation.di

import android.content.Context
import app.mymultiverse.ammo.domain.manager.AndroidLanguageManager
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.data.platform.AndroidDeviceRegionService
import app.mymultiverse.ammo.data.platform.AndroidPersonalDataExporter
import app.mymultiverse.ammo.domain.location.DeviceRegionService
import app.mymultiverse.ammo.domain.platform.PersonalDataExporter
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<Settings> {
        val context = androidContext()
        SharedPreferencesSettings(context.getSharedPreferences("app_settings", Context.MODE_PRIVATE))
    }
    single<LanguageManager> { AndroidLanguageManager(androidContext(), get()) }
    single<PersonalDataExporter> { AndroidPersonalDataExporter(androidContext()) }
    single<DeviceRegionService> { AndroidDeviceRegionService(androidContext()) }
}
