package app.mymultiverse.ammo.data.platform

import app.mymultiverse.ammo.domain.sharing.AvatarImagePrepareException
import app.mymultiverse.ammo.domain.sharing.AvatarUploadConstraints
import app.mymultiverse.ammo.domain.sharing.isAvatarContentTypeSupported
import app.mymultiverse.ammo.domain.sharing.normalizeAvatarContentType
import app.mymultiverse.ammo.domain.sharing.validatePreparedAvatarBytes
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
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

    if (rawBytes.size <= AvatarUploadConstraints.UPLOAD_TARGET_BYTES && normalizedType == "image/jpeg") {
        validatePreparedAvatarBytes(rawBytes)
        return PreparedAvatarImage(bytes = rawBytes, contentType = normalizedType)
    }

    val image = Image.makeFromEncoded(rawBytes)
        ?: throw AvatarImagePrepareException(AvatarImagePrepareException.Reason.DecodeFailed)

    val scaled = scaleImage(image)
    return compressToUploadableJpeg(scaled)
}

private fun scaleImage(image: Image): Image {
    val width = image.width
    val height = image.height
    val longestEdge = maxOf(width, height)
    if (longestEdge <= AvatarUploadConstraints.MAX_DIMENSION_PX) return image

    val scale = AvatarUploadConstraints.MAX_DIMENSION_PX.toFloat() / longestEdge.toFloat()
    val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
    val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)

    val surface = Surface.makeRasterN32Premul(targetWidth, targetHeight)
  return try {
        val canvas = surface.canvas
        canvas.drawImageRect(
            image,
            Rect.makeWH(width.toFloat(), height.toFloat()),
            Rect.makeWH(targetWidth.toFloat(), targetHeight.toFloat()),
        )
        surface.makeImageSnapshot()
    } finally {
        surface.close()
    }
}

private fun compressToUploadableJpeg(image: Image): PreparedAvatarImage {
    var quality = 90
    while (quality >= 40) {
        val data = image.encodeToData(EncodedImageFormat.JPEG, quality)
        if (data != null && data.size <= AvatarUploadConstraints.UPLOAD_TARGET_BYTES) {
            validatePreparedAvatarBytes(data.bytes)
            return PreparedAvatarImage(bytes = data.bytes, contentType = "image/jpeg")
        }
        quality -= 10
    }

    val lastAttempt = image.encodeToData(EncodedImageFormat.JPEG, 40)
    if (lastAttempt != null && lastAttempt.size <= AvatarUploadConstraints.HARD_LIMIT_BYTES) {
        validatePreparedAvatarBytes(lastAttempt.bytes)
        return PreparedAvatarImage(bytes = lastAttempt.bytes, contentType = "image/jpeg")
    }

    throw AvatarImagePrepareException(
        reason = AvatarImagePrepareException.Reason.TooLarge,
        limitBytes = AvatarUploadConstraints.HARD_LIMIT_BYTES,
        actualBytes = lastAttempt?.size,
    )
}
