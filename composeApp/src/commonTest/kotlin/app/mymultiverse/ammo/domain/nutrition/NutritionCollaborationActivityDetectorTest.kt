package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
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

    // ---------------------------------------------------------------
    // S6: GroceryRemoved
    // ---------------------------------------------------------------

    @Test
    fun detectGroceryChanges_findsRemovedItem() {
        val removed = GroceryItem(id = "1", label = "Milk", isChecked = false)
        val activities = NutritionCollaborationActivityDetector.detectGroceryChanges(
            before = listOf(removed, GroceryItem(id = "2", label = "Eggs")),
            after = listOf(GroceryItem(id = "2", label = "Eggs")),
            actorUserId = "user-maria",
        )

        assertEquals(1, activities.size)
        assertEquals(NutritionCollaborationActivityKind.GroceryRemoved, activities.single().kind)
        assertEquals("Milk", activities.single().itemLabel)
        assertEquals("user-maria", activities.single().actorUserId)
        assertEquals(removed, activities.single().removedItem, "undo must restore the original item")
    }

    @Test
    fun detectGroceryChanges_findsRemovedAndAddedTogether() {
        val activities = NutritionCollaborationActivityDetector.detectGroceryChanges(
            before = listOf(GroceryItem(id = "1", label = "Milk")),
            after = listOf(GroceryItem(id = "2", label = "Pasta")),
            actorUserId = "user-maria",
        )

        val kinds = activities.map { it.kind }.toSet()
        assertEquals(setOf(NutritionCollaborationActivityKind.GroceryRemoved, NutritionCollaborationActivityKind.GroceryAdded), kinds)
    }

    @Test
    fun detectGroceryChanges_nothingRemoved_whenIdsOverlap() {
        val activities = NutritionCollaborationActivityDetector.detectGroceryChanges(
            before = listOf(GroceryItem(id = "1", label = "Milk")),
            after = listOf(GroceryItem(id = "1", label = "Milk"), GroceryItem(id = "2", label = "Eggs")),
            actorUserId = "user-maria",
        )

        assertTrue(activities.none { it.kind == NutritionCollaborationActivityKind.GroceryRemoved })
    }

    @Test
    fun detectGroceryChanges_emptyBefore_emitsNoRemoved() {
        val activities = NutritionCollaborationActivityDetector.detectGroceryChanges(
            before = emptyList(),
            after = listOf(GroceryItem(id = "1", label = "Milk")),
            actorUserId = "user-maria",
        )

        assertTrue(
            activities.none { it.kind == NutritionCollaborationActivityKind.GroceryRemoved },
            "a first sync must not announce removals",
        )
    }
}
