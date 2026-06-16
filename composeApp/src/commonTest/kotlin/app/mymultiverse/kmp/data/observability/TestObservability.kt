package app.mymultiverse.kmp.data.observability

import app.mymultiverse.kmp.domain.observability.DiagnosticsContext

object TestObservability {
    val diagnostics = DiagnosticsContext(sessionId = "test-session")
    val logger = AppLogger(NoOpCrashReporter(), diagnostics)
}
