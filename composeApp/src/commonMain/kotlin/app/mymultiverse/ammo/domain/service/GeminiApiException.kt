package app.mymultiverse.ammo.domain.service

/**
 * Thrown when a Gemini API call fails while a user API key is configured.
 *
 * Screen models map [Reason.AUTH_ERROR] to the inline key-setup prompt (invalid or
 * revoked key). Other reasons surface a generic retry message and are recorded as
 * non-fatal events in Crashlytics.
 */
class GeminiApiException(
    val reason: Reason,
    val httpStatus: Int? = null,
    cause: Throwable? = null,
) : Exception(reason.code, cause) {

    enum class Reason(val code: String) {
        NETWORK("gemini_network_error"),
        HTTP_ERROR("gemini_http_error"),
        AUTH_ERROR("gemini_auth_error"),
        PARSE_ERROR("gemini_parse_error"),
        EMPTY_RESPONSE("gemini_empty_response"),
    }

    val isAuthError: Boolean
        get() = reason == Reason.AUTH_ERROR
}
