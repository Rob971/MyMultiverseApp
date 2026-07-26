package app.mymultiverse.ammo.presentation.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

private const val MAX_AVATAR_DIMENSION_PX = 1080
private const val JPEG_QUALITY = 85
private const val MAX_AVATAR_BYTES = 4 * 1024 * 1024 // 4 MiB safety margin under the 5 MiB bucket limit

@Composable
actual fun rememberMemberPhotoPickerLauncher(
    onPhotoPicked: (ByteArray, String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val resolver = context.contentResolver
        val rawBytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@rememberLauncherForActivityResult

        // Compress to JPEG: normalises HEIC/HEIF/BMP/GIF and any other format
        // unsupported by the member-avatars bucket (allowed: image/jpeg, image/png, image/webp).
        // Resizing to MAX_AVATAR_DIMENSION_PX also keeps uploads well within the 5 MiB limit.
        val compressed = compressToJpeg(rawBytes)
        if (compressed == null || compressed.isEmpty()) return@rememberLauncherForActivityResult

        onPhotoPicked(compressed, "image/jpeg")
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

/**
 * Decodes [rawBytes] into a [Bitmap], downsamples it so neither dimension exceeds
 * [MAX_AVATAR_DIMENSION_PX], then re-encodes as JPEG.
 *
 * Returns null if the bytes cannot be decoded as an image.
 */
private fun compressToJpeg(rawBytes: ByteArray): ByteArray? {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, opts)
    if (opts.outWidth <= 0 || opts.outHeight <= 0) return null

    val sampleSize = computeSampleSize(opts.outWidth, opts.outHeight)
    val decodeOpts = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts)
        ?: return null

    return try {
        val scaled = scaleBitmapIfNeeded(bitmap)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        if (scaled !== bitmap) scaled.recycle()
        out.toByteArray().also { bitmap.recycle() }
    } catch (_: Exception) {
        bitmap.recycle()
        null
    }
}

private fun computeSampleSize(width: Int, height: Int): Int {
    var size = 1
    while ((width / (size * 2)) >= MAX_AVATAR_DIMENSION_PX ||
        (height / (size * 2)) >= MAX_AVATAR_DIMENSION_PX
    ) {
        size *= 2
    }
    return size
}

private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    if (w <= MAX_AVATAR_DIMENSION_PX && h <= MAX_AVATAR_DIMENSION_PX) return bitmap

    val scale = MAX_AVATAR_DIMENSION_PX.toFloat() / maxOf(w, h)
    val newW = (w * scale).toInt().coerceAtLeast(1)
    val newH = (h * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
}
