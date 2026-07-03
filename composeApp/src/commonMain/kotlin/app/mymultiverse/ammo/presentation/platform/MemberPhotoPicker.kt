package app.mymultiverse.ammo.presentation.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberMemberPhotoPickerLauncher(
    onPhotoPicked: (ByteArray, String) -> Unit,
): () -> Unit
