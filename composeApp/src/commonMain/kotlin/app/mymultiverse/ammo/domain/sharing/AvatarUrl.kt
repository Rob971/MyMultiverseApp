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
        contentType.contains("png", ignoreCase = true) -> "png"
        contentType.contains("webp", ignoreCase = true) -> "webp"
        else -> "jpg"
    }
