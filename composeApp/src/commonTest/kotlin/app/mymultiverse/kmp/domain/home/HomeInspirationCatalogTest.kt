package app.mymultiverse.kmp.domain.home

import app.mymultiverse.kmp.domain.manager.SupportedAppLanguages
import kotlin.test.Test
import kotlin.test.assertTrue

class HomeInspirationCatalogTest {

    @Test
    fun pick_returnsLineFromRequestedLocalePool() {
        val line = HomeInspirationCatalog.pick("it")
        assertTrue(line in HomeInspirationCatalog.linesFor("it"))
    }

    @Test
    fun pick_fallsBackToDefaultLocaleForUnknownCode() {
        val line = HomeInspirationCatalog.pick("xx")
        assertTrue(line in HomeInspirationCatalog.linesFor(SupportedAppLanguages.DEFAULT_CODE))
    }

    @Test
    fun linesFor_everySupportedLocaleHasInspirationPool() {
        listOf("en", "es", "fr", "de", "it", "nap", "ar-rSA").forEach { locale ->
            assertTrue(HomeInspirationCatalog.linesFor(locale).isNotEmpty(), locale)
        }
    }
}
