package app.mymultiverse.ammo.data.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import app.mymultiverse.ammo.domain.sharing.AvatarImagePrepareException
import app.mymultiverse.ammo.domain.sharing.AvatarUploadConstraints
import app.mymultiverse.ammo.domain.sharing.isAvatarContentTypeSupported
import app.mymultiverse.ammo.domain.sharing.normalizeAvatarContentType
import app.mymultiverse.ammo.domain.sharing.validatePreparedAvatarBytes
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

actual fun prepareAvatarImageForUpload(
    rawBytes: ByteArray,
    contentType: String,
): PreparedAvatarImage {
    if (rawBytes.isEmpty()) {
        throw AvatarImagePrepareException(AvatarImagePrepareException.Reason.DecodeFailed)
    }
    if (rawBytes.size > AvatarUploadConstraints.MAX_PICK_BYTES) {
        throw AvatarImagePrepareException(
            reason = AvatarImagePrepareException.Reason.TooLarge,
            limitBytes = AvatarUploadConstraints.HARD_LIMIT_BYTES,
            actualBytes = rawBytes.size,
        )
    }

    val normalizedType = normalizeAvatarContentType(contentType)
    if (!isAvatarContentTypeSupported(normalizedType)) {
        throw AvatarImagePrepareException(AvatarImagePrepareException.Reason.UnsupportedFormat)
    }

    if (rawBytes.size <= AvatarUploadConstraints.UPLOAD_TARGET_BYTES &&
        normalizedType == "image/jpeg" &&
        isJpeg(rawBytes)
    ) {
        validatePreparedAvatarBytes(rawBytes)
        return PreparedAvatarImage(bytes = rawBytes, contentType = normalizedType)
    }

    val decoded = decodeBitmap(rawBytes)
        ?: throw AvatarImagePrepareException(AvatarImagePrepareException.Reason.DecodeFailed)

    val scaled = scaleToMaxDimension(decoded, AvatarUploadConstraints.MAX_DIMENSION_PX)
    if (scaled !== decoded) {
        decoded.recycle()
    }

    return try {
        compressToUploadableJpeg(scaled)
    } finally {
        scaled.recycle()
    }
}

private fun isJpeg(bytes: ByteArray): Boolean =
    bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()

private fun decodeBitmap(bytes: ByteArray): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        runCatching {
            val source = ImageDecoder.createSource(bytes)
            return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (
        bounds.outWidth / sampleSize > AvatarUploadConstraints.MAX_DIMENSION_PX ||
        bounds.outHeight / sampleSize > AvatarUploadConstraints.MAX_DIMENSION_PX
    ) {
        sampleSize *= 2
    }

    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
}

private fun scaleToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val longestEdge = maxOf(width, height)
    if (longestEdge <= maxDimension) return bitmap

    val scale = maxDimension.toFloat() / longestEdge.toFloat()
    val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
    val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
}

private fun compressToUploadableJpeg(bitmap: Bitmap): PreparedAvatarImage {
    var quality = 90
    val output = ByteArrayOutputStream()
    while (quality >= 40) {
        output.reset()
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
            throw AvatarImagePrepareException(AvatarImagePrepareException.Reason.DecodeFailed)
        }
        if (output.size() <= AvatarUploadConstraints.UPLOAD_TARGET_BYTES) {
            val bytes = output.toByteArray()
            validatePreparedAvatarBytes(bytes)
            return PreparedAvatarImage(bytes = bytes, contentType = "image/jpeg")
        }
        quality -= 10
    }

    val bytes = output.toByteArray()
    if (bytes.size <= AvatarUploadConstraints.HARD_LIMIT_BYTES) {
        validatePreparedAvatarBytes(bytes)
        return PreparedAvatarImage(bytes = bytes, contentType = "image/jpeg")
    }

    throw AvatarImagePrepareException(
        reason = AvatarImagePrepareException.Reason.TooLarge,
        limitBytes = AvatarUploadConstraints.HARD_LIMIT_BYTES,
        actualBytes = bytes.size,
    )
}
