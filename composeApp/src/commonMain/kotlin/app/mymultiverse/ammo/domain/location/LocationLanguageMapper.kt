package app.mymultiverse.ammo.domain.location

import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages

/**
 * Pure domain mapper: converts a [DeviceRegion] to the most appropriate app language code
 * from [SupportedAppLanguages].
 *
 * Rules (in priority order):
 * 1. Italy + Campania region (or province of Napoli) → "nap" (Neapolitan).
 * 2. Italy (other regions, or admin area unknown) → "it".
 * 3. Known country → mapped official language.
 * 4. Unknown country or null region → "en" (international fallback).
 *
 * This is intentionally an object with no I/O so it can be tested deterministically.
 */
object LocationLanguageMapper {

    fun mapToLanguageCode(region: DeviceRegion?): String {
        if (region == null || region.countryCode.isBlank()) return INTERNATIONAL_FALLBACK

        return when (region.countryCode.uppercase()) {
            "IT" -> mapItalyRegion(region.adminArea)
            else -> COUNTRY_TO_LANGUAGE[region.countryCode.uppercase()] ?: INTERNATIONAL_FALLBACK
        }
    }

    private fun mapItalyRegion(adminArea: String?): String {
        if (adminArea != null && isInCampania(adminArea)) return "nap"
        return "it"
    }

    /**
     * Returns true if [adminArea] refers to Campania or the Neapolitan metro area.
     * Comparison is case-insensitive and handles both Italian and English names.
     */
    internal fun isInCampania(adminArea: String): Boolean {
        val normalized = adminArea.trim().lowercase()
        return normalized == "campania" ||
            normalized == "naples" ||
            normalized == "napoli" ||
            normalized.startsWith("napol") ||
            normalized.startsWith("naple")
    }

    private const val INTERNATIONAL_FALLBACK = "en"

    /**
     * ISO 3166-1 alpha-2 country → app language code mapping.
     *
     * Only countries with a clear single-language match to a [SupportedAppLanguages] code
     * are listed. Multilingual countries or those without a supported language fall through
     * to the international fallback.
     */
    private val COUNTRY_TO_LANGUAGE: Map<String, String> = mapOf(
        // French
        "FR" to "fr",
        "MC" to "fr", // Monaco
        "LU" to "fr", // Luxembourg (French is one official language)

        // German
        "DE" to "de",
        "AT" to "de", // Austria
        "LI" to "de", // Liechtenstein

        // Spanish
        "ES" to "es",
        "MX" to "es",
        "AR" to "es", // Argentina
        "CO" to "es",
        "PE" to "es",
        "VE" to "es",
        "CL" to "es",
        "EC" to "es",
        "GT" to "es",
        "CU" to "es",
        "BO" to "es",
        "DO" to "es",
        "HN" to "es",
        "PY" to "es",
        "SV" to "es",
        "NI" to "es",
        "CR" to "es",
        "PA" to "es",
        "UY" to "es",
        "GQ" to "es", // Equatorial Guinea

        // Arabic (Saudi / Gulf variant supported)
        "SA" to "ar-rSA",
        "AE" to "ar-rSA",
        "KW" to "ar-rSA",
        "QA" to "ar-rSA",
        "BH" to "ar-rSA",
        "OM" to "ar-rSA",
        "JO" to "ar-rSA",
        "IQ" to "ar-rSA",
        "SY" to "ar-rSA",
        "LB" to "ar-rSA",
        "EG" to "ar-rSA",
        "MA" to "ar-rSA",
        "DZ" to "ar-rSA",
        "TN" to "ar-rSA",
        "LY" to "ar-rSA",
        "SD" to "ar-rSA",
        "YE" to "ar-rSA",

        // English
        "GB" to "en",
        "US" to "en",
        "AU" to "en",
        "NZ" to "en",
        "IE" to "en",
        "ZA" to "en",
        "SG" to "en",
        "IN" to "en",
        "PH" to "en",
        "NG" to "en",
        "GH" to "en",
        "KE" to "en",
        "UG" to "en",
        "TZ" to "en",
        "ZW" to "en",
        "JM" to "en",
        "TT" to "en",
        "BB" to "en",
        "MT" to "en",
    )
}
