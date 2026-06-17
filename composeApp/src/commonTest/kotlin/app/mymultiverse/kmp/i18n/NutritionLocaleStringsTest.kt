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

    @Test
    fun formattedNutritionStringsContainExpectedPlaceholdersInEveryLocale() {
        val expectedPlaceholdersByKey = mapOf(
            "nutrition_week_label" to listOf("%1\$s"),
            "nutrition_week_dates" to listOf("%1\$s", "%2\$s"),
            "nutrition_grocery_progress" to listOf("%1\$d", "%2\$d"),
            "nutrition_meal_plan_progress" to listOf("%1\$d", "%2\$d"),
            "nutrition_meal_grocery_added" to listOf("%1\$d", "%2\$s", "%3\$s"),
            "nutrition_ai_grocery_summary" to listOf("%1\$d"),
            "nutrition_ai_meal_plan_summary_single_day" to listOf("%1\$s"),
            "nutrition_meal_plan_preview_line" to listOf("%1\$s", "%2\$s"),
            "nutrition_sync_status_pending" to listOf("%1\$d"),
        )

        NutritionStringKeys.localeDirectories.forEach { localeDir ->
            val contents = LocaleTestFiles.stringsFile(localeDir).readText()
            expectedPlaceholdersByKey.forEach { (key, placeholders) ->
                val value = LocaleTestFiles.readStringValue(contents, key)
                placeholders.forEach { placeholder ->
                    assertTrue(
                        value.contains(placeholder),
                        "Locale '$localeDir' must use $placeholder placeholder in $key",
                    )
                }
            }
        }
    }
}
