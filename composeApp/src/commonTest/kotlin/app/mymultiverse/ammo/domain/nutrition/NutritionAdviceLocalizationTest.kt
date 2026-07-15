package app.mymultiverse.ammo.domain.nutrition

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NutritionAdviceLocalizationTest {

    @Test
    fun categoryFor_italianProteinKeyword_returnsProtein() {
        assertEquals(
            NutritionAdviceLocalization.Category.Protein,
            NutritionAdviceLocalization.categoryFor("Pasti proteici a pranzo"),
        )
    }

    @Test
    fun advice_proteinItalian_returnsItalianText() {
        val answer = NutritionAdviceLocalization.advice(
            NutritionAdviceLocalization.Category.Protein,
            languageCode = "it",
        )

        assertContains(answer.lowercase(), "proteine")
        assertContains(answer.lowercase(), "legumi")
    }

    @Test
    fun advice_genericNeapolitan_echoesQuestionInNeapolitanTemplate() {
        val question = "Cosa mangiamo martedì?"
        val answer = NutritionAdviceLocalization.advice(
            NutritionAdviceLocalization.Category.Generic,
            languageCode = "nap",
            question = question,
        )

        assertContains(answer, question)
        assertContains(answer.lowercase(), "famiglia")
    }

    @Test
    fun buildAdvice_spanishBudgetQuestion_returnsSpanishText() {
        val answer = NutritionAdviceBuilder.buildAdvice("Comidas económicas semanales", languageCode = "es")

        assertContains(answer.lowercase(), "comida")
        assertTrue("batch" !in answer.lowercase() || "lote" in answer.lowercase())
    }
}
