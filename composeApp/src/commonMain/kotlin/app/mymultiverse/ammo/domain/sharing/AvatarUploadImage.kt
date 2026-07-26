package app.mymultiverse.ammo.domain.sharing

/**
 * Client-side constraints for avatar uploads to the `member-avatars` bucket.
 *
 * Bucket policy (supabase/config.toml): jpeg/png/webp only, historically 5 MiB.
 * Android gallery often reports `image/jpg` / HEIC / oversized camera JPEGs — those
 * must be normalized or re-encoded before Storage or the API returns 415/413 and the
 * UI shows a generic "check your connection" failure.
 */
object AvatarUploadImage {
    const val MAX_EDGE_PX = 1_280
    const val JPEG_QUALITY = 85
    /** Soft target after re-encode; keeps uploads under the storage limit with headroom. */
    const val TARGET_MAX_BYTES = 4_500_000
    const val UNSUPPORTED_ERROR_CODE = "avatar_unsupported_image"

    val allowedContentTypes: Set<String> = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
    )

    fun normalizeContentType(raw: String): String {
        val bare = raw.substringBefore(';').trim().lowercase()
        return when (bare) {
            "image/jpg", "image/pjpeg", "image/x-jpeg" -> "image/jpeg"
            "image/x-png" -> "image/png"
            "image/x-webp" -> "image/webp"
            else -> bare
        }
    }

    fun isAllowedContentType(contentType: String): Boolean =
        normalizeContentType(contentType) in allowedContentTypes

    fun storageFailureReason(message: String?): AvatarUploadFailureReason? {
        if (message.isNullOrBlank()) return null
        val lower = message.lowercase()
        return when {
            lower.contains("invalid_mime") ||
                (lower.contains("mime type") && lower.contains("not supported")) ||
                lower.contains(UNSUPPORTED_ERROR_CODE) ->
                AvatarUploadFailureReason.InvalidMime
            lower.contains("payload too large") ||
                lower.contains("exceeded the maximum allowed size") ||
                lower.contains("entity too large") ->
                AvatarUploadFailureReason.PayloadTooLarge
            else -> null
        }
    }
}

class AvatarUnsupportedImageException :
    IllegalArgumentException(AvatarUploadImage.UNSUPPORTED_ERROR_CODE)
