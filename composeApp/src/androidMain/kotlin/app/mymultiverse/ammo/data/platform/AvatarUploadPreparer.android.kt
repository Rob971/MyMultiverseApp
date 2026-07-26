package app.mymultiverse.ammo.data.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.mymultiverse.ammo.domain.sharing.AvatarUploadPreparation
import app.mymultiverse.ammo.domain.sharing.PreparedAvatarUpload
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

actual object AvatarUploadPreparer {
    actual fun prepare(imageBytes: ByteArray, contentType: String): PreparedAvatarUpload {
        val normalized = AvatarUploadPreparation.normalizeContentType(contentType)
        if (
            AvatarUploadPreparation.isPassthroughMime(contentType) &&
            !AvatarUploadPreparation.exceedsSizeLimit(imageBytes.size)
        ) {
            return PreparedAvatarUpload(imageBytes, normalized)
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            // Undecodable (rare) — pass through with a bucket-safe MIME and let Storage decide.
            return PreparedAvatarUpload(imageBytes, normalized)
        }

        var sampleSize = 1
        val longest = max(bounds.outWidth, bounds.outHeight)
        while (longest / sampleSize > AvatarUploadPreparation.MAX_EDGE_PX * 2) {
            sampleSize *= 2
        }
        val decoded = BitmapFactory.decodeByteArray(
            imageBytes,
            0,
            imageBytes.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize },
        ) ?: return PreparedAvatarUpload(imageBytes, normalized)

        val scaled = scaleToMaxEdge(decoded, AvatarUploadPreparation.MAX_EDGE_PX)
        if (scaled !== decoded) {
            decoded.recycle()
        }

        var quality = 85
        var jpegBytes: ByteArray
        do {
            val stream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            jpegBytes = stream.toByteArray()
            quality -= 10
        } while (
            AvatarUploadPreparation.exceedsSizeLimit(jpegBytes.size) &&
            quality >= 40
        )
        scaled.recycle()

        return PreparedAvatarUpload(jpegBytes, "image/jpeg")
    }

    private fun scaleToMaxEdge(source: Bitmap, maxEdge: Int): Bitmap {
        val longest = max(source.width, source.height)
        if (longest <= maxEdge) return source
        val scale = maxEdge.toFloat() / longest.toFloat()
        val width = (source.width * scale).roundToInt().coerceAtLeast(1)
        val height = (source.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, width, height, true)
    }
}
