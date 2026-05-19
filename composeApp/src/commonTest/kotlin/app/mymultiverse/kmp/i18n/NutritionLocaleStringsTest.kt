package app.mymultiverse.kmp.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class NutritionLocaleStringsTest {

    @Test
    fun allSupportedLocalesDefineEveryNutritionStringKey() {
        val projectRoot = locateProjectRoot()
        val resourcesRoot = projectRoot.resolve("composeApp/src/commonMain/composeResources")

        NutritionStringKeys.localeDirectories.forEach { localeDir ->
            val stringsFile = resourcesRoot.resolve("$localeDir/strings.xml")
            assertTrue(
                actual = stringsFile.exists(),
                message = "Missing strings file: ${stringsFile.path}",
            )

            val keysInFile = readStringKeys(stringsFile.readText())
            val missing = NutritionStringKeys.all - keysInFile

            if (missing.isNotEmpty()) {
                fail(
                    "Locale '$localeDir' is missing nutrition keys: ${missing.sorted().joinToString()}",
                )
            }
            assertEquals(
                NutritionStringKeys.all,
                keysInFile.intersect(NutritionStringKeys.all),
                "Locale '$localeDir' nutrition key set mismatch",
            )
        }
    }

    @Test
    fun nutritionStringValuesAreNotBlankInDefaultLocale() {
        val projectRoot = locateProjectRoot()
        val defaultFile = projectRoot.resolve("composeApp/src/commonMain/composeResources/values/strings.xml")
        val contents = defaultFile.readText()

        NutritionStringKeys.all.forEach { key ->
            val value = readStringValue(contents, key)
            assertTrue(value.isNotBlank(), "Default locale value for '$key' must not be blank")
        }
    }

    private fun readStringKeys(xml: String): Set<String> {
        val regex = Regex("""<string\s+name="([^"]+)"""")
        return regex.findAll(xml).map { it.groupValues[1] }.toSet()
    }

    private fun readStringValue(xml: String, key: String): String {
        val regex = Regex("""<string\s+name="$key">([\s\S]*?)</string>""")
        return regex.find(xml)?.groupValues?.get(1)?.trim().orEmpty()
    }

    private fun locateProjectRoot(): java.io.File {
        var current = java.io.File(checkNotNull(System.getProperty("user.dir")))
        for (depth in 0..6) {
            if (current.resolve("composeApp").isDirectory) return current
            current = current.parentFile ?: break
        }
        error("Could not locate project root from user.dir=${System.getProperty("user.dir")}")
    }
}
