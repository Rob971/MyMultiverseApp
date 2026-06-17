package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.data.observability.NoOpCrashReporter
import app.mymultiverse.kmp.domain.manager.IOSLanguageManager
import app.mymultiverse.kmp.domain.manager.LanguageManager
import app.mymultiverse.kmp.data.platform.IosPersonalDataExporter
import app.mymultiverse.kmp.data.platform.NoOpPushNotificationRegistrar
import app.mymultiverse.kmp.data.platform.SupabasePushNotificationRegistrar
import app.mymultiverse.kmp.data.supabase.SupabaseClientHolder
import app.mymultiverse.kmp.domain.platform.PersonalDataExporter
import app.mymultiverse.kmp.domain.platform.PushNotificationRegistrar
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule(): Module = module {
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single<LanguageManager> { IOSLanguageManager(get()) }
    // iOS Crashlytics pending Firebase CocoaPods + GoogleService-Info.plist setup.
    single<CrashReporter> { NoOpCrashReporter() }
    single<PersonalDataExporter> { IosPersonalDataExporter() }
    single<PushNotificationRegistrar> {
        val client = get<SupabaseClientHolder>().client
        if (client != null) {
            SupabasePushNotificationRegistrar(client, "ios") { "ios-stub-token" }
        } else {
            NoOpPushNotificationRegistrar()
        }
    }
}
