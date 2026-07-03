package app.mymultiverse.ammo.data.platform

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes

internal object AvatarImageFetcher {
    private val client: HttpClient by lazy { createAvatarHttpClient() }

    suspend fun load(url: String): ImageBitmap? = runCatching {
        val bytes = client.get(url).bodyAsBytes()
        decodeImageBitmap(bytes)
    }.getOrNull()
}

internal expect fun createAvatarHttpClient(): HttpClient

internal expect suspend fun decodeImageBitmap(bytes: ByteArray): ImageBitmap?
