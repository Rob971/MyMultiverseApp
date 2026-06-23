package app.mymultiverse.kmp.domain.nutrition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MealGroceryPartitionTest {

    @Test
    fun partition_separatesPantryStaplesFromShopping() {
        val result = MealGroceryPartition.partition(
            listOf("Chicken breast", "Garlic", "Olive oil", "Salt", "Black pepper", "Broccoli"),
        )

        assertEquals(listOf("Chicken breast", "Broccoli"), result.shopping)
        assertTrue(result.pantryStaples.containsAll(listOf("Garlic", "Olive oil", "Salt", "Black pepper")))
    }

    @Test
    fun partition_skipsBlankLabels() {
        val result = MealGroceryPartition.partition(listOf("Milk", "  ", "Salt"))

        assertEquals(listOf("Milk"), result.shopping)
        assertEquals(listOf("Salt"), result.pantryStaples)
    }

    @Test
    fun partition_classifiesGarlicAsPantryAndPastaAsShopping() {
        val result = MealGroceryPartition.partition(listOf("Garlic", "Pasta"))

        assertEquals(listOf("Pasta"), result.shopping)
        assertEquals(listOf("Garlic"), result.pantryStaples)
    }

    @Test
    fun isPantryStaple_matchesCommonStaples() {
        assertTrue(MealGroceryPartition.isPantryStaple("Olive oil"))
        assertFalse(MealGroceryPartition.isPantryStaple("Chicken breast"))
    }
}
