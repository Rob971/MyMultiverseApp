package app.mymultiverse.kmp.data.service

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalNutritionAdviceServiceTest {

    @Test
    fun ask_blankQuestion_returnsEmptyQuestionFailure() = runTest {
        val service = LocalNutritionAdviceService(responseDelayMs = 0)

        val result = service.ask("   ")

        assertTrue(result.isFailure)
        assertEquals("empty_question", result.exceptionOrNull()?.message)
    }

    @Test
    fun ask_validQuestion_returnsSuccess() = runTest {
        val service = LocalNutritionAdviceService(responseDelayMs = 0)

        val result = service.ask("High protein lunches")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.contains("protein", ignoreCase = true))
    }

    @Test
    fun ask_trimsQuestionBeforeBuildingAdvice() = runTest {
        val service = LocalNutritionAdviceService(responseDelayMs = 0)

        val result = service.ask("  protein lunch  ")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.contains("protein", ignoreCase = true))
    }
}
