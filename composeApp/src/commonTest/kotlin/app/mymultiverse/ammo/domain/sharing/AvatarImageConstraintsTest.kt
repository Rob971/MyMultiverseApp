package app.mymultiverse.ammo.domain.sharing

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AvatarImageConstraintsTest {

    @Test
    fun isSupportedAvatarContentType_acceptsBucketAllowedTypes() {
        assertTrue(isSupportedAvatarContentType("image/jpeg"))
        assertTrue(isSupportedAvatarContentType("image/png"))
        assertTrue(isSupportedAvatarContentType("image/webp"))
        assertTrue(isSupportedAvatarContentType("IMAGE/JPEG"))
        assertTrue(isSupportedAvatarContentType("image/jpeg; charset=utf-8"))
    }

    @Test
    fun isSupportedAvatarContentType_rejectsTypesTheBucketRejects() {
        assertFalse(isSupportedAvatarContentType("image/heic"))
        assertFalse(isSupportedAvatarContentType("image/heif"))
        assertFalse(isSupportedAvatarContentType("image/gif"))
        // Non-standard alias not present in the bucket allowed_mime_types list.
        assertFalse(isSupportedAvatarContentType("image/jpg"))
        assertFalse(isSupportedAvatarContentType(""))
    }

    @Test
    fun avatarUploadRequiresTranscode_whenContentTypeUnsupported() {
        assertTrue(avatarUploadRequiresTranscode(byteCount = 1_024, contentType = "image/heic"))
    }

    @Test
    fun avatarUploadRequiresTranscode_whenBytesExceedBucketLimit() {
        assertTrue(
            avatarUploadRequiresTranscode(
                byteCount = AVATAR_MAX_UPLOAD_BYTES + 1,
                contentType = "image/jpeg",
            ),
        )
    }

    @Test
    fun avatarUploadRequiresTranscode_falseForSupportedTypeWithinLimit() {
        assertFalse(
            avatarUploadRequiresTranscode(
                byteCount = AVATAR_MAX_UPLOAD_BYTES,
                contentType = "image/jpeg",
            ),
        )
        assertFalse(avatarUploadRequiresTranscode(byteCount = 1, contentType = "image/webp"))
    }
}
