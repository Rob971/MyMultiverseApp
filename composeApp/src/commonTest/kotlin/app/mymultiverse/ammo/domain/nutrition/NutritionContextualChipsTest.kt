package app.mymultiverse.ammo.domain.nutrition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NutritionContextualChipsTest {

    @Test
    fun ingredientsFromHistory_prioritizesUncheckedGroceryThenMeals() {
        val result = NutritionContextualChips.ingredientsFromHistory(
            mealTexts = listOf("Chicken stir fry", "Pasta primavera"),
            groceryLabels = listOf("Tomatoes", "Milk"),
            maxChips = 3,
        )

        assertEquals(
            listOf("tomatoes", "chicken", "pasta"),
            result.map { it.id },
        )
    }

    @Test
    fun ingredientsFromHistory_deduplicatesAcrossSources() {
        val result = NutritionContextualChips.ingredientsFromHistory(
            mealTexts = listOf("Chicken tacos"),
            groceryLabels = listOf("Chicken breast"),
            maxChips = 2,
        )

        assertEquals(1, result.size)
        assertEquals("chicken", result.single().id)
    }

    @Test
    fun ingredientsFromHistory_returnsEmptyWhenNoSignals() {
        val result = NutritionContextualChips.ingredientsFromHistory(
            mealTexts = listOf("Leftovers"),
            groceryLabels = listOf("Paper towels"),
            maxChips = 3,
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun ingredientsFromHistory_localizesDisplayNamesForSelectedLanguage() {
        val result = NutritionContextualChips.ingredientsFromHistory(
            mealTexts = listOf("Pasta con pollo"),
            groceryLabels = listOf("Tomates"),
            maxChips = 2,
            languageCode = "es",
        )

        assertEquals(listOf("tomatoes", "chicken"), result.map { it.id })
        assertEquals(listOf("Tomates", "Pollo"), result.map { it.displayName })
    }
}
