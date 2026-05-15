package com.example.kmp.presentation.di

import com.example.kmp.database.DatabaseDriverFactory
import com.example.kmp.domain.manager.IOSLanguageManager
import com.example.kmp.domain.manager.LanguageManager
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
