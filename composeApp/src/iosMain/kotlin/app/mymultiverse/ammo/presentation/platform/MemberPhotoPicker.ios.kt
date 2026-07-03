package app.mymultiverse.ammo.presentation.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberMemberPhotoPickerLauncher(
    onPhotoPicked: (ByteArray, String) -> Unit,
): () -> Unit = {}
