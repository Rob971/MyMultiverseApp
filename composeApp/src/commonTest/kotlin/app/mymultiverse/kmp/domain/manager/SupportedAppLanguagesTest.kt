package app.mymultiverse.kmp.domain.manager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SupportedAppLanguagesTest {

    @Test
    fun normalize_returnsKnownCodesUnchanged() {
        SupportedAppLanguages.codes.forEach { code ->
            assertEquals(code, SupportedAppLanguages.normalize(code))
        }
    }

    @Test
    fun normalize_unknownCodeFallsBackToDefault() {
        assertEquals(SupportedAppLanguages.DEFAULT_CODE, SupportedAppLanguages.normalize("xx"))
        assertEquals(SupportedAppLanguages.DEFAULT_CODE, SupportedAppLanguages.normalize(""))
    }

    @Test
    fun options_includeAllSupportedLocales() {
        assertEquals(SupportedAppLanguages.codes.size, SupportedAppLanguages.options.size)
        assertTrue(SupportedAppLanguages.options.all { (code, label) -> code.isNotBlank() && label.isNotBlank() })
    }

    @Test
    fun labelFor_returnsDisplayNameForKnownCode() {
        assertEquals("Napulitano", SupportedAppLanguages.labelFor("nap"))
        assertEquals("English", SupportedAppLanguages.labelFor("en"))
    }

    @Test
    fun flagEmojiFor_returnsFlagForEmojiLocales() {
        SupportedAppLanguages.codes
            .filterNot { SupportedAppLanguages.usesNapoliFootballFlag(it) }
            .forEach { code ->
                assertTrue(
                    SupportedAppLanguages.flagEmojiFor(code).isNotBlank(),
                    "Expected flag emoji for $code",
                )
            }
    }

    @Test
    fun napulitanoUsesNapoliFootballFlag() {
        assertTrue(SupportedAppLanguages.usesNapoliFootballFlag("nap"))
        assertTrue(SupportedAppLanguages.usesNapoliFootballFlag("xx"))
        assertFalse(SupportedAppLanguages.usesNapoliFootballFlag("it"))
    }

    @Test
    fun flagEmojiFor_unknownCodeFallsBackToNapoliFootballFlag() {
        assertTrue(SupportedAppLanguages.usesNapoliFootballFlag("xx"))
    }
}
