package app.mymultiverse.ammo.domain.sharing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AvatarUploadConstraintsTest {

    @Test
    fun normalizeAvatarContentType_mapsGalleryAliasesToBucketMimeTypes() {
        assertEquals("image/jpeg", normalizeAvatarContentType("image/jpg"))
        assertEquals("image/jpeg", normalizeAvatarContentType("image/heic"))
        assertEquals("image/jpeg", normalizeAvatarContentType("image/heif"))
        assertEquals("image/jpeg", normalizeAvatarContentType("image/pjpeg"))
        assertEquals("image/png", normalizeAvatarContentType("image/png"))
        assertEquals("image/webp", normalizeAvatarContentType("image/webp"))
        assertEquals("image/jpeg", normalizeAvatarContentType(""))
    }

    @Test
    fun isAvatarContentTypeSupported_acceptsBucketMimeTypesOnly() {
        assertTrue(isAvatarContentTypeSupported("image/jpeg"))
        assertTrue(isAvatarContentTypeSupported("image/heic"))
        assertTrue(isAvatarContentTypeSupported("image/png"))
        assertTrue(isAvatarContentTypeSupported("image/webp"))
        assertFalse(isAvatarContentTypeSupported("image/gif"))
        assertFalse(isAvatarContentTypeSupported("application/pdf"))
    }

    @Test
    fun avatarMaxFileSizeMegabytes_matchesHardLimit() {
        assertEquals(5, avatarMaxFileSizeMegabytes())
        assertEquals(5 * 1024 * 1024, AvatarUploadConstraints.HARD_LIMIT_BYTES)
    }

    @Test
    fun validatePreparedAvatarBytes_allowsAtHardLimit() {
        validatePreparedAvatarBytes(ByteArray(AvatarUploadConstraints.HARD_LIMIT_BYTES))
    }

    @Test
    fun validatePreparedAvatarBytes_rejectsAboveHardLimit() {
        val error = assertFailsWith<AvatarImagePrepareException> {
            validatePreparedAvatarBytes(ByteArray(AvatarUploadConstraints.HARD_LIMIT_BYTES + 1))
        }
        assertEquals(AvatarImagePrepareException.Reason.TooLarge, error.reason)
        assertEquals(AvatarUploadConstraints.HARD_LIMIT_BYTES, error.limitBytes)
    }
}
