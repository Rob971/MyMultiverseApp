package app.mymultiverse.kmp.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class AuthLocaleStringsTest {

    @Test
    fun allSupportedLocalesDefineEveryAuthStringKey() {
        AuthStringKeys.localeDirectories.forEach { localeDir ->
            val stringsFile = LocaleTestFiles.stringsFile(localeDir)
            assertTrue(stringsFile.exists(), "Missing strings file: ${stringsFile.path}")

            val keysInFile = LocaleTestFiles.readStringKeys(stringsFile.readText())
            val missing = AuthStringKeys.all - keysInFile

            if (missing.isNotEmpty()) {
                fail("Locale '$localeDir' is missing auth keys: ${missing.sorted().joinToString()}")
            }
            assertEquals(AuthStringKeys.all, keysInFile.intersect(AuthStringKeys.all))
        }
    }

    @Test
    fun authStringValuesAreNotBlankInDefaultLocale() {
        val contents = LocaleTestFiles.stringsFile("values").readText()

        AuthStringKeys.all.forEach { key ->
            val value = LocaleTestFiles.readStringValue(contents, key)
            assertTrue(value.isNotBlank(), "Default locale value for '$key' must not be blank")
        }
    }
}
