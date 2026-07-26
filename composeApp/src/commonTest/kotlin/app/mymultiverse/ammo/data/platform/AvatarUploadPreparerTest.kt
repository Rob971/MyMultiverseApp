package app.mymultiverse.ammo.data.platform

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/**
 * JVM unit tests can only exercise the passthrough branch — BitmapFactory is not mocked.
 * Decode/transcode is covered by staging probes + manual Firebase QA (HEIC / large photos).
 */
class AvatarUploadPreparerTest {

    @Test
    fun prepare_passthrough_keepsSmallJpegBytesAndNormalizesJpgAlias() {
        val source = ByteArray(64) { index -> index.toByte() }
        val prepared = AvatarUploadPreparer.prepare(source, "image/jpg")

        assertEquals("image/jpeg", prepared.contentType)
        assertContentEquals(source, prepared.bytes)
    }

    @Test
    fun prepare_passthrough_keepsSmallPng() {
        val source = ByteArray(32) { 7 }
        val prepared = AvatarUploadPreparer.prepare(source, "image/png")

        assertEquals("image/png", prepared.contentType)
        assertContentEquals(source, prepared.bytes)
    }
}
