package app.mymultiverse.ammo.data.service

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemoteNutritionAiAssistantServiceTest {

    private fun makeService(
        geminiResult: Result<List<String>> = Result.success(listOf("Polpo", "Patate")),
        languageCode: String = "it",
    ): RemoteNutritionAiAssistantService {
        val local = LocalNutritionAiAssistantService(
            responseDelayMs = 0,
            currentLanguageCode = { languageCode },
        )
        val fakeClient = FakeDishIngredientClient(geminiResult)
        return RemoteNutritionAiAssistantService(
            local = local,
            geminiClient = fakeClient,
            currentLanguageCode = { languageCode },
        )
    }

    private class FakeDishIngredientClient(
        private val result: Result<List<String>>,
    ) : DishIngredientClient {
        override suspend fun generateIngredients(dish: String, languageCode: String) = result
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
}
