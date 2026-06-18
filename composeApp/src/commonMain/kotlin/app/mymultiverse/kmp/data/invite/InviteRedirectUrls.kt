package app.mymultiverse.kmp.data.invite

object InviteRedirectUrls {
    const val SCHEME = "app.mymultiverse.kmp"
    const val HOST = "invite"
    const val TOKEN_QUERY = "token"

    fun build(token: String): String = "$SCHEME://$HOST?$TOKEN_QUERY=$token"

    fun parseToken(url: String): String? {
        if (!isInviteRedirect(url)) return null
        val queryStart = url.indexOf('?')
        if (queryStart < 0) return null
        return url.substring(queryStart + 1)
            .split('&')
            .asSequence()
            .map { part -> part.split('=', limit = 2) }
            .firstOrNull { parts -> parts.size == 2 && parts[0] == TOKEN_QUERY }
            ?.get(1)
            ?.takeIf { it.isNotBlank() }
    }

    fun isInviteRedirect(url: String): Boolean =
        url.startsWith("$SCHEME://$HOST")
}
