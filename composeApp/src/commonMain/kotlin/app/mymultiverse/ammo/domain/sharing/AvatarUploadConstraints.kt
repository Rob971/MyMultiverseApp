package app.mymultiverse.ammo.domain.sharing

/**
 * Avatar upload limits — keep in sync with `supabase/config.toml`
 * (`[storage.buckets.member-avatars].file_size_limit = "5MiB"`).
 */
object AvatarUploadConstraints {
    /** Hard cap enforced by the `member-avatars` Storage bucket and the client upload path. */
    const val HARD_LIMIT_BYTES: Int = 5 * 1024 * 1024

    /** Best-practice payload size after client-side resize/compress. */
    const val UPLOAD_TARGET_BYTES: Int = 2 * 1024 * 1024

    /**
     * Gallery picks above this are rejected before decode to avoid OOM on low-memory devices.
     * Well above [HARD_LIMIT_BYTES] so normal photos are still compressed, not blocked outright.
     */
    const val MAX_PICK_BYTES: Int = 25 * 1024 * 1024

    /** Longest edge after downscaling — keeps avatars crisp without multi‑MiB payloads. */
    const val MAX_DIMENSION_PX: Int = 1024

    @Deprecated(
        message = "Use HARD_LIMIT_BYTES",
        replaceWith = ReplaceWith("HARD_LIMIT_BYTES"),
    )
    const val MAX_STORAGE_BYTES: Int = HARD_LIMIT_BYTES

    @Deprecated(
        message = "Use UPLOAD_TARGET_BYTES",
        replaceWith = ReplaceWith("UPLOAD_TARGET_BYTES"),
    )
    const val TARGET_MAX_BYTES: Int = UPLOAD_TARGET_BYTES
}

/** User-facing megabyte cap for i18n placeholders (matches [AvatarUploadConstraints.HARD_LIMIT_BYTES]). */
fun avatarMaxFileSizeMegabytes(): Int = AvatarUploadConstraints.HARD_LIMIT_BYTES / (1024 * 1024)

/**
 * Ensures prepared bytes respect the Storage bucket hard limit.
 * @throws AvatarImagePrepareException when [bytes] exceeds [AvatarUploadConstraints.HARD_LIMIT_BYTES].
 */
fun validatePreparedAvatarBytes(bytes: ByteArray) {
    if (bytes.size > AvatarUploadConstraints.HARD_LIMIT_BYTES) {
        throw AvatarImagePrepareException(
            reason = AvatarImagePrepareException.Reason.TooLarge,
            limitBytes = AvatarUploadConstraints.HARD_LIMIT_BYTES,
            actualBytes = bytes.size,
        )
    }
}

/**
 * Maps gallery MIME types to values accepted by the `member-avatars` bucket
 * (`image/jpeg`, `image/png`, `image/webp`).
 */
fun normalizeAvatarContentType(contentType: String): String {
    val normalized = contentType.substringBefore(';').trim().lowercase()
    return when {
        normalized.isEmpty() -> "image/jpeg"
        normalized == "image/jpg" -> "image/jpeg"
        normalized == "image/pjpeg" -> "image/jpeg"
        normalized == "image/heic" -> "image/jpeg"
        normalized == "image/heif" -> "image/jpeg"
        normalized == "image/heic-sequence" -> "image/jpeg"
        normalized == "image/heif-sequence" -> "image/jpeg"
        normalized == "image/jpeg" -> "image/jpeg"
        normalized == "image/png" -> "image/png"
        normalized == "image/webp" -> "image/webp"
        normalized.contains("jpeg") -> "image/jpeg"
        normalized.contains("png") -> "image/png"
        normalized.contains("webp") -> "image/webp"
        normalized.contains("heic") || normalized.contains("heif") -> "image/jpeg"
        else -> normalized
    }
}

fun isAvatarContentTypeSupported(contentType: String): Boolean =
    when (normalizeAvatarContentType(contentType)) {
        "image/jpeg", "image/png", "image/webp" -> true
        else -> false
    }
