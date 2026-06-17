package app.mymultiverse.kmp.presentation.di

import android.content.Context
import app.mymultiverse.kmp.data.observability.FirebaseBuildFlags
import app.mymultiverse.kmp.data.observability.FirebaseCrashReporter
import app.mymultiverse.kmp.data.observability.NoOpCrashReporter
import app.mymultiverse.kmp.domain.manager.AndroidLanguageManager
import app.mymultiverse.kmp.domain.manager.LanguageManager
import app.mymultiverse.kmp.domain.observability.CrashReporter
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
    single<CrashReporter> {
        if (FirebaseBuildFlags.CRASHLYTICS_ENABLED) {
            FirebaseCrashReporter(androidContext())
        } else {
            NoOpCrashReporter()
        }
    }
}
