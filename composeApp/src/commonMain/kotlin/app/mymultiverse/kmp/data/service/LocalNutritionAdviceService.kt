package app.mymultiverse.kmp.data.service

import app.mymultiverse.kmp.domain.nutrition.NutritionAdviceBuilder
import app.mymultiverse.kmp.domain.service.NutritionAdviceService
import kotlinx.coroutines.delay

class LocalNutritionAdviceService(
    private val responseDelayMs: Long = DEFAULT_DELAY_MS,
) : NutritionAdviceService {
    override suspend fun ask(question: String): Result<String> {
        if (question.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_question"))
        }

        if (responseDelayMs > 0) {
            delay(responseDelayMs)
        }
        return Result.success(NutritionAdviceBuilder.buildAdvice(question.trim()))
    }

    companion object {
        const val DEFAULT_DELAY_MS = 700L
    }
}
