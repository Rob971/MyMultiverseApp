@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package app.mymultiverse.ammo.domain.observability

import app.mymultiverse.ammo.domain.AppBuildInfo
import kotlin.uuid.Uuid

/**
 * Session-scoped diagnostic metadata attached to logs and crash reports.
 * Do not store PII beyond opaque user/household identifiers.
 */
class DiagnosticsContext(
    val sessionId: String = Uuid.random().toString(),
) {
    var userId: String? = null
    var activeHouseholdId: String? = null
    var localeTag: String? = null

    fun snapshot(): Map<String, String> = buildMap {
        put(KEY_SESSION_ID, sessionId)
        put(KEY_APP_VERSION, AppBuildInfo.VERSION_NAME)
        put(KEY_BUILD_CODE, AppBuildInfo.VERSION_CODE.toString())
        userId?.let { put(KEY_USER_ID, it) }
        activeHouseholdId?.let { put(KEY_ACTIVE_HOUSEHOLD_ID, it) }
        localeTag?.let { put(KEY_LOCALE, it) }
    }

    private companion object {
        const val KEY_SESSION_ID = "session_id"
        const val KEY_APP_VERSION = "app_version"
        const val KEY_BUILD_CODE = "build_code"
        const val KEY_USER_ID = "user_id"
        const val KEY_ACTIVE_HOUSEHOLD_ID = "active_household_id"
        const val KEY_LOCALE = "locale"
    }
}
