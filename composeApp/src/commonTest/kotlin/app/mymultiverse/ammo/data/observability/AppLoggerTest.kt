package app.mymultiverse.ammo.data.observability

import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppLoggerTest {

    @Test
    fun recordError_forwardsToCrashReporterWithDiagnostics() {
        val crashReporter = RecordingCrashReporter()
        val diagnostics = app.mymultiverse.ammo.domain.observability.DiagnosticsContext(sessionId = "sess-1")
        val logger = AppLogger(crashReporter, diagnostics)
        logger.startSession()

        val error = IllegalStateException("sync_failed")
        logger.recordError(
            tag = "NutritionSync",
            message = "push_failed",
            throwable = error,
            context = mapOf("data_kind" to "grocery"),
        )

        assertEquals(1, crashReporter.nonFatals.size)
        val recorded = crashReporter.nonFatals.single()
        assertTrue(recorded.throwable === error)
        assertEquals("sess-1", recorded.context["session_id"])
        assertEquals("grocery", recorded.context["data_kind"])
        assertEquals("push_failed", recorded.context["message"])
    }

    @Test
    fun onAuthStateChanged_setsUserIdOnCrashReporter() {
        val crashReporter = RecordingCrashReporter()
        val logger = AppLogger(
            crashReporter,
            app.mymultiverse.ammo.domain.observability.DiagnosticsContext(sessionId = "sess-2"),
        )

        logger.onAuthStateChanged(
            AuthState.Authenticated(
                AuthUser(id = "user-42", email = "a@b.c", displayName = "Tester"),
            ),
        )

        assertEquals("user-42", crashReporter.recordedUserId)
    }

    private class RecordingCrashReporter : app.mymultiverse.ammo.domain.observability.CrashReporter {
        var recordedUserId: String? = null
        val nonFatals = mutableListOf<RecordedNonFatal>()

        data class RecordedNonFatal(
            val throwable: Throwable,
            val context: Map<String, String>,
        )

        override fun initialize() = Unit

        override fun setUserId(userId: String?) {
            recordedUserId = userId
        }

        override fun logBreadcrumb(message: String) = Unit

        override fun recordNonFatal(throwable: Throwable, context: Map<String, String>) {
            nonFatals += RecordedNonFatal(throwable, context)
        }
    }
}
