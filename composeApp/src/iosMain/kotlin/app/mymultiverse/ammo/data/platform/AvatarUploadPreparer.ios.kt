package app.mymultiverse.ammo.data.platform

import app.mymultiverse.ammo.domain.sharing.AvatarUploadPreparation
import app.mymultiverse.ammo.domain.sharing.PreparedAvatarUpload

/**
 * iOS photo picker is not wired yet; normalize MIME only so future uploads stay bucket-safe.
 */
actual object AvatarUploadPreparer {
    actual fun prepare(imageBytes: ByteArray, contentType: String): PreparedAvatarUpload =
        PreparedAvatarUpload(
            bytes = imageBytes,
            contentType = AvatarUploadPreparation.normalizeContentType(contentType),
        )
}
