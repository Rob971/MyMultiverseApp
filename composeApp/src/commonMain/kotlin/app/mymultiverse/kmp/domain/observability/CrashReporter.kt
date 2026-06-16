package app.mymultiverse.kmp.domain.observability

/**
 * Platform port for crash and non-fatal error reporting (Firebase Crashlytics on Android).
 */
interface CrashReporter {
    fun initialize()
    fun setUserId(userId: String?)
    fun logBreadcrumb(message: String)
    fun recordNonFatal(throwable: Throwable, context: Map<String, String> = emptyMap())
}
