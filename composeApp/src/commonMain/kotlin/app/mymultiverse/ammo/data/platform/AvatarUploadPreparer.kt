package app.mymultiverse.ammo.data.platform

import app.mymultiverse.ammo.domain.sharing.PreparedAvatarUpload

/**
 * Platform image preparation before Supabase Storage upload.
 * Downscales and re-encodes when the picker MIME/size would be rejected by the bucket.
 */
expect object AvatarUploadPreparer {
    fun prepare(imageBytes: ByteArray, contentType: String): PreparedAvatarUpload
}
