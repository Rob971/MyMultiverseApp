package app.mymultiverse.ammo.presentation.platform

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

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
        val image = resolver.loadAvatarImageForUpload(uri) ?: return@rememberLauncherForActivityResult
        onPhotoPicked(image.bytes, image.contentType)
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

private data class PickedAvatarImage(
    val bytes: ByteArray,
    val contentType: String,
)

private const val AvatarUploadContentType = "image/jpeg"
private const val AvatarUploadMaxDimensionPx = 1024
private const val AvatarUploadJpegQuality = 86

private fun ContentResolver.loadAvatarImageForUpload(uri: Uri): PickedAvatarImage? = runCatching {
    val decoded = decodeAvatarBitmap(uri)
    if (decoded != null) {
        return@runCatching PickedAvatarImage(
            bytes = decoded.toJpegBytes(),
            contentType = AvatarUploadContentType,
        )
    }

    val fallbackContentType = getType(uri)?.takeIf { it.isAllowedAvatarContentType() } ?: return@runCatching null
    val fallbackBytes = openInputStream(uri)?.use { it.readBytes() } ?: return@runCatching null
    PickedAvatarImage(bytes = fallbackBytes, contentType = fallbackContentType)
}.getOrNull()

private fun ContentResolver.decodeAvatarBitmap(uri: Uri): Bitmap? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(this, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val largestSide = max(info.size.width, info.size.height)
            if (largestSide > AvatarUploadMaxDimensionPx) {
                val scale = AvatarUploadMaxDimensionPx.toFloat() / largestSide.toFloat()
                decoder.setTargetSize(
                    (info.size.width * scale).roundToInt().coerceAtLeast(1),
                    (info.size.height * scale).roundToInt().coerceAtLeast(1),
                )
            }
        }
    } else {
        decodeAvatarBitmapLegacy(uri)
    }
}.getOrNull()

private fun ContentResolver.decodeAvatarBitmapLegacy(uri: Uri): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val largestSide = max(bounds.outWidth, bounds.outHeight)
    val sampleSize = if (largestSide <= AvatarUploadMaxDimensionPx) {
        1
    } else {
        var sample = 1
        while (largestSide / (sample * 2) >= AvatarUploadMaxDimensionPx) {
            sample *= 2
        }
        sample
    }

    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
}

private fun Bitmap.toJpegBytes(): ByteArray =
    ByteArrayOutputStream().use { output ->
        compress(Bitmap.CompressFormat.JPEG, AvatarUploadJpegQuality, output)
        output.toByteArray()
    }

private fun String.isAllowedAvatarContentType(): Boolean =
    equals("image/jpeg", ignoreCase = true) ||
        equals("image/jpg", ignoreCase = true) ||
        equals("image/png", ignoreCase = true) ||
        equals("image/webp", ignoreCase = true)
