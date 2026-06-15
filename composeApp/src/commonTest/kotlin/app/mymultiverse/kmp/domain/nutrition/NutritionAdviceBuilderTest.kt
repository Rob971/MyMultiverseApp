package app.mymultiverse.kmp.domain.nutrition

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class NutritionAdviceBuilderTest {

    @Test
    fun buildAdvice_proteinQuestion_mentionsProtein() {
        val answer = NutritionAdviceBuilder.buildAdvice("How much protein at lunch?")

        assertContains(answer.lowercase(), "protein")
    }

    @Test
    fun buildAdvice_vegetarianQuestion_mentionsPlantFoods() {
        val answer = NutritionAdviceBuilder.buildAdvice("We are vegetarian")

        assertContains(answer.lowercase(), "beans")
    }

    @Test
    fun buildAdvice_childQuestion_mentionsFamilyMeals() {
        val answer = NutritionAdviceBuilder.buildAdvice("Meals for kids")

        assertContains(answer.lowercase(), "family")
    }

    @Test
    fun buildAdvice_budgetQuestion_mentionsBatchCooking() {
        val answer = NutritionAdviceBuilder.buildAdvice("Cheap weekly meals")

        assertContains(answer.lowercase(), "batch")
    }

    @Test
    fun buildAdvice_allergyQuestion_mentionsAllergens() {
        val answer = NutritionAdviceBuilder.buildAdvice("Nut allergy planning")

        assertContains(answer.lowercase(), "allerg")
    }

    @Test
    fun buildAdvice_weightQuestion_mentionsBalancedPlates() {
        val answer = NutritionAdviceBuilder.buildAdvice("Calorie goals")

        assertContains(answer.lowercase(), "balanced")
    }

    @Test
    fun buildAdvice_genericQuestion_echoesQuestion() {
        val question = "What should we eat on Tuesday?"
        val answer = NutritionAdviceBuilder.buildAdvice(question)

        assertContains(answer, question)
    }

    @Test
    fun buildAdvice_isCaseInsensitive() {
        val answer = NutritionAdviceBuilder.buildAdvice("PROTEIN AT DINNER")

        assertContains(answer.lowercase(), "protein")
    }

    @Test
    fun buildAdvice_returnsNonBlankText() {
        assertTrue(NutritionAdviceBuilder.buildAdvice("hello").isNotBlank())
    }
}
