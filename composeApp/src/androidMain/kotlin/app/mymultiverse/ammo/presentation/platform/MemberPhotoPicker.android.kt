package app.mymultiverse.ammo.presentation.platform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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
        // Normalise to a downscaled JPEG so the member-avatars bucket accepts the
        // payload (it rejects HEIC/HEIF and anything over 5 MiB). Fall back to the
        // raw bytes only if the image could not be decoded.
        val normalized = AvatarImageNormalizer.normalize(resolver, uri)
        if (normalized != null) {
            onPhotoPicked(normalized.bytes, normalized.contentType)
            return@rememberLauncherForActivityResult
        }
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@rememberLauncherForActivityResult
        val contentType = resolver.getType(uri) ?: "image/jpeg"
        onPhotoPicked(bytes, contentType)
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
