package app.mymultiverse.ammo.domain.sharing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AvatarUploadImageTest {

    @Test
    fun normalizeContentType_mapsJpegAliases() {
        assertEquals("image/jpeg", AvatarUploadImage.normalizeContentType("image/jpg"))
        assertEquals("image/jpeg", AvatarUploadImage.normalizeContentType("IMAGE/PJPEG"))
        assertEquals("image/jpeg", AvatarUploadImage.normalizeContentType("image/jpeg; charset=binary"))
        assertEquals("image/png", AvatarUploadImage.normalizeContentType("image/x-png"))
        assertEquals("image/webp", AvatarUploadImage.normalizeContentType("image/x-webp"))
    }

    @Test
    fun isAllowedContentType_acceptsBucketMimesOnly() {
        assertTrue(AvatarUploadImage.isAllowedContentType("image/jpg"))
        assertTrue(AvatarUploadImage.isAllowedContentType("image/png"))
        assertFalse(AvatarUploadImage.isAllowedContentType("image/heic"))
        assertFalse(AvatarUploadImage.isAllowedContentType("application/octet-stream"))
    }

    @Test
    fun storageFailureReason_mapsMimeAndSizeErrors() {
        assertEquals(
            AvatarUploadFailureReason.InvalidMime,
            AvatarUploadImage.storageFailureReason(
                "mime type image/heic is not supported",
            ),
        )
        assertEquals(
            AvatarUploadFailureReason.InvalidMime,
            AvatarUploadImage.storageFailureReason("invalid_mime_type"),
        )
        assertEquals(
            AvatarUploadFailureReason.PayloadTooLarge,
            AvatarUploadImage.storageFailureReason(
                "The object exceeded the maximum allowed size",
            ),
        )
        assertNull(AvatarUploadImage.storageFailureReason("connection reset"))
    }
}
