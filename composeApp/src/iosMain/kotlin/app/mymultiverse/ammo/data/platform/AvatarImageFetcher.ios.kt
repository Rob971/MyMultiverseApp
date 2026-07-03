package app.mymultiverse.ammo.data.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.jetbrains.skia.Image

internal actual fun createAvatarHttpClient(): HttpClient = HttpClient(Darwin)

internal actual suspend fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? =
    Image.makeFromEncoded(bytes)?.toComposeImageBitmap()
