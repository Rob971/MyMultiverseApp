package app.mymultiverse.kmp.data.observability

import android.content.Context
import app.mymultiverse.kmp.domain.AppBuildInfo
import app.mymultiverse.kmp.domain.observability.CrashReporter
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseCrashReporter(
    private val context: Context,
) : CrashReporter {
    private var initialized = false

    override fun initialize() {
        if (initialized) return
        runCatching {
            ensureFirebaseApp()
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.isCrashlyticsCollectionEnabled = true
            crashlytics.setCustomKey(KEY_APP_VERSION, AppBuildInfo.VERSION_NAME)
            crashlytics.setCustomKey(KEY_BUILD_CODE, AppBuildInfo.VERSION_CODE)
            initialized = true
        }
    }

    override fun setUserId(userId: String?) {
        runCatching {
            ensureFirebaseApp()
            FirebaseCrashlytics.getInstance().setUserId(userId.orEmpty())
        }
    }

    override fun logBreadcrumb(message: String) {
        runCatching {
            ensureFirebaseApp()
            FirebaseCrashlytics.getInstance().log(message.take(BREADCRUMB_MAX_LENGTH))
        }
    }

    override fun recordNonFatal(throwable: Throwable, context: Map<String, String>) {
        runCatching {
            ensureFirebaseApp()
            val crashlytics = FirebaseCrashlytics.getInstance()
            context.forEach { (key, value) ->
                crashlytics.setCustomKey(
                    key.take(CUSTOM_KEY_MAX_LENGTH),
                    value.take(CUSTOM_VALUE_MAX_LENGTH),
                )
            }
            crashlytics.recordException(throwable)
        }
    }

    private fun ensureFirebaseApp() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }

    private companion object {
        const val KEY_APP_VERSION = "app_version"
        const val KEY_BUILD_CODE = "build_code"
        const val BREADCRUMB_MAX_LENGTH = 1_000
        const val CUSTOM_KEY_MAX_LENGTH = 40
        const val CUSTOM_VALUE_MAX_LENGTH = 100
    }
}
