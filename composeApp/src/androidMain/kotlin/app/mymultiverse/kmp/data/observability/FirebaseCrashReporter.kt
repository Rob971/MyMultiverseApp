package app.mymultiverse.kmp.data.observability

import android.content.Context
import app.mymultiverse.kmp.domain.observability.CrashReporter
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseCrashReporter : CrashReporter {
    private var initialized = false

    override fun initialize() {
        if (initialized) return
        runCatching {
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
            initialized = true
        }
    }

    override fun setUserId(userId: String?) {
        runCatching {
            FirebaseCrashlytics.getInstance().setUserId(userId.orEmpty())
        }
    }

    override fun logBreadcrumb(message: String) {
        runCatching {
            FirebaseCrashlytics.getInstance().log(message.take(BREADCRUMB_MAX_LENGTH))
        }
    }

    override fun recordNonFatal(throwable: Throwable, context: Map<String, String>) {
        runCatching {
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

    companion object {
        private const val BREADCRUMB_MAX_LENGTH = 1_000
        private const val CUSTOM_KEY_MAX_LENGTH = 40
        private const val CUSTOM_VALUE_MAX_LENGTH = 100

        fun isAvailable(context: Context): Boolean =
            runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)
    }
}
