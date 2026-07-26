package app.mymultiverse.ammo.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.domain.sharing.AVATAR_MAX_DIMENSION_PX
import app.mymultiverse.ammo.domain.sharing.AVATAR_MAX_UPLOAD_BYTES
import app.mymultiverse.ammo.presentation.platform.prepareAvatarImageForUpload
import java.io.ByteArrayOutputStream
import kotlin.random.Random
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvatarImageTranscoderInstrumentedTest {

    @Test
    fun supportedSmallJpegPassesThroughUntouched() {
        val original = solidBitmap(width = 640, height = 480).encode(Bitmap.CompressFormat.JPEG)

        val prepared = prepareAvatarImageForUpload(original, "image/jpeg")

        assertArrayEquals(original, prepared.bytes)
        assertEquals("image/jpeg", prepared.contentType)
    }

    @Test
    fun unsupportedContentTypeIsReEncodedAsJpegWithinMaxDimension() {
        // HEIC captures decode fine via BitmapFactory but the bucket rejects the mime type.
        val original = solidBitmap(width = 3000, height = 2000).encode(Bitmap.CompressFormat.JPEG)

        val prepared = prepareAvatarImageForUpload(original, "image/heic")

        assertEquals("image/jpeg", prepared.contentType)
        assertTrue(prepared.bytes.size <= AVATAR_MAX_UPLOAD_BYTES)
        val bounds = decodeBounds(prepared.bytes)
        assertTrue(maxOf(bounds.outWidth, bounds.outHeight) <= AVATAR_MAX_DIMENSION_PX)
    }

    @Test
    fun oversizedPhotoIsDownscaledBelowBucketLimit() {
        // Random noise defeats PNG compression, producing a payload above 5MiB
        // like real multi-megabyte camera photos.
        val original = noiseBitmap(width = 1500, height = 1500).encode(Bitmap.CompressFormat.PNG)
        assertTrue("test input must exceed bucket limit", original.size > AVATAR_MAX_UPLOAD_BYTES)

        val prepared = prepareAvatarImageForUpload(original, "image/png")

        assertEquals("image/jpeg", prepared.contentType)
        assertTrue(prepared.bytes.size <= AVATAR_MAX_UPLOAD_BYTES)
        val bounds = decodeBounds(prepared.bytes)
        assertTrue(maxOf(bounds.outWidth, bounds.outHeight) <= AVATAR_MAX_DIMENSION_PX)
    }

    @Test
    fun undecodableBytesPassThroughForServerSideErrorReporting() {
        val original = ByteArray(AVATAR_MAX_UPLOAD_BYTES + 1) { 0x42 }

        val prepared = prepareAvatarImageForUpload(original, "image/jpeg")

        assertArrayEquals(original, prepared.bytes)
        assertEquals("image/jpeg", prepared.contentType)
    }

    private fun solidBitmap(width: Int, height: Int): Bitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.rgb(200, 120, 80))
        }

    private fun noiseBitmap(width: Int, height: Int): Bitmap {
        val random = Random(seed = 7)
        val pixels = IntArray(width * height) { random.nextInt() or (0xFF shl 24) }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun Bitmap.encode(format: Bitmap.CompressFormat): ByteArray {
        val output = ByteArrayOutputStream()
        compress(format, 100, output)
        recycle()
        return output.toByteArray()
    }

    private fun decodeBounds(bytes: ByteArray): BitmapFactory.Options =
        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, this)
        }
}
