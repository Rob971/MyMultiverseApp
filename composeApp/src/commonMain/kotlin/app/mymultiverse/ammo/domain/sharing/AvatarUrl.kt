package app.mymultiverse.ammo.domain.sharing

import kotlinx.datetime.Clock

/**
 * Appends a version query parameter so clients refetch the image after re-uploads to
 * the same storage path (Supabase public URLs are stable per object key).
 */
fun versionedAvatarUrl(
    publicUrl: String,
    versionMs: Long = Clock.System.now().toEpochMilliseconds(),
): String {
    val base = publicUrl.substringBefore('?').trimEnd('/')
    return "$base?v=$versionMs"
}

fun avatarExtensionForContentType(contentType: String): String =
    when {
        contentType.contains("heic", ignoreCase = true) -> "heic"
        contentType.contains("heif", ignoreCase = true) -> "heif"
        contentType.contains("png", ignoreCase = true) -> "png"
        contentType.contains("webp", ignoreCase = true) -> "webp"
        else -> "jpg"
    }

fun normalizeAvatarContentType(contentType: String): String {
    val normalized = contentType.trim().lowercase()
    if (normalized.isBlank()) return "image/jpeg"
    return when {
        normalized == "image/*" -> "image/jpeg"
        normalized == "image/jpg" -> "image/jpeg"
        normalized.startsWith("image/heic") -> "image/heic"
        normalized.startsWith("image/heif") -> "image/heif"
        normalized.startsWith("image/png") -> "image/png"
        normalized.startsWith("image/webp") -> "image/webp"
        normalized.startsWith("image/jpeg") -> "image/jpeg"
        else -> "image/jpeg"
    }
}
