package app.mymultiverse.ammo.domain.sharing

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Shared specification for preparing picked avatar images before upload.
 *
 * The `member-avatars` Storage bucket only accepts a small set of MIME types
 * (`image/jpeg`, `image/png`, `image/webp`) under a 5 MiB size limit. Modern
 * phone galleries hand back HEIC/HEIF photos, or JPEGs well above that limit,
 * which Storage rejects with a generic upload error. Platform pickers must
 * normalise the picked bytes to a downscaled JPEG using these values so the
 * bucket always receives an accepted payload.
 */
object AvatarImageSpec {
    /** Longest edge (px) of the uploaded avatar; keeps files small and square-crop friendly. */
    const val MAX_DIMENSION: Int = 1024

    /** JPEG quality used when re-encoding; 85 balances clarity and payload size. */
    const val JPEG_QUALITY: Int = 85

    /** Content type every platform normaliser emits — always an accepted bucket MIME type. */
    const val NORMALIZED_CONTENT_TYPE: String = "image/jpeg"
}

/** Final avatar dimensions after downscaling to [AvatarImageSpec.MAX_DIMENSION]. */
data class AvatarDimensions(val width: Int, val height: Int)

/**
 * Computes a power-of-two `inSampleSize` for decoding a source bitmap without
 * loading the full-resolution image into memory. Matches the Android
 * `BitmapFactory` guidance: keep halving while both halved edges still exceed
 * [maxDimension].
 */
fun avatarSampleSize(
    sourceWidth: Int,
    sourceHeight: Int,
    maxDimension: Int = AvatarImageSpec.MAX_DIMENSION,
): Int {
    if (sourceWidth <= 0 || sourceHeight <= 0 || maxDimension <= 0) return 1
    var sampleSize = 1
    val halfWidth = sourceWidth / 2
    val halfHeight = sourceHeight / 2
    while ((halfWidth / sampleSize) >= maxDimension && (halfHeight / sampleSize) >= maxDimension) {
        sampleSize *= 2
    }
    return sampleSize
}

/**
 * Scales [width] x [height] down so the longest edge is at most [maxDimension],
 * preserving aspect ratio. Images already within the bound are returned unchanged.
 */
fun avatarScaledDimensions(
    width: Int,
    height: Int,
    maxDimension: Int = AvatarImageSpec.MAX_DIMENSION,
): AvatarDimensions {
    if (width <= 0 || height <= 0 || maxDimension <= 0) return AvatarDimensions(width, height)
    val longestEdge = max(width, height)
    if (longestEdge <= maxDimension) return AvatarDimensions(width, height)
    val scale = maxDimension.toDouble() / longestEdge
    val scaledWidth = max(1, (width * scale).roundToInt())
    val scaledHeight = max(1, (height * scale).roundToInt())
    return AvatarDimensions(scaledWidth, scaledHeight)
}
