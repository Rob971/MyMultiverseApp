package app.mymultiverse.ammo.domain.nutrition

object NutritionAdviceBuilder {
    fun buildAdvice(question: String, languageCode: String = "en"): String {
        val trimmed = question.trim()
        val category = NutritionAdviceLocalization.categoryFor(trimmed)
        return NutritionAdviceLocalization.advice(category, languageCode, trimmed)
    }
}
