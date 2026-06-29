package app.mymultiverse.ammo.presentation.di

import android.content.Context
import app.mymultiverse.ammo.data.observability.FirebaseBuildFlags
import app.mymultiverse.ammo.data.observability.FirebaseCrashReporter
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.manager.AndroidLanguageManager
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.observability.CrashReporter
import app.mymultiverse.ammo.data.platform.AndroidFcmTokenProvider
import app.mymultiverse.ammo.data.platform.AndroidPersonalDataExporter
import app.mymultiverse.ammo.data.platform.NoOpPushNotificationRegistrar
import app.mymultiverse.ammo.data.platform.SupabasePushNotificationRegistrar
import app.mymultiverse.ammo.data.supabase.SupabaseClientHolder
import app.mymultiverse.ammo.domain.platform.PersonalDataExporter
import app.mymultiverse.ammo.domain.platform.PushNotificationRegistrar
import app.mymultiverse.ammo.push.PushTokenRefreshStore
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
            if (FirebaseBuildFlags.PUSH_ENABLED) {
                SupabasePushNotificationRegistrar(client, "android") {
                    PushTokenRefreshStore.consume(context)
                        ?: AndroidFcmTokenProvider.getToken(context)
                }
            } else {
                SupabasePushNotificationRegistrar(client, "android") {
                    "android-stub-${context.packageName}"
                }
            }
        } else {
            NoOpPushNotificationRegistrar()
        }
    }
}
