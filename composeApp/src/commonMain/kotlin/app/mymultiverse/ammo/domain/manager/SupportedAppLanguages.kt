package app.mymultiverse.ammo.domain.manager

object SupportedAppLanguages {
    const val SETTINGS_KEY = "app_language"
    const val DEFAULT_CODE = "nap"

    val options: List<Pair<String, String>> = listOf(
        "ar-rSA" to "العربية",
        "de" to "Deutsch",
        "en" to "English",
        "es" to "Español",
        "fr" to "Français",
        "it" to "Italiano",
        "nap" to "Napulitano",
    )

    val codes: Set<String> = options.map { it.first }.toSet()

    fun normalize(code: String): String =
        if (code in codes) code else DEFAULT_CODE

    fun labelFor(code: String): String =
        options.firstOrNull { it.first == normalize(code) }?.second ?: normalize(code)

    private val flagEmojis: Map<String, String> = mapOf(
        "ar-rSA" to "\uD83C\uDDF8\uD83C\uDDE6", // Saudi Arabia
        "de" to "\uD83C\uDDE9\uD83C\uDDEA", // Germany
        "en" to "\uD83C\uDDEC\uD83C\uDDE7", // United Kingdom
        "es" to "\uD83C\uDDEA\uD83C\uDDF8", // Spain
        "fr" to "\uD83C\uDDEB\uD83C\uDDF7", // France
        "it" to "\uD83C\uDDEE\uD83C\uDDF9", // Italy
    )

    fun usesNapoliFootballFlag(code: String): Boolean =
        normalize(code) == DEFAULT_CODE

    fun flagEmojiFor(code: String): String {
        val normalized = normalize(code)
        require(!usesNapoliFootballFlag(normalized)) {
            "Napulitano uses the Napoli football flag composable"
        }
        return flagEmojis.getValue(normalized)
    }
}
