package app.mymultiverse.ammo.presentation.di

import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.observability.CrashReporter
import app.mymultiverse.ammo.domain.manager.IOSLanguageManager
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.data.platform.IosApnsTokenProvider
import app.mymultiverse.ammo.data.platform.IosDeviceRegionService
import app.mymultiverse.ammo.data.platform.IosNetworkConnectivityMonitor
import app.mymultiverse.ammo.data.platform.NoOpPushNotificationRegistrar
import app.mymultiverse.ammo.data.platform.SupabasePushNotificationRegistrar
import app.mymultiverse.ammo.data.supabase.SupabaseClientHolder
import app.mymultiverse.ammo.domain.sync.NetworkConnectivityMonitor
import app.mymultiverse.ammo.domain.platform.PersonalDataExporter
import app.mymultiverse.ammo.domain.platform.PushNotificationRegistrar
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
    single<DeviceRegionService> { IosDeviceRegionService() }
    single<NetworkConnectivityMonitor> { IosNetworkConnectivityMonitor() }
    single<PushNotificationRegistrar> {
        val client = get<SupabaseClientHolder>().client
        if (client != null) {
            SupabasePushNotificationRegistrar(client, "ios", {
                IosApnsTokenProvider.currentToken()
            }) {
                get<LanguageManager>().currentLanguage.value
            }
        } else {
            NoOpPushNotificationRegistrar()
        }
    }
}
