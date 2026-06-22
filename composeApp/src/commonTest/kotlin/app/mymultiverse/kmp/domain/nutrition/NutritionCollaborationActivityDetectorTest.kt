package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NutritionCollaborationActivityDetectorTest {

    @Test
    fun detectGroceryChanges_findsAddedItem() {
        val activities = NutritionCollaborationActivityDetector.detectGroceryChanges(
            before = emptyList(),
            after = listOf(GroceryItem(id = "1", label = "Milk")),
            actorUserId = "user-maria",
        )

        assertEquals(1, activities.size)
        assertEquals(NutritionCollaborationActivityKind.GroceryAdded, activities.single().kind)
        assertEquals("Milk", activities.single().itemLabel)
        assertEquals("user-maria", activities.single().actorUserId)
    }

    @Test
    fun detectGroceryChanges_findsCheckedItem() {
        val activities = NutritionCollaborationActivityDetector.detectGroceryChanges(
            before = listOf(GroceryItem(id = "1", label = "Milk", isChecked = false)),
            after = listOf(GroceryItem(id = "1", label = "Milk", isChecked = true)),
            actorUserId = "user-maria",
        )

        assertEquals(1, activities.size)
        assertEquals(NutritionCollaborationActivityKind.GroceryChecked, activities.single().kind)
    }

    @Test
    fun detectGroceryChanges_ignoresUncheckedAndUnchanged() {
        val item = GroceryItem(id = "1", label = "Milk", isChecked = true)
        val activities = NutritionCollaborationActivityDetector.detectGroceryChanges(
            before = listOf(item),
            after = listOf(item),
            actorUserId = "user-maria",
        )

        assertTrue(activities.isEmpty())
    }
}
