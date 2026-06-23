package app.mymultiverse.kmp.data.invite

object InviteRedirectUrls {
    const val SCHEME = "app.mymultiverse.kmp"
    const val HOST = "invite"
    const val TOKEN_QUERY = "token"

    /** Primary marketing domain for verified App Links / Universal Links. */
    const val HTTPS_HOST_PRIMARY = "mymultiverse.app"
    const val HTTPS_INVITE_PATH = "/invite"
    const val HTTPS_INVITE_OPEN_PATH_SUFFIX = "/functions/v1/invite-open"

    fun build(token: String): String =
        "$SCHEME://$HOST?$TOKEN_QUERY=${encodeQueryComponent(token.trim())}"

    fun buildHttps(token: String, host: String = HTTPS_HOST_PRIMARY): String =
        "https://$host$HTTPS_INVITE_PATH?$TOKEN_QUERY=${encodeQueryComponent(token.trim())}"

    fun parseToken(url: String): String? {
        if (isInviteRedirect(url)) {
            return parseTokenFromQuery(url)
        }
        return null
    }

    fun isInviteRedirect(url: String): Boolean =
        url.startsWith("$SCHEME://$HOST") || isHttpsInviteRedirect(url)

    private fun isHttpsInviteRedirect(url: String): Boolean {
        if (!url.startsWith("https://", ignoreCase = true)) return false
        val pathStart = url.indexOf('/', startIndex = "https://".length)
        if (pathStart < 0) return false
        val hostEnd = url.indexOfAny(charArrayOf('/', '?', '#'), startIndex = "https://".length)
        val host = if (hostEnd < 0) {
            url.substring("https://".length)
        } else {
            url.substring("https://".length, hostEnd)
        }
        if (!isAllowedHttpsHost(host)) return false
        val path = url.substring(pathStart).substringBefore('?').substringBefore('#')
        return isAllowedHttpsInvitePath(path)
    }

    private fun isAllowedHttpsHost(host: String): Boolean =
        host.equals(HTTPS_HOST_PRIMARY, ignoreCase = true) ||
            host.endsWith(".supabase.co", ignoreCase = true)

    private fun isAllowedHttpsInvitePath(path: String): Boolean =
        path.equals(HTTPS_INVITE_PATH, ignoreCase = true) ||
            path.endsWith(HTTPS_INVITE_OPEN_PATH_SUFFIX, ignoreCase = true)

    private fun parseTokenFromQuery(url: String): String? {
        val queryStart = url.indexOf('?')
        if (queryStart < 0) return null
        return url.substring(queryStart + 1)
            .split('&')
            .asSequence()
            .map { part -> part.split('=', limit = 2) }
            .firstOrNull { parts -> parts.size == 2 && parts[0] == TOKEN_QUERY }
            ?.get(1)
            ?.let(::decodeQueryComponent)
            ?.takeIf { it.isNotBlank() }
    }

    private fun encodeQueryComponent(value: String): String = buildString(value.length) {
        value.forEach { ch ->
            when {
                ch.isLetterOrDigit() || ch == '-' || ch == '_' || ch == '.' || ch == '~' -> append(ch)
                else -> {
                    val code = ch.code
                    when {
                        code < 128 -> append('%').append(code.toString(16).uppercase().padStart(2, '0'))
                        else -> ch.toString().encodeToByteArray().forEach { byte ->
                            append('%').append((byte.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0'))
                        }
                    }
                }
            }
        }
    }

    private fun decodeQueryComponent(value: String): String = buildString {
        var index = 0
        while (index < value.length) {
            when (val ch = value[index]) {
                '%' -> {
                    if (index + 2 >= value.length) {
                        append(ch)
                        index += 1
                    } else {
                        val hex = value.substring(index + 1, index + 3)
                        append(hex.toInt(16).toChar())
                        index += 3
                    }
                }
                '+' -> {
                    append(' ')
                    index += 1
                }
                else -> {
                    append(ch)
                    index += 1
                }
            }
        }
    }
}
