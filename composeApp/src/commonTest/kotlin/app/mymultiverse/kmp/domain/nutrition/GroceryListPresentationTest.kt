package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroceryListPresentationTest {

    @Test
    fun partition_splitsActiveAndCompleted() {
        val items = listOf(
            GroceryItem("1", "Milk", isChecked = false),
            GroceryItem("2", "Bread", isChecked = true),
            GroceryItem("3", "Eggs", isChecked = false),
        )

        val sections = GroceryListPresentation.partition(items)

        assertEquals(listOf("Milk", "Eggs"), sections.active.map { it.label })
        assertEquals(listOf("Bread"), sections.completed.map { it.label })
    }

    @Test
    fun isDuplicateLabel_ignoresCaseAndWhitespace() {
        val items = listOf(GroceryItem("1", "Olive Oil", isChecked = false))

        assertTrue(GroceryListPresentation.isDuplicateLabel(items, "  olive oil "))
        assertFalse(GroceryListPresentation.isDuplicateLabel(items, "olive oil", excludingId = "1"))
    }

    @Test
    fun forShoppingDisplay_hidesCompletedWhenRequested() {
        val sections = GroceryListPresentation.Sections(
            active = listOf(GroceryItem("1", "Milk")),
            completed = listOf(GroceryItem("2", "Bread", isChecked = true)),
        )

        val hidden = GroceryListPresentation.forShoppingDisplay(sections, hideCompleted = true)
        val shown = GroceryListPresentation.forShoppingDisplay(sections, hideCompleted = false)

        assertEquals(emptyList(), hidden.completed)
        assertEquals(1, shown.completed.size)
    }
}
