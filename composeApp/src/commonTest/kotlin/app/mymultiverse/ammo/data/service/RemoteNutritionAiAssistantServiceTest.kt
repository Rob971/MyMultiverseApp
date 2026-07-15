package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemoteNutritionAiAssistantServiceTest {

    private fun makeService(
        geminiResult: Result<List<String>> = Result.success(listOf("Polpo", "Patate")),
        languageCode: String = "it",
        apiKey: String = "test-api-key",
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
            currentLanguageCode = { languageCode },
            aiSettings = fakeSettings,
        )
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
    fun askAdvice_delegatesToLocal() = runTest {
        val service = makeService()

        val result = service.askAdvice("High protein lunches")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.contains("protein", ignoreCase = true))
    }

    @Test
    fun generateGroceryList_delegatesToLocal() = runTest {
        val service = makeService()

        val result = service.generateGroceryList("vegetarian")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isNotEmpty())
    }

    @Test
    fun generateGroceryForMeal_skipsGemini_whenApiKeyBlank() = runTest {
        val fakeClient = FakeDishIngredientClient(Result.success(listOf("Remote ingredient")))
        val fakeSettings = FakeAiAssistantSettings(key = "")
        val local = LocalNutritionAiAssistantService(responseDelayMs = 0, currentLanguageCode = { "it" })
        val service = RemoteNutritionAiAssistantService(
            local = local,
            geminiClient = fakeClient,
            currentLanguageCode = { "it" },
            aiSettings = fakeSettings,
        )

        val result = service.generateGroceryForMeal("Pollo alla cacciatora")

        assertTrue(result.isSuccess)
        assertEquals(0, fakeClient.callCount, "Gemini should not be called when API key is blank")
        // Falls back to local — returns something (local planner generates for "Pollo")
        assertTrue(result.getOrNull()!!.isNotEmpty())
    }

    @Test
    fun geminiApiKey_exposesSettingsFlow() {
        val fakeSettings = FakeAiAssistantSettings(key = "my-key")
        val local = LocalNutritionAiAssistantService(responseDelayMs = 0, currentLanguageCode = { "en" })
        val service = RemoteNutritionAiAssistantService(
            local = local,
            geminiClient = FakeDishIngredientClient(Result.success(emptyList())),
            currentLanguageCode = { "en" },
            aiSettings = fakeSettings,
        )

        assertEquals("my-key", service.geminiApiKey.value)

        fakeSettings.setGeminiApiKey("new-key")
        assertEquals("new-key", service.geminiApiKey.value)
    }
}
