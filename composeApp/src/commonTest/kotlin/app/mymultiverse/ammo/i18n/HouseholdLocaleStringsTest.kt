package app.mymultiverse.ammo.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class HouseholdLocaleStringsTest {

    @Test
    fun allSupportedLocalesDefineEveryHouseholdStringKey() {
        HouseholdStringKeys.localeDirectories.forEach { localeDir ->
            val stringsFile = LocaleTestFiles.stringsFile(localeDir)
            assertTrue(stringsFile.exists(), "Missing strings file: ${stringsFile.path}")

            val keysInFile = LocaleTestFiles.readStringKeys(stringsFile.readText())
            val missing = HouseholdStringKeys.all - keysInFile
            if (missing.isNotEmpty()) {
                fail("Locale '$localeDir' is missing household keys: ${missing.sorted().joinToString()}")
            }
            assertEquals(HouseholdStringKeys.all, keysInFile.intersect(HouseholdStringKeys.all))
        }
    }

    @Test
    fun householdStringValuesAreNotBlankInDefaultLocale() {
        val contents = LocaleTestFiles.stringsFile("values").readText()
        HouseholdStringKeys.all.forEach { key ->
            val value = LocaleTestFiles.readStringValue(contents, key)
            assertTrue(value.isNotBlank(), "Default locale value for '$key' must not be blank")
        }
    }
}
