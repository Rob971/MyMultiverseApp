package app.mymultiverse.ammo.domain.sharing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AvatarUploadPreparationTest {

    @Test
    fun normalizeContentType_mapsAliasesAndUnsupportedToBucketSafeValues() {
        assertEquals("image/jpeg", AvatarUploadPreparation.normalizeContentType("image/jpeg"))
        assertEquals("image/jpeg", AvatarUploadPreparation.normalizeContentType("image/jpg"))
        assertEquals("image/jpeg", AvatarUploadPreparation.normalizeContentType("image/pjpeg"))
        assertEquals("image/png", AvatarUploadPreparation.normalizeContentType("image/png"))
        assertEquals("image/webp", AvatarUploadPreparation.normalizeContentType("image/webp"))
        assertEquals("image/jpeg", AvatarUploadPreparation.normalizeContentType("image/heic"))
        assertEquals("image/jpeg", AvatarUploadPreparation.normalizeContentType("image/heif"))
        assertEquals(
            "image/jpeg",
            AvatarUploadPreparation.normalizeContentType("image/jpeg; charset=binary"),
        )
    }

    @Test
    fun isPassthroughMime_acceptsBucketSafeAndJpgAliasOnly() {
        assertTrue(AvatarUploadPreparation.isPassthroughMime("image/jpeg"))
        assertTrue(AvatarUploadPreparation.isPassthroughMime("image/png"))
        assertTrue(AvatarUploadPreparation.isPassthroughMime("image/webp"))
        assertTrue(AvatarUploadPreparation.isPassthroughMime("image/jpg"))
        assertFalse(AvatarUploadPreparation.isPassthroughMime("image/heic"))
        assertFalse(AvatarUploadPreparation.isPassthroughMime("image/heif"))
        assertFalse(AvatarUploadPreparation.isPassthroughMime("application/octet-stream"))
    }

    @Test
    fun exceedsSizeLimit_matchesBucketFiveMibCap() {
        assertFalse(AvatarUploadPreparation.exceedsSizeLimit(AvatarUploadPreparation.MAX_UPLOAD_BYTES))
        assertTrue(AvatarUploadPreparation.exceedsSizeLimit(AvatarUploadPreparation.MAX_UPLOAD_BYTES + 1))
    }
}
