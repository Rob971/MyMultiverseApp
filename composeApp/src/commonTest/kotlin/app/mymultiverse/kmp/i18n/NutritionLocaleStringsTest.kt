package app.mymultiverse.kmp.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class NutritionLocaleStringsTest {

    @Test
    fun allSupportedLocalesDefineEveryNutritionStringKey() {
        NutritionStringKeys.localeDirectories.forEach { localeDir ->
            val stringsFile = LocaleTestFiles.stringsFile(localeDir)
            assertTrue(stringsFile.exists(), "Missing strings file: ${stringsFile.path}")

            val keysInFile = LocaleTestFiles.readStringKeys(stringsFile.readText())
            val missing = NutritionStringKeys.all - keysInFile

            if (missing.isNotEmpty()) {
                fail("Locale '$localeDir' is missing nutrition keys: ${missing.sorted().joinToString()}")
            }
            assertEquals(NutritionStringKeys.all, keysInFile.intersect(NutritionStringKeys.all))
        }
    }

    @Test
    fun nutritionStringValuesAreNotBlankInDefaultLocale() {
        val contents = LocaleTestFiles.stringsFile("values").readText()

        NutritionStringKeys.all.forEach { key ->
            val value = LocaleTestFiles.readStringValue(contents, key)
            assertTrue(value.isNotBlank(), "Default locale value for '$key' must not be blank")
        }
    }
}
