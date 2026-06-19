package app.mymultiverse.kmp.data.invite

object InviteRedirectUrls {
    const val SCHEME = "app.mymultiverse.kmp"
    const val HOST = "invite"
    const val TOKEN_QUERY = "token"

    fun build(token: String): String =
        "$SCHEME://$HOST?$TOKEN_QUERY=${encodeQueryComponent(token.trim())}"

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
            ?.let(::decodeQueryComponent)
            ?.takeIf { it.isNotBlank() }
    }

    fun isInviteRedirect(url: String): Boolean =
        url.startsWith("$SCHEME://$HOST")

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
