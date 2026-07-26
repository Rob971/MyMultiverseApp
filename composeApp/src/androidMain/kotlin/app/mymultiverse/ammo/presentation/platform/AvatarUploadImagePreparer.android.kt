package app.mymultiverse.ammo.presentation.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.mymultiverse.ammo.domain.sharing.AvatarUnsupportedImageException
import app.mymultiverse.ammo.domain.sharing.AvatarUploadImage
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Decodes gallery bytes (including HEIC on supported devices), downscales, and re-encodes
 * as JPEG so Storage's mime allow-list and size limit accept the upload.
 */
internal fun prepareAvatarBytesForUpload(
    imageBytes: ByteArray,
    reportedContentType: String,
): Pair<ByteArray, String> {
    require(imageBytes.isNotEmpty()) { "avatar_image_required" }
    val normalized = AvatarUploadImage.normalizeContentType(reportedContentType)
    val decoded = decodeDownsampledBitmap(imageBytes, AvatarUploadImage.MAX_EDGE_PX)
    if (decoded != null) {
        try {
            val jpegBytes = encodeJpegUnderLimit(decoded)
            if (jpegBytes.isNotEmpty()) {
                return jpegBytes to "image/jpeg"
            }
        } finally {
            decoded.recycle()
        }
    }
    if (AvatarUploadImage.isAllowedContentType(normalized) &&
        imageBytes.size <= AvatarUploadImage.TARGET_MAX_BYTES
    ) {
        return imageBytes to normalized
    }
    throw AvatarUnsupportedImageException()
}

private fun decodeDownsampledBitmap(imageBytes: ByteArray, maxEdgePx: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxEdgePx)
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        ?: return null
    val longest = max(bitmap.width, bitmap.height)
    if (longest <= maxEdgePx) return bitmap

    val scale = maxEdgePx.toFloat() / longest.toFloat()
    val width = max(1, (bitmap.width * scale).roundToInt())
    val height = max(1, (bitmap.height * scale).roundToInt())
    val scaled = Bitmap.createScaledBitmap(bitmap, width, height, true)
    if (scaled !== bitmap) {
        bitmap.recycle()
    }
    return scaled
}

private fun calculateInSampleSize(width: Int, height: Int, maxEdgePx: Int): Int {
    var sampleSize = 1
    var halfWidth = width / 2
    var halfHeight = height / 2
    while (halfWidth / sampleSize >= maxEdgePx && halfHeight / sampleSize >= maxEdgePx) {
        sampleSize *= 2
    }
    // Also shrink when only one edge is huge.
    while (max(width / sampleSize, height / sampleSize) > maxEdgePx * 2) {
        sampleSize *= 2
    }
    return sampleSize.coerceAtLeast(1)
}

private fun encodeJpegUnderLimit(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    var quality = AvatarUploadImage.JPEG_QUALITY
    var bytes: ByteArray
    do {
        stream.reset()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        bytes = stream.toByteArray()
        quality -= 10
    } while (bytes.size > AvatarUploadImage.TARGET_MAX_BYTES && quality >= 50)
    return bytes
}
