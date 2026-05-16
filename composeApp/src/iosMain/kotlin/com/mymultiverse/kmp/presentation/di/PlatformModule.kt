package com.mymultiverse.kmp.presentation.di

import com.mymultiverse.kmp.database.DatabaseDriverFactory
import com.mymultiverse.kmp.domain.manager.IOSLanguageManager
import com.mymultiverse.kmp.domain.manager.LanguageManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DatabaseDriverFactory() }
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single<LanguageManager> { IOSLanguageManager(get()) }
}
