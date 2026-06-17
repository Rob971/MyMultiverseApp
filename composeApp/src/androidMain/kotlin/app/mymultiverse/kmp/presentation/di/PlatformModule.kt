package app.mymultiverse.kmp.presentation.di

import android.content.Context
import app.mymultiverse.kmp.data.observability.FirebaseBuildFlags
import app.mymultiverse.kmp.data.observability.FirebaseCrashReporter
import app.mymultiverse.kmp.data.observability.NoOpCrashReporter
import app.mymultiverse.kmp.domain.manager.AndroidLanguageManager
import app.mymultiverse.kmp.domain.manager.LanguageManager
import app.mymultiverse.kmp.domain.observability.CrashReporter
import app.mymultiverse.kmp.data.platform.AndroidPersonalDataExporter
import app.mymultiverse.kmp.data.platform.NoOpPushNotificationRegistrar
import app.mymultiverse.kmp.data.platform.SupabasePushNotificationRegistrar
import app.mymultiverse.kmp.data.supabase.SupabaseClientHolder
import app.mymultiverse.kmp.domain.platform.PersonalDataExporter
import app.mymultiverse.kmp.domain.platform.PushNotificationRegistrar
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
    single<PersonalDataExporter> { AndroidPersonalDataExporter(androidContext()) }
    single<PushNotificationRegistrar> {
        val client = get<SupabaseClientHolder>().client
        if (client != null) {
            val context = androidContext()
            SupabasePushNotificationRegistrar(client, "android") {
                "android-stub-${context.packageName}"
            }
        } else {
            NoOpPushNotificationRegistrar()
        }
    }
}
