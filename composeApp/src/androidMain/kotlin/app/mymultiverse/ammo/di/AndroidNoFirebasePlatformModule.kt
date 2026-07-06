package app.mymultiverse.ammo.di

import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.data.platform.NoOpPushNotificationRegistrar
import app.mymultiverse.ammo.data.platform.SupabasePushNotificationRegistrar
import app.mymultiverse.ammo.data.supabase.SupabaseClientHolder
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.observability.CrashReporter
import app.mymultiverse.ammo.domain.platform.PushNotificationRegistrar
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

fun androidNoFirebasePlatformModule(): Module = module {
    single<CrashReporter> { NoOpCrashReporter() }
    single<PushNotificationRegistrar> {
        val client = get<SupabaseClientHolder>().client
        if (client != null) {
            val context = androidContext()
            SupabasePushNotificationRegistrar(client, "android", {
                "android-stub-${context.packageName}"
            }) {
                get<LanguageManager>().currentLanguage.value
            }
        } else {
            NoOpPushNotificationRegistrar()
        }
    }
}
