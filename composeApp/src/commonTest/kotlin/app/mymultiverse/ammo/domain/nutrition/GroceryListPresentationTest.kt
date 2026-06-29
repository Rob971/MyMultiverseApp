package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
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
    fun findItemByNormalizedLabel_returnsMatchingItem() {
        val milk = GroceryItem("1", "Milk", isChecked = false)
        val items = listOf(milk)

        assertEquals(milk, GroceryListPresentation.findItemByNormalizedLabel(items, "  milk "))
        assertEquals(null, GroceryListPresentation.findItemByNormalizedLabel(items, "Bread"))
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

    @Test
    fun moveActiveItem_reordersUncheckedItemsOnly() {
        val items = listOf(
            GroceryItem("1", "Milk", isChecked = false),
            GroceryItem("2", "Bread", isChecked = false),
            GroceryItem("3", "Eggs", isChecked = true),
        )

        val movedDown = GroceryListPresentation.moveActiveItem(items, "1", direction = 1)
        assertEquals(listOf("Bread", "Milk", "Eggs"), movedDown.map { it.label })

        val movedUp = GroceryListPresentation.moveActiveItem(movedDown, "1", direction = -1)
        assertEquals(listOf("Milk", "Bread", "Eggs"), movedUp.map { it.label })
    }

    @Test
    fun moveActiveItem_ignoresCheckedItems() {
        val items = listOf(
            GroceryItem("1", "Milk", isChecked = true),
            GroceryItem("2", "Bread", isChecked = false),
        )

        val result = GroceryListPresentation.moveActiveItem(items, "1", direction = 1)
        assertEquals(items, result)
    }
}
