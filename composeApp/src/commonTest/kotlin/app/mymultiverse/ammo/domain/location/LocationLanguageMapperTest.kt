package app.mymultiverse.ammo.domain.location

import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocationLanguageMapperTest {

    // ── mapToLanguageCode ───────────────────────────────────────────────

    @Test
    fun nullRegion_returnsEnglish() {
        assertEquals("en", LocationLanguageMapper.mapToLanguageCode(null))
    }

    @Test
    fun blankCountryCode_returnsEnglish() {
        assertEquals("en", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("")))
        assertEquals("en", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("  ")))
    }

    @Test
    fun unknownCountryCode_returnsEnglish() {
        assertEquals("en", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("XX")))
        assertEquals("en", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("ZZ")))
    }

    // Italy ──────────────────────────────────────────────────────────────

    @Test
    fun italy_campania_returnsNapolitan() {
        assertEquals("nap", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "Campania")))
    }

    @Test
    fun italy_campania_caseInsensitive() {
        assertEquals("nap", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "CAMPANIA")))
        assertEquals("nap", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "campania")))
    }

    @Test
    fun italy_napoli_returnsNapolitan() {
        assertEquals("nap", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "Napoli")))
        assertEquals("nap", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "Naples")))
        assertEquals("nap", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "napoli")))
        assertEquals("nap", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "naples")))
    }

    @Test
    fun italy_otherRegion_returnsItalian() {
        assertEquals("it", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "Lombardia")))
        assertEquals("it", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "Lazio")))
        assertEquals("it", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "Sicilia")))
        assertEquals("it", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", "Toscana")))
    }

    @Test
    fun italy_noAdminArea_returnsItalian() {
        assertEquals("it", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT", null)))
        assertEquals("it", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("IT")))
    }

    @Test
    fun italyCountryCode_lowercaseStillMaps() {
        // Country code normalisation: we uppercase internally
        assertEquals("nap", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("it", "Campania")))
        assertEquals("it", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("it", null)))
    }

    // French-speaking ────────────────────────────────────────────────────

    @Test
    fun france_returnsFrench() {
        assertEquals("fr", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("FR")))
    }

    @Test
    fun monaco_returnsFrench() {
        assertEquals("fr", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("MC")))
    }

    // German-speaking ────────────────────────────────────────────────────

    @Test
    fun germany_returnsGerman() {
        assertEquals("de", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("DE")))
    }

    @Test
    fun austria_returnsGerman() {
        assertEquals("de", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("AT")))
    }

    // Spanish-speaking ───────────────────────────────────────────────────

    @Test
    fun spain_returnsSpanish() {
        assertEquals("es", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("ES")))
    }

    @Test
    fun mexico_returnsSpanish() {
        assertEquals("es", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("MX")))
    }

    @Test
    fun argentina_returnsSpanish() {
        assertEquals("es", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("AR")))
    }

    // Arabic ─────────────────────────────────────────────────────────────

    @Test
    fun saudiArabia_returnsArabic() {
        assertEquals("ar-rSA", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("SA")))
    }

    @Test
    fun uae_returnsArabic() {
        assertEquals("ar-rSA", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("AE")))
    }

    @Test
    fun egypt_returnsArabic() {
        assertEquals("ar-rSA", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("EG")))
    }

    // English ────────────────────────────────────────────────────────────

    @Test
    fun unitedKingdom_returnsEnglish() {
        assertEquals("en", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("GB")))
    }

    @Test
    fun unitedStates_returnsEnglish() {
        assertEquals("en", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("US")))
    }

    @Test
    fun australia_returnsEnglish() {
        assertEquals("en", LocationLanguageMapper.mapToLanguageCode(DeviceRegion("AU")))
    }

    // All codes are valid ─────────────────────────────────────────────────

    @Test
    fun allMappedCodes_areValidSupportedLanguages() {
        val testRegions = listOf(
            DeviceRegion("IT", "Campania"),
            DeviceRegion("IT"),
            DeviceRegion("FR"),
            DeviceRegion("DE"),
            DeviceRegion("ES"),
            DeviceRegion("SA"),
            DeviceRegion("GB"),
            DeviceRegion("US"),
            DeviceRegion("XX"),
        )
        testRegions.forEach { region ->
            val code = LocationLanguageMapper.mapToLanguageCode(region)
            assertTrue(
                code in SupportedAppLanguages.codes,
                "Expected code '$code' for $region to be a valid SupportedAppLanguages code",
            )
        }
    }

    // ── isInCampania ─────────────────────────────────────────────────────

    @Test
    fun isInCampania_matchesCampania() {
        assertTrue(LocationLanguageMapper.isInCampania("Campania"))
        assertTrue(LocationLanguageMapper.isInCampania("campania"))
        assertTrue(LocationLanguageMapper.isInCampania("CAMPANIA"))
        assertTrue(LocationLanguageMapper.isInCampania("  Campania  "))
    }

    @Test
    fun isInCampania_matchesNapoli() {
        assertTrue(LocationLanguageMapper.isInCampania("Napoli"))
        assertTrue(LocationLanguageMapper.isInCampania("napoli"))
        assertTrue(LocationLanguageMapper.isInCampania("NAPOLI"))
    }

    @Test
    fun isInCampania_matchesNaples() {
        assertTrue(LocationLanguageMapper.isInCampania("Naples"))
        assertTrue(LocationLanguageMapper.isInCampania("naples"))
        assertTrue(LocationLanguageMapper.isInCampania("NAPLES"))
    }

    @Test
    fun isInCampania_doesNotMatchOtherRegions() {
        assertFalse(LocationLanguageMapper.isInCampania("Lombardia"))
        assertFalse(LocationLanguageMapper.isInCampania("Roma"))
        assertFalse(LocationLanguageMapper.isInCampania("Toscana"))
        assertFalse(LocationLanguageMapper.isInCampania("Sicilia"))
        assertFalse(LocationLanguageMapper.isInCampania("Lazio"))
        assertFalse(LocationLanguageMapper.isInCampania("Puglia"))
        assertFalse(LocationLanguageMapper.isInCampania("Veneto"))
    }
}
