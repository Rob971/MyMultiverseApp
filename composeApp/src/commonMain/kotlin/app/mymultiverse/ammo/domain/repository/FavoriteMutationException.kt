package app.mymultiverse.ammo.domain.repository

/**
 * Why a favorite-dish mutation failed, mapped so the UI can respond without
 * coupling to the transport (PostgREST, network) or exposing internals.
 */
class FavoriteMutationException(
    val kind: Kind,
    cause: Throwable? = null,
) : Exception(kind.name, cause) {
    enum class Kind {
        /** Server rejected the insert: 10 favorites already saved. */
        CAP_EXCEEDED,

        /** Network/transport failure — the device is likely offline. */
        OFFLINE,

        /** Anything else. Treated as a transient write failure. */
        UNKNOWN,
    }
}