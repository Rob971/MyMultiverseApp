package app.mymultiverse.kmp.domain.service

interface NutritionAdviceService {
    suspend fun ask(question: String): Result<String>
}
