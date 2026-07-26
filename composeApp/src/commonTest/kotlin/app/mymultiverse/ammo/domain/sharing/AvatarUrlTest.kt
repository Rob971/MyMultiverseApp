package app.mymultiverse.ammo.domain.sharing

import kotlin.test.Test
import kotlin.test.assertEquals

class AvatarUrlTest {

    @Test
    fun versionedAvatarUrl_appendsVersionQueryParam() {
        val versioned = versionedAvatarUrl("https://cdn.example.com/avatar.jpg", versionMs = 42L)

        assertEquals("https://cdn.example.com/avatar.jpg?v=42", versioned)
    }

    @Test
    fun versionedAvatarUrl_replacesExistingQueryParam() {
        val versioned = versionedAvatarUrl(
            publicUrl = "https://cdn.example.com/avatar.jpg?v=1",
            versionMs = 99L,
        )

        assertEquals("https://cdn.example.com/avatar.jpg?v=99", versioned)
    }

    @Test
    fun avatarExtensionForContentType_mapsCommonMimeTypes() {
        assertEquals("png", avatarExtensionForContentType("image/png"))
        assertEquals("webp", avatarExtensionForContentType("image/webp"))
        assertEquals("jpg", avatarExtensionForContentType("image/jpeg"))
        assertEquals("jpg", avatarExtensionForContentType("image/jpg"))
        assertEquals("jpg", avatarExtensionForContentType("image/heic"))
        assertEquals(
            "jpg",
            avatarExtensionForContentType(AvatarUploadPreparation.normalizeContentType("image/heic")),
        )
    }
}
