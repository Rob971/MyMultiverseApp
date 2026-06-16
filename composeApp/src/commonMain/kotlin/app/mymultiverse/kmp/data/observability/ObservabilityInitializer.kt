package app.mymultiverse.kmp.data.observability

import app.mymultiverse.kmp.domain.observability.CrashReporter
import app.mymultiverse.kmp.domain.observability.DiagnosticsContext

object ObservabilityInitializer {
    fun start(
        logger: AppLogger,
        crashReporter: CrashReporter,
        diagnostics: DiagnosticsContext,
    ) {
        crashReporter.initialize()
        logger.breadcrumb("session_start session_id=${diagnostics.sessionId}")
    }
}
