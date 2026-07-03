package app.mymultiverse.ammo.data.platform

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

internal actual fun createAvatarHttpClient(): HttpClient = HttpClient(Android)

internal actual suspend fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? =
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
