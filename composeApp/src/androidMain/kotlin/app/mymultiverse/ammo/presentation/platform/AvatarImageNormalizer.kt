package app.mymultiverse.ammo.presentation.platform

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import app.mymultiverse.ammo.domain.sharing.AvatarImageSpec
import app.mymultiverse.ammo.domain.sharing.avatarSampleSize
import app.mymultiverse.ammo.domain.sharing.avatarScaledDimensions
import java.io.ByteArrayOutputStream

/**
 * Decodes a picked image, corrects its EXIF orientation, downscales it, and
 * re-encodes it as JPEG so the `member-avatars` bucket always receives an
 * accepted MIME type well under the 5 MiB limit — regardless of the source
 * format (HEIC/HEIF, large JPEG/PNG, etc.).
 */
internal object AvatarImageNormalizer {

    /**
     * @return the normalised JPEG bytes with [AvatarImageSpec.NORMALIZED_CONTENT_TYPE],
     * or `null` when the image could not be decoded (caller may fall back to raw bytes).
     */
    fun normalize(resolver: ContentResolver, uri: Uri): NormalizedAvatar? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = avatarSampleSize(bounds.outWidth, bounds.outHeight, AvatarImageSpec.MAX_DIMENSION)
        }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        } ?: return null

        val oriented = applyExifOrientation(resolver, uri, decoded)
        val scaled = scaleToMax(oriented)

        val bytes = ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, AvatarImageSpec.JPEG_QUALITY, out)
            out.toByteArray()
        }

        if (scaled !== oriented) scaled.recycle()
        if (oriented !== decoded) oriented.recycle()
        decoded.recycle()

        return NormalizedAvatar(bytes = bytes, contentType = AvatarImageSpec.NORMALIZED_CONTENT_TYPE)
    }

    private fun scaleToMax(bitmap: Bitmap): Bitmap {
        val target = avatarScaledDimensions(bitmap.width, bitmap.height, AvatarImageSpec.MAX_DIMENSION)
        if (target.width == bitmap.width && target.height == bitmap.height) return bitmap
        return Bitmap.createScaledBitmap(bitmap, target.width, target.height, true)
    }

    private fun applyExifOrientation(resolver: ContentResolver, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            resolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return runCatching {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }.getOrDefault(bitmap)
    }
}

internal class NormalizedAvatar(
    val bytes: ByteArray,
    val contentType: String,
)
