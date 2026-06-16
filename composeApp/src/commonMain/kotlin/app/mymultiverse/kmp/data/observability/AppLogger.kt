package app.mymultiverse.kmp.data.observability

import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.observability.CrashReporter
import app.mymultiverse.kmp.domain.observability.DiagnosticsContext
import co.touchlab.kermit.Logger

class AppLogger(
    private val crashReporter: CrashReporter,
    private val diagnostics: DiagnosticsContext,
) {
    fun startSession() {
        crashReporter.initialize()
        breadcrumb("session_start session_id=${diagnostics.sessionId}")
    }

    fun breadcrumb(message: String) {
        crashReporter.logBreadcrumb(message)
        Logger.d(tag = ROOT_TAG) { enrich(message) }
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

    fun onAuthStateChanged(state: AuthState) {
        when (state) {
            is AuthState.Authenticated -> {
                diagnostics.userId = state.user.id
                crashReporter.setUserId(state.user.id)
                breadcrumb("auth_authenticated")
            }
            AuthState.Unauthenticated -> {
                diagnostics.userId = null
                crashReporter.setUserId(null)
            }
            else -> Unit
        }
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
