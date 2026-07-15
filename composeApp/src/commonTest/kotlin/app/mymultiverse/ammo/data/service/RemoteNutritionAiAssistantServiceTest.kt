package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.domain.service.AiKeyNotConfiguredException
import app.mymultiverse.ammo.domain.service.GeminiApiException
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RemoteNutritionAiAssistantServiceTest {

    private val noOpLogger = AppLogger(NoOpCrashReporter(), DiagnosticsContext())

    private class RecordingCrashReporter : app.mymultiverse.ammo.domain.observability.CrashReporter {
        val nonFatals = mutableListOf<Pair<Throwable, Map<String, String>>>()

        override fun initialize() = Unit
        override fun setUserId(userId: String?) = Unit
        override fun logBreadcrumb(message: String) = Unit
        override fun recordNonFatal(throwable: Throwable, context: Map<String, String>) {
            nonFatals += throwable to context
        }
    }

    private fun recordingLogger(): Pair<AppLogger, RecordingCrashReporter> {
        val crashReporter = RecordingCrashReporter()
        return AppLogger(crashReporter, DiagnosticsContext(sessionId = "test")) to crashReporter
    }

    private fun makeService(
        geminiResult: Result<List<String>> = Result.success(listOf("Polpo", "Patate")),
        languageCode: String = "it",
        apiKey: String = "test-api-key",
        geminiApi: GeminiTextClient = FakeGeminiTextClient(Result.failure(UnsupportedOperationException("not used"))),
        appLogger: AppLogger = noOpLogger,
    ): RemoteNutritionAiAssistantService {
        val local = LocalNutritionAiAssistantService(
            responseDelayMs = 0,
            currentLanguageCode = { languageCode },
        )
        val fakeSettings = FakeAiAssistantSettings(apiKey)
        val fakeClient = FakeDishIngredientClient(geminiResult)
        return RemoteNutritionAiAssistantService(
            local = local,
            geminiClient = fakeClient,
            geminiApi = geminiApi,
            currentLanguageCode = { languageCode },
            aiSettings = fakeSettings,
            appLogger = appLogger,
        )
    }

    private class FakeGeminiTextClient(
        private val result: Result<String>,
    ) : GeminiTextClient {
        override suspend fun complete(prompt: String, maxOutputTokens: Int, temperature: Double) = result
    }

    private class FakeDishIngredientClient(
        private val result: Result<List<String>>,
    ) : DishIngredientClient {
        var callCount = 0
        override suspend fun generateIngredients(dish: String, languageCode: String): Result<List<String>> {
            callCount++
            return result
        }
    }

    private class FakeAiAssistantSettings(key: String = "") : AiAssistantSettings {
        private val _key = MutableStateFlow(key)
        override val geminiApiKey: StateFlow<String> = _key
        override fun setGeminiApiKey(key: String) { _key.value = key.trim() }
        override fun clearGeminiApiKey() { _key.value = "" }
    }

    @Test
    fun generateGroceryForMeal_usesGeminiResult_whenSuccessful() = runTest {
        val service = makeService(geminiResult = Result.success(listOf("Polpo", "Patate", "Prezzemolo")))

        val result = service.generateGroceryForMeal("Polpo e patate con prezzemolo")

        assertTrue(result.isSuccess)
        assertEquals(listOf("Polpo", "Patate", "Prezzemolo"), result.getOrNull())
    }

    @Test
    fun generateGroceryForMeal_fallsBackToLocal_whenGeminiFails() = runTest {
        val service = makeService(
            geminiResult = Result.failure(RuntimeException("Network error")),
            languageCode = "it",
        )

        val result = service.generateGroceryForMeal("Pollo alla cacciatora con olive")

        assertTrue(result.isSuccess)
        val items = result.getOrNull().orEmpty()
        assertTrue(items.isNotEmpty(), "Expected non-empty fallback ingredients")
    }

    @Test
    fun generateGroceryForMeal_fallsBackToLocal_whenGeminiReturnsEmpty() = runTest {
        val service = makeService(geminiResult = Result.success(emptyList()))

        val result = service.generateGroceryForMeal("Pollo alla cacciatora")

        assertTrue(result.isSuccess)
        val items = result.getOrNull().orEmpty()
        assertTrue(items.isNotEmpty(), "Expected non-empty fallback ingredients")
    }

    @Test
    fun generateGroceryForMeal_blankInput_returnsFailure() = runTest {
        val service = makeService()

        val result = service.generateGroceryForMeal("   ")

        assertFalse(result.isSuccess)
        assertEquals("empty_meal", result.exceptionOrNull()?.message)
    }

    @Test
    fun generateGroceryForMeal_geminiReturnsNull_fallsBackToLocal() = runTest {
        val service = makeService(geminiResult = Result.success(emptyList()))

        val result = service.generateGroceryForMeal("Pasta e fagioli")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isNotEmpty())
    }

    @Test
    fun askAdvice_promptIncludesLanguageDirective() = runTest {
        var capturedPrompt = ""
        val fakeApi = object : GeminiTextClient {
            override suspend fun complete(prompt: String, maxOutputTokens: Int, temperature: Double): Result<String> {
                capturedPrompt = prompt
                return Result.success("Consiglio pratico in italiano.")
            }
        }
        val service = makeService(apiKey = "test-key", geminiApi = fakeApi, languageCode = "it")

        service.askAdvice("Quanta proteina a pranzo?")

        assertContains(capturedPrompt, "Italian")
        assertContains(capturedPrompt, "locale: it")
    }

    @Test
    fun askAdvice_usesGemini_whenKeyPresent() = runTest {
        val fakeApi = FakeGeminiTextClient(Result.success("Eat more protein-rich foods like eggs and legumes."))
        val service = makeService(apiKey = "test-key", geminiApi = fakeApi)

        val result = service.askAdvice("High protein lunches")

        assertTrue(result.isSuccess)
        assertEquals("Eat more protein-rich foods like eggs and legumes.", result.getOrNull())
    }

    @Test
    fun askAdvice_fallsBackToLocal_whenGeminiFails() = runTest {
        val fakeApi = FakeGeminiTextClient(Result.failure(RuntimeException("network error")))
        val service = makeService(apiKey = "test-key", geminiApi = fakeApi, languageCode = "it")

        val result = service.askAdvice("Pasti proteici")

        assertTrue(result.isSuccess)
        val answer = result.getOrNull().orEmpty()
        assertContains(answer.lowercase(), "proteine")
    }

    @Test
    fun generateGroceryList_usesGemini_whenKeyPresent() = runTest {
        val fakeApi = FakeGeminiTextClient(Result.success("[\"Chicken\",\"Quinoa\",\"Spinach\"]"))
        val service = makeService(apiKey = "test-key", geminiApi = fakeApi)

        val result = service.generateGroceryList("high protein")

        assertTrue(result.isSuccess)
        assertEquals(listOf("Chicken", "Quinoa", "Spinach"), result.getOrNull())
    }

    @Test
    fun generateMealPlan_usesGemini_whenKeyPresent() = runTest {
        val mealPlanJson = """{"days":[{"lunch":"Pasta al pomodoro","dinner":"Pollo arrosto"},{"lunch":"Insalata","dinner":"Branzino"},{"lunch":"Risotto","dinner":"Cotoletta"},{"lunch":"Zuppa","dinner":"Salmone"},{"lunch":"Panino","dinner":"Polpette"},{"lunch":"Frittata","dinner":"Tagliata"},{"lunch":"Pasta","dinner":"Pizza"}],"summary":"Piano settimanale equilibrato"}"""
        val fakeApi = FakeGeminiTextClient(Result.success(mealPlanJson))
        val service = makeService(apiKey = "test-key", geminiApi = fakeApi)
        val emptyPlan = app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan(weekKey = "2026-07-15")

        val result = service.generateMealPlan("equilibrato", app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope.FullWeek, emptyPlan)

        assertTrue(result.isSuccess)
        val gen = result.getOrNull()!!
        assertEquals(7, gen.days.size)
        assertEquals("Pasta al pomodoro", gen.days[0].lunch)
        assertEquals("Pollo arrosto", gen.days[0].dinner)
        assertEquals("Piano settimanale equilibrato", gen.summary)
    }

    @Test
    fun generateMealPlan_returnsFailure_whenGeminiFails() = runTest {
        val fakeApi = FakeGeminiTextClient(Result.failure(RuntimeException("network error")))
        val (logger, crashReporter) = recordingLogger()
        val service = makeService(apiKey = "test-key", geminiApi = fakeApi, appLogger = logger)
        val emptyPlan = app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan(weekKey = "2026-07-15")

        val result = service.generateMealPlan("balanced", app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope.FullWeek, emptyPlan)

        assertFalse(result.isSuccess)
        assertIs<GeminiApiException>(result.exceptionOrNull())
        assertEquals(1, crashReporter.nonFatals.size)
        assertEquals("api_call", crashReporter.nonFatals.single().second["stage"])
    }

    @Test
    fun generateMealPlan_returnsAuthFailure_whenGeminiRejectsKey() = runTest {
        val fakeApi = FakeGeminiTextClient(
            Result.failure(GeminiApiException(GeminiApiException.Reason.AUTH_ERROR, httpStatus = 403)),
        )
        val service = makeService(apiKey = "bad-key", geminiApi = fakeApi)
        val emptyPlan = app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan(weekKey = "2026-07-15")

        val result = service.generateMealPlan(
            "protein dinner",
            app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope.SingleMeal(2, app.mymultiverse.ammo.domain.nutrition.MealSlot.Dinner),
            emptyPlan,
        )

        assertFalse(result.isSuccess)
        val error = result.exceptionOrNull()
        assertIs<GeminiApiException>(error)
        assertTrue(error.isAuthError)
    }

    @Test
    fun generateGroceryForMeal_declines_whenApiKeyBlank() = runTest {
        val fakeClient = FakeDishIngredientClient(Result.success(listOf("Remote ingredient")))
        val fakeSettings = FakeAiAssistantSettings(key = "")
        val local = LocalNutritionAiAssistantService(responseDelayMs = 0, currentLanguageCode = { "it" })
        val service = RemoteNutritionAiAssistantService(
            local = local,
            geminiClient = fakeClient,
            geminiApi = FakeGeminiTextClient(Result.failure(UnsupportedOperationException())),
            currentLanguageCode = { "it" },
            aiSettings = fakeSettings,
            appLogger = noOpLogger,
        )

        val result = service.generateGroceryForMeal("Pollo alla cacciatora")

        assertFalse(result.isSuccess, "Should decline when API key is blank")
        assertIs<AiKeyNotConfiguredException>(result.exceptionOrNull())
        assertEquals(0, fakeClient.callCount, "Gemini must not be called when key is blank")
    }

    @Test
    fun askAdvice_declines_whenApiKeyBlank() = runTest {
        val service = makeService(apiKey = "")

        val result = service.askAdvice("High protein lunches")

        assertFalse(result.isSuccess)
        assertIs<AiKeyNotConfiguredException>(result.exceptionOrNull())
    }

    @Test
    fun generateGroceryList_declines_whenApiKeyBlank() = runTest {
        val service = makeService(apiKey = "")

        val result = service.generateGroceryList("vegetarian")

        assertFalse(result.isSuccess)
        assertIs<AiKeyNotConfiguredException>(result.exceptionOrNull())
    }

    @Test
    fun generateMealPlan_declines_whenApiKeyBlank() = runTest {
        val service = makeService(apiKey = "")
        val emptyPlan = app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan(weekKey = "2026-07-15")

        val result = service.generateMealPlan(
            "balanced",
            app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope.FullWeek,
            emptyPlan,
        )

        assertFalse(result.isSuccess)
        assertIs<AiKeyNotConfiguredException>(result.exceptionOrNull())
    }

    @Test
    fun geminiApiKey_exposesSettingsFlow() {
        val fakeSettings = FakeAiAssistantSettings(key = "my-key")
        val local = LocalNutritionAiAssistantService(responseDelayMs = 0, currentLanguageCode = { "en" })
        val service = RemoteNutritionAiAssistantService(
            local = local,
            geminiClient = FakeDishIngredientClient(Result.success(emptyList())),
            geminiApi = FakeGeminiTextClient(Result.failure(UnsupportedOperationException())),
            currentLanguageCode = { "en" },
            aiSettings = fakeSettings,
            appLogger = noOpLogger,
        )

        assertEquals("my-key", service.geminiApiKey.value)

        fakeSettings.setGeminiApiKey("new-key")
        assertEquals("new-key", service.geminiApiKey.value)
    }
}
