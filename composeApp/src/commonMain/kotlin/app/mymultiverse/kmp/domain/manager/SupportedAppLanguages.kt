package app.mymultiverse.kmp.domain.manager

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
}
