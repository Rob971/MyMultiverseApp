package app.mymultiverse.ammo.domain.sharing

/**
 * Upload constraints enforced server-side by the Supabase `member-avatars` bucket
 * (supabase/config.toml): objects above 5MiB are rejected with 413 Payload too large,
 * and mime types outside jpeg/png/webp are rejected with 415 invalid_mime_type.
 */
const val AVATAR_MAX_UPLOAD_BYTES: Int = 5 * 1024 * 1024

/** Longest edge used when downscaling gallery photos before avatar upload. */
const val AVATAR_MAX_DIMENSION_PX: Int = 1280

private val supportedAvatarContentTypes = setOf("image/jpeg", "image/png", "image/webp")

/** True when the bucket accepts this content type as-is (exact allowed list, no `image/jpg`). */
fun isSupportedAvatarContentType(contentType: String): Boolean =
    contentType.substringBefore(';').trim().lowercase() in supportedAvatarContentTypes

/**
 * True when picked image bytes must be re-encoded client-side (downscale + JPEG)
 * because the storage bucket would reject the upload outright.
 */
fun avatarUploadRequiresTranscode(byteCount: Int, contentType: String): Boolean =
    !isSupportedAvatarContentType(contentType) || byteCount > AVATAR_MAX_UPLOAD_BYTES
