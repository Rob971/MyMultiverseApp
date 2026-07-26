package app.mymultiverse.ammo.presentation.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.mymultiverse.ammo.domain.sharing.AVATAR_MAX_DIMENSION_PX
import app.mymultiverse.ammo.domain.sharing.AVATAR_MAX_UPLOAD_BYTES
import app.mymultiverse.ammo.domain.sharing.avatarUploadRequiresTranscode
import java.io.ByteArrayOutputStream

/** Picked photo bytes and the content type they will be uploaded with. */
class PreparedAvatarImage(val bytes: ByteArray, val contentType: String)

/**
 * Ensures a picked gallery photo satisfies the `member-avatars` bucket constraints
 * (5MiB limit, jpeg/png/webp only) by downscaling to [AVATAR_MAX_DIMENSION_PX] and
 * re-encoding to JPEG when the original bytes would be rejected server-side —
 * typically multi-megabyte camera photos or HEIC captures.
 *
 * Bytes that already pass the bucket checks are returned untouched. Undecodable
 * inputs are also returned untouched so the existing upload error surface reports them.
 */
fun prepareAvatarImageForUpload(bytes: ByteArray, contentType: String): PreparedAvatarImage {
    if (!avatarUploadRequiresTranscode(bytes.size, contentType)) {
        return PreparedAvatarImage(bytes, contentType)
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    val longestEdge = maxOf(bounds.outWidth, bounds.outHeight)
    if (longestEdge <= 0) return PreparedAvatarImage(bytes, contentType)

    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSizeFor(longestEdge) }
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
        ?: return PreparedAvatarImage(bytes, contentType)
    val scaled = scaleToMaxDimension(decoded)

    var quality = INITIAL_JPEG_QUALITY
    var encoded = scaled.encodeJpeg(quality)
    while (encoded.size > AVATAR_MAX_UPLOAD_BYTES && quality > MIN_JPEG_QUALITY) {
        quality -= JPEG_QUALITY_STEP
        encoded = scaled.encodeJpeg(quality)
    }

    if (scaled !== decoded) decoded.recycle()
    scaled.recycle()
    return PreparedAvatarImage(encoded, "image/jpeg")
}

/** Largest power-of-two subsampling that keeps the decoded longest edge >= target size. */
private fun sampleSizeFor(longestEdge: Int): Int {
    var sampleSize = 1
    while (longestEdge / (sampleSize * 2) >= AVATAR_MAX_DIMENSION_PX) {
        sampleSize *= 2
    }
    return sampleSize
}

private fun scaleToMaxDimension(source: Bitmap): Bitmap {
    val longestEdge = maxOf(source.width, source.height)
    if (longestEdge <= AVATAR_MAX_DIMENSION_PX) return source
    val scale = AVATAR_MAX_DIMENSION_PX.toFloat() / longestEdge
    return Bitmap.createScaledBitmap(
        source,
        (source.width * scale).toInt().coerceAtLeast(1),
        (source.height * scale).toInt().coerceAtLeast(1),
        true,
    )
}

private fun Bitmap.encodeJpeg(quality: Int): ByteArray {
    val output = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, output)
    return output.toByteArray()
}

private const val INITIAL_JPEG_QUALITY = 90
private const val MIN_JPEG_QUALITY = 40
private const val JPEG_QUALITY_STEP = 10
