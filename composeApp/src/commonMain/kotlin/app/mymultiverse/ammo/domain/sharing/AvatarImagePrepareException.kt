package app.mymultiverse.ammo.domain.sharing

/**
 * Raised when a picked image cannot be prepared for upload (decode, format, or size).
 * Stable [errorCode] values are matched in UI error mapping.
 */
class AvatarImagePrepareException(
    val reason: Reason,
    val limitBytes: Int? = null,
    val actualBytes: Int? = null,
    message: String = reason.errorCode,
) : IllegalStateException(message) {

    enum class Reason(val errorCode: String) {
        UnsupportedFormat(ERROR_CODE_UNSUPPORTED),
        DecodeFailed(ERROR_CODE_DECODE_FAILED),
        TooLarge(ERROR_CODE_TOO_LARGE),
    }

    companion object {
        const val ERROR_CODE_UNSUPPORTED = "avatar_image_unsupported_format"
        const val ERROR_CODE_DECODE_FAILED = "avatar_image_decode_failed"
        const val ERROR_CODE_TOO_LARGE = "avatar_image_too_large"
    }
}
