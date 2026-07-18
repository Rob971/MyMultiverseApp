package app.mymultiverse.ammo.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class ProductTourLocaleStringsTest {

    @Test
    fun allSupportedLocalesDefineEveryProductTourStringKey() {
        ProductTourStringKeys.localeDirectories.forEach { localeDir ->
            val stringsFile = LocaleTestFiles.stringsFile(localeDir)
            assertTrue(stringsFile.exists(), "Missing strings file: ${stringsFile.path}")

            val keysInFile = LocaleTestFiles.readStringKeys(stringsFile.readText())
            val missing = ProductTourStringKeys.all - keysInFile

            if (missing.isNotEmpty()) {
                fail("Locale '$localeDir' is missing product-tour keys: ${missing.sorted().joinToString()}")
            }
            assertEquals(
                ProductTourStringKeys.all,
                keysInFile.intersect(ProductTourStringKeys.all),
            )
        }
    }

    @Test
    fun productTourStringValuesAreNotBlankInDefaultLocale() {
        val contents = LocaleTestFiles.stringsFile("values").readText()

        ProductTourStringKeys.all.forEach { key ->
            val value = LocaleTestFiles.readStringValue(contents, key)
            assertTrue(value.isNotBlank(), "Default locale value for '$key' must not be blank")
        }
    }

    @Test
    fun tourStepCounterPlaceholderPresentInEveryLocale() {
        ProductTourStringKeys.localeDirectories.forEach { localeDir ->
            val contents = LocaleTestFiles.stringsFile(localeDir).readText()
            val value = LocaleTestFiles.readStringValue(contents, "tour_step_counter")
            assertTrue(
                value.contains("%1\$d") && value.contains("%2\$d"),
                "Locale '$localeDir' must use %1\$d and %2\$d placeholders in tour_step_counter",
            )
        }
    }
}
