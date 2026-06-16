package app.mymultiverse.kmp.data.observability

import app.mymultiverse.kmp.domain.observability.CrashReporter
import app.mymultiverse.kmp.domain.observability.DiagnosticsContext
import co.touchlab.kermit.Logger

class AppLogger(
    private val crashReporter: CrashReporter,
    private val diagnostics: DiagnosticsContext,
) {
    fun breadcrumb(message: String) {
        crashReporter.logBreadcrumb(message)
        Logger.d(tag = ROOT_TAG) { enrich(message) }
    }

    fun warn(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        context: Map<String, String> = emptyMap(),
    ) {
        val formatted = enrich(message, context)
        if (throwable != null) {
            Logger.w(tag = tag, throwable = throwable) { formatted }
        } else {
            Logger.w(tag = tag) { formatted }
        }
    }

    fun recordError(
        tag: String,
        message: String,
        throwable: Throwable,
        context: Map<String, String> = emptyMap(),
    ) {
        val merged = diagnostics.snapshot() + context + mapOf("message" to message)
        Logger.e(tag = tag, throwable = throwable) { formatContext(message, merged) }
        crashReporter.recordNonFatal(throwable, merged)
    }

    private fun enrich(message: String, context: Map<String, String> = emptyMap()): String =
        formatContext(message, diagnostics.snapshot() + context)

    private fun formatContext(message: String, context: Map<String, String>): String =
        if (context.isEmpty()) {
            message
        } else {
            "$message | ${context.entries.joinToString { "${it.key}=${it.value}" }}"
        }

    private companion object {
        const val ROOT_TAG = "MyMultiverse"
    }
}
