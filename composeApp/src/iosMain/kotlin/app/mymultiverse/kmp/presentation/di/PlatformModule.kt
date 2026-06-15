package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.manager.IOSLanguageManager
import app.mymultiverse.kmp.domain.manager.LanguageManager
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule(): Module = module {
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single<LanguageManager> { IOSLanguageManager(get()) }
}
