package app.mymultiverse.ammo.data.observability

import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.observability.CrashReporter
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.domain.sharing.AvatarPersistException
import app.mymultiverse.ammo.domain.sharing.AvatarUploadFailureReason
import app.mymultiverse.ammo.domain.sharing.AvatarUploadStep
import app.mymultiverse.ammo.domain.sharing.AvatarUploadTarget
import app.mymultiverse.ammo.domain.sharing.AvatarUploadTelemetry
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

    fun logAvatarUploadStep(
        target: AvatarUploadTarget,
        step: AvatarUploadStep,
        householdId: String,
        extra: String = "",
        context: Map<String, String> = emptyMap(),
    ) {
        breadcrumb(
            AvatarUploadTelemetry.breadcrumb(
                target = target,
                step = step,
                householdId = householdId,
                extra = extra,
            ),
        )
        if (context.isNotEmpty()) {
            Logger.d(tag = AVATAR_UPLOAD_TAG) {
                enrich(
                    AvatarUploadTelemetry.breadcrumb(target, step, householdId, extra),
                    AvatarUploadTelemetry.context(
                        target = target,
                        step = step,
                        householdId = householdId,
                    ) + context,
                )
            }
        }
    }

    fun recordAvatarUploadFailure(
        target: AvatarUploadTarget,
        step: AvatarUploadStep,
        householdId: String,
        throwable: Throwable,
        storagePath: String? = null,
        memberId: String? = null,
        dbTable: String? = null,
        contentType: String? = null,
        imageBytes: Int? = null,
    ) {
        val resolvedDbTable = dbTable ?: (throwable as? AvatarPersistException)?.dbTable
        val failureReason = AvatarUploadTelemetry.failureReasonFor(throwable)
        val message = when (failureReason) {
            AvatarUploadFailureReason.RlsZeroRows ->
                AvatarUploadTelemetry.persistFailureMessage(
                    target = target,
                    dbTable = resolvedDbTable.orEmpty(),
                )
            else -> "avatar_upload_failed target=${target.telemetryValue} step=${step.telemetryValue}"
        }
        recordError(
            tag = AVATAR_UPLOAD_TAG,
            message = message,
            throwable = throwable,
            context = AvatarUploadTelemetry.context(
                target = target,
                step = step,
                householdId = householdId,
                storagePath = storagePath,
                memberId = memberId,
                dbTable = resolvedDbTable,
                contentType = contentType,
                imageBytes = imageBytes,
                failureReason = failureReason,
            ),
        )
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
                breadcrumb("auth_unauthenticated")
            }
            AuthState.ConfigurationMissing -> breadcrumb("auth_config_missing")
            else -> Unit
        }
    }

    fun setLocale(tag: String) {
        diagnostics.localeTag = tag
        breadcrumb("locale_set lang=$tag")
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
        const val ROOT_TAG = "Ammò"
        const val AVATAR_UPLOAD_TAG = "AvatarUpload"
    }
}
