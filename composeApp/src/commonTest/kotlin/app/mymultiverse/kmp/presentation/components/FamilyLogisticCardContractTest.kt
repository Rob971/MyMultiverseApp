package app.mymultiverse.kmp.presentation.components

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Regression guard for AppCompat + Material3: `Surface(onClick = …)` crashes at runtime
 * but is not caught by JVM unit tests unless we assert the implementation pattern.
 */
class FamilyLogisticCardContractTest {

    @Test
    fun familyLogisticCard_avoidsMaterial3SurfaceOnClick() {
        val source = readComponentSource("FamilyLogisticCard.kt")

        val surfaceOnClick = Regex("""Surface\s*\([^)]*onClick\s*=""")
        assertFalse(
            surfaceOnClick.containsMatchIn(source),
            "FamilyLogisticCard must use Modifier.clickable instead of Surface(onClick); " +
                "Surface(onClick) crashes with the AppCompat activity theme.",
        )
        assertTrue(
            source.contains("Modifier.clickable"),
            "FamilyLogisticCard should use Modifier.clickable for tap handling.",
        )
    }

    @Test
    fun groceryAndAiScreens_avoidMaterial3SurfaceOnClick() {
        listOf(
            "GroceryShoppingScreen.kt",
            "NutritionAiAdviceScreen.kt",
        ).forEach { fileName ->
            val source = readScreenSource(fileName)
            val surfaceOnClick = Regex("""Surface\s*\([^)]*onClick\s*=""")
            assertFalse(
                surfaceOnClick.containsMatchIn(source),
                "$fileName must not use Surface(onClick); use Modifier.clickable instead.",
            )
        }
    }

    @Test
    fun appEntry_doesNotWrapKoinApplicationWithKoinContext() {
        val source = readPresentationSource("App.kt")
        assertFalse(
            source.contains("KoinContext"),
            "App() is hosted inside KoinApplication on Android/iOS; an extra KoinContext is redundant.",
        )
    }

    @Test
    fun iosEntryPoint_startsKoinBeforeApp() {
        val source = readIosEntrySource()
        assertTrue(
            source.contains("KoinApplication"),
            "iOS entry point must start Koin before composing App().",
        )
    }

    private fun readComponentSource(fileName: String): String =
        readProjectFile("composeApp/src/commonMain/kotlin/app/mymultiverse/kmp/presentation/components/$fileName")

    private fun readScreenSource(fileName: String): String =
        readProjectFile("composeApp/src/commonMain/kotlin/app/mymultiverse/kmp/presentation/screens/nutrition/$fileName")

    private fun readPresentationSource(fileName: String): String =
        readProjectFile("composeApp/src/commonMain/kotlin/app/mymultiverse/kmp/presentation/$fileName")

    private fun readIosEntrySource(): String =
        readProjectFile("composeApp/src/iosMain/kotlin/app/mymultiverse/kmp/IosEntryPoint.kt")

    private fun readProjectFile(relativePath: String): String {
        val file = locateProjectRoot().resolve(relativePath)
        if (!file.exists()) {
            fail("Expected source file at ${file.path}")
        }
        return file.readText()
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
