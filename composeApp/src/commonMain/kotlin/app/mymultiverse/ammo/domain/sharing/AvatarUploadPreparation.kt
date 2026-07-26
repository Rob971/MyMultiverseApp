package app.mymultiverse.ammo.domain.sharing

/**
 * Shared helpers for preparing avatar uploads to the `member-avatars` bucket.
 *
 * The bucket allowlist is `image/jpeg|png|webp` with a 5 MiB limit (see
 * `supabase/config.toml`). Android gallery picks often return `image/heic`,
 * `image/jpg`, or multi-megabyte camera originals — all of which Storage rejects
 * with 415/413, which the UI previously mapped to a misleading “check connection”
 * error.
 */
data class PreparedAvatarUpload(
    val bytes: ByteArray,
    val contentType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PreparedAvatarUpload
        return contentType == other.contentType && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = 31 * contentType.hashCode() + bytes.contentHashCode()
}

object AvatarUploadPreparation {
    /** Matches `[storage.buckets.member-avatars] file_size_limit` in config.toml. */
    const val MAX_UPLOAD_BYTES: Int = 5 * 1024 * 1024

    /** Longest edge after downscale — enough for circular avatars, keeps payloads small. */
    const val MAX_EDGE_PX: Int = 1280

    val BUCKET_ALLOWED_MIME_TYPES: Set<String> = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
    )

    /**
     * Normalizes picker / resolver MIME strings to a bucket-safe value.
     * Unknown or HEIC/HEIF types become `image/jpeg` (callers must convert bytes).
     */
    fun normalizeContentType(raw: String): String {
        val mime = raw.substringBefore(';').trim().lowercase()
        return when (mime) {
            "image/png" -> "image/png"
            "image/webp" -> "image/webp"
            "image/jpeg", "image/jpg", "image/pjpeg" -> "image/jpeg"
            else -> "image/jpeg"
        }
    }

    fun exceedsSizeLimit(byteCount: Int): Boolean = byteCount > MAX_UPLOAD_BYTES

    /** True when the raw picker MIME is already accepted by the bucket (after jpg→jpeg). */
    fun isPassthroughMime(contentType: String): Boolean {
        val mime = contentType.substringBefore(';').trim().lowercase()
        return mime in BUCKET_ALLOWED_MIME_TYPES || mime == "image/jpg" || mime == "image/pjpeg"
    }
}
