package app.mymultiverse.ammo.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class SharingLocaleStringsTest {

    @Test
    fun allSupportedLocalesDefineEverySharingStringKey() {
        SharingStringKeys.localeDirectories.forEach { localeDir ->
            val stringsFile = LocaleTestFiles.stringsFile(localeDir)
            assertTrue(stringsFile.exists(), "Missing strings file: ${stringsFile.path}")

            val keysInFile = LocaleTestFiles.readStringKeys(stringsFile.readText())
            val missing = SharingStringKeys.all - keysInFile

            if (missing.isNotEmpty()) {
                fail("Locale '$localeDir' is missing sharing keys: ${missing.sorted().joinToString()}")
            }
            assertEquals(SharingStringKeys.all, keysInFile.intersect(SharingStringKeys.all))
        }
    }

    @Test
    fun sharingStringValuesAreNotBlankInDefaultLocale() {
        val contents = LocaleTestFiles.stringsFile("values").readText()

        SharingStringKeys.all.forEach { key ->
            val value = LocaleTestFiles.readStringValue(contents, key)
            assertTrue(value.isNotBlank(), "Default locale value for '$key' must not be blank")
        }
    }
}
