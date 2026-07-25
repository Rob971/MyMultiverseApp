package app.mymultiverse.ammo.data.observability

import app.mymultiverse.ammo.domain.sharing.AvatarPersistException
import app.mymultiverse.ammo.domain.sharing.AvatarUploadStep
import app.mymultiverse.ammo.domain.sharing.AvatarUploadTarget
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

    @Test
    fun recordAvatarUploadFailure_forwardsStructuredContext() {
        val crashReporter = RecordingCrashReporter()
        val diagnostics = app.mymultiverse.ammo.domain.observability.DiagnosticsContext(sessionId = "sess-3")
        val logger = AppLogger(crashReporter, diagnostics)

        val error = AvatarPersistException(
            target = AvatarUploadTarget.Household,
            dbTable = "households",
            householdId = "hh-9",
            storagePath = "households/hh-9/avatar.jpg",
        )
        logger.recordAvatarUploadFailure(
            target = AvatarUploadTarget.Household,
            step = AvatarUploadStep.DbPersist,
            householdId = "hh-9",
            throwable = error,
            storagePath = "households/hh-9/avatar.jpg",
            dbTable = "households",
        )

        assertEquals(1, crashReporter.nonFatals.size)
        val recorded = crashReporter.nonFatals.single()
        assertEquals("household", recorded.context["avatar_target"])
        assertEquals("db_persist", recorded.context["avatar_step"])
        assertEquals("rls_zero_rows", recorded.context["avatar_failure_reason"])
        assertEquals("households", recorded.context["avatar_db_table"])
        assertEquals("hh-9", recorded.context["household_id"])
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
