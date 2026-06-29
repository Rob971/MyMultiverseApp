package app.mymultiverse.ammo.data.observability

import app.mymultiverse.ammo.domain.observability.DiagnosticsContext

object TestObservability {
    val diagnostics = DiagnosticsContext(sessionId = "test-session")
    val logger = AppLogger(NoOpCrashReporter(), diagnostics)
}
