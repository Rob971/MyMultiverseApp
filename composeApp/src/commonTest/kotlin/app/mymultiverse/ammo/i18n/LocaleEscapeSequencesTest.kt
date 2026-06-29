package app.mymultiverse.ammo.i18n

import kotlin.test.Test
import kotlin.test.assertFalse

class LocaleEscapeSequencesTest {

    @Test
    fun noVisibleBackslashApostropheEscapesInAnyLocale() {
        NutritionStringKeys.localeDirectories.forEach { localeDir ->
            val contents = LocaleTestFiles.stringsFile(localeDir).readText()
            LocaleTestFiles.readStringKeys(contents).forEach { key ->
                val rawValue = LocaleTestFiles.readStringValue(contents, key)
                assertFalse(
                    rawValue.contains("\\'"),
                    "Locale '$localeDir' key '$key' uses \\\\' which can render a visible backslash in Compose resources.",
                )
            }
        }
    }
}
