package app.mymultiverse.ammo.data.platform

/**
 * Downscales and re-encodes a gallery image so uploads stay within bucket MIME and size limits.
 * Always returns bytes + a content type accepted by the `member-avatars` bucket.
 */
data class PreparedAvatarImage(
    val bytes: ByteArray,
    val contentType: String,
)

/**
 * @throws app.mymultiverse.ammo.domain.sharing.AvatarImagePrepareException when the image
 *   cannot be decoded, is an unsupported format, or remains too large after compression.
 */
expect fun prepareAvatarImageForUpload(
    rawBytes: ByteArray,
    contentType: String,
): PreparedAvatarImage
