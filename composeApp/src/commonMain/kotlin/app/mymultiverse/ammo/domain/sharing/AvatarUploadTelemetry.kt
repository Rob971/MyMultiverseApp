package app.mymultiverse.ammo.domain.sharing

/**
 * Structured telemetry for avatar upload flows (Crashlytics custom keys + breadcrumbs).
 *
 * Use consistent key names so dashboards can filter on `avatar_failure_reason=rls_zero_rows`
 * without parsing free-form log messages.
 */
enum class AvatarUploadTarget(val telemetryValue: String) {
    Household("household"),
    MemberProfile("member_profile"),
    Dependant("dependant"),
}

enum class AvatarUploadStep(val telemetryValue: String) {
    Start("start"),
    StorageUpload("storage_upload"),
    DbPersist("db_persist"),
    Success("success"),
}

enum class AvatarUploadFailureReason(val telemetryValue: String) {
    /** PostgREST UPDATE returned 0 rows — usually missing SELECT/UPDATE RLS on the target table. */
    RlsZeroRows("rls_zero_rows"),
    InsufficientRole("insufficient_role"),
    /** Storage rejected Content-Type (HEIC, image/jpg, octet-stream, etc.). */
    InvalidMime("invalid_mime"),
    /** Storage rejected object larger than the bucket file_size_limit. */
    PayloadTooLarge("payload_too_large"),
    StorageUpload("storage_upload"),
    Unknown("unknown"),
}

/**
 * Thrown when Storage upload succeeded but the avatar URL could not be written to Postgres.
 * [message] stays stable for legacy string matching; prefer [target] and [dbTable] in new code.
 */
class AvatarPersistException(
    val target: AvatarUploadTarget,
    val dbTable: String,
    val householdId: String,
    val storagePath: String,
    val memberId: String? = null,
    val contentType: String? = null,
    val imageBytes: Int? = null,
) : IllegalStateException(ERROR_CODE) {

    companion object {
        const val ERROR_CODE = "avatar_db_update_no_rows"
    }
}

object AvatarUploadTelemetry {
    const val KEY_TARGET = "avatar_target"
    const val KEY_STEP = "avatar_step"
    const val KEY_FAILURE_REASON = "avatar_failure_reason"
    const val KEY_DB_TABLE = "avatar_db_table"
    const val KEY_HOUSEHOLD_ID = "household_id"
    const val KEY_MEMBER_ID = "member_id"
    const val KEY_STORAGE_PATH = "avatar_storage_path"
    const val KEY_CONTENT_TYPE = "avatar_content_type"
    const val KEY_IMAGE_BYTES = "avatar_image_bytes"

    fun failureReasonFor(throwable: Throwable): AvatarUploadFailureReason =
        when {
            throwable is AvatarPersistException -> AvatarUploadFailureReason.RlsZeroRows
            throwable is AvatarUnsupportedImageException -> AvatarUploadFailureReason.InvalidMime
            throwable.message?.contains(CollaborationErrorCodes.INSUFFICIENT_ROLE) == true ->
                AvatarUploadFailureReason.InsufficientRole
            throwable.message?.contains(AvatarPersistException.ERROR_CODE) == true ->
                AvatarUploadFailureReason.RlsZeroRows
            else -> AvatarUploadImage.storageFailureReason(throwable.message)
                ?: AvatarUploadFailureReason.Unknown
        }

    fun context(
        target: AvatarUploadTarget,
        step: AvatarUploadStep,
        householdId: String,
        storagePath: String? = null,
        memberId: String? = null,
        dbTable: String? = null,
        contentType: String? = null,
        imageBytes: Int? = null,
        failureReason: AvatarUploadFailureReason? = null,
    ): Map<String, String> = buildMap {
        put(KEY_TARGET, target.telemetryValue)
        put(KEY_STEP, step.telemetryValue)
        put(KEY_HOUSEHOLD_ID, householdId)
        storagePath?.let { put(KEY_STORAGE_PATH, it) }
        memberId?.let { put(KEY_MEMBER_ID, it) }
        dbTable?.let { put(KEY_DB_TABLE, it) }
        contentType?.let { put(KEY_CONTENT_TYPE, it) }
        imageBytes?.let { put(KEY_IMAGE_BYTES, it.toString()) }
        failureReason?.let { put(KEY_FAILURE_REASON, it.telemetryValue) }
    }

    fun breadcrumb(
        target: AvatarUploadTarget,
        step: AvatarUploadStep,
        householdId: String,
        extra: String = "",
    ): String {
        val suffix = if (extra.isBlank()) "" else " $extra"
        return "avatar_upload step=${step.telemetryValue} target=${target.telemetryValue} " +
            "household=$householdId$suffix"
    }

    fun persistFailureMessage(
        target: AvatarUploadTarget,
        dbTable: String,
    ): String =
        "avatar_db_persist_failed target=${target.telemetryValue} table=$dbTable — " +
            "check SELECT+UPDATE RLS policies are deployed"
}
