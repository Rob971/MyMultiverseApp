package app.mymultiverse.kmp.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class HomeLocaleStringsTest {

    @Test
    fun allSupportedLocalesDefineEveryHomeStringKey() {
        HomeStringKeys.localeDirectories.forEach { localeDir ->
            val stringsFile = LocaleTestFiles.stringsFile(localeDir)
            assertTrue(stringsFile.exists(), "Missing strings file: ${stringsFile.path}")

            val keysInFile = LocaleTestFiles.readStringKeys(stringsFile.readText())
            val missing = HomeStringKeys.all - keysInFile
            if (missing.isNotEmpty()) {
                fail("Locale '$localeDir' is missing home keys: ${missing.sorted().joinToString()}")
            }
            assertEquals(HomeStringKeys.all, keysInFile.intersect(HomeStringKeys.all))
        }
    }

    @Test
    fun homeStringValuesAreNotBlankInDefaultLocale() {
        val contents = LocaleTestFiles.stringsFile("values").readText()
        HomeStringKeys.all.forEach { key ->
            val value = LocaleTestFiles.readStringValue(contents, key)
            assertTrue(value.isNotBlank(), "Default locale value for '$key' must not be blank")
        }
    }

    @Test
    fun personalizedGreeting_containsNamePlaceholderInEveryLocale() {
        HomeStringKeys.localeDirectories.forEach { localeDir ->
            val contents = LocaleTestFiles.stringsFile(localeDir).readText()
            val value = LocaleTestFiles.readStringValue(contents, "home_greeting_personalized")
            assertTrue(
                value.contains("%1\$s"),
                "Locale '$localeDir' must use %1\$s placeholder in home_greeting_personalized",
            )
        }
    }

    @Test
    fun waitForInviteBody_containsEmailPlaceholderInEveryLocale() {
        HomeStringKeys.localeDirectories.forEach { localeDir ->
            val contents = LocaleTestFiles.stringsFile(localeDir).readText()
            val value = LocaleTestFiles.readStringValue(contents, "home_onboarding_wait_for_invite_body")
            assertTrue(
                value.contains("%1\$s"),
                "Locale '$localeDir' must use %1\$s placeholder in home_onboarding_wait_for_invite_body",
            )
        }
    }
}
