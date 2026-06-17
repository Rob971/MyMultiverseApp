package app.mymultiverse.kmp.data.local.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionLocalStoreTest {

    private val weekKey = "2026-05-18"

    @Test
    fun saveGroceryItems_persistsUnderScopedKey() = runTest {
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, householdId = "household-1", weekKey = weekKey)
        val items = listOf(GroceryItem("1", "Rice", false))

        store.saveGroceryItems(items)

        assertEquals(items, store.observeGroceryItems().first())
        assertEquals(
            items,
            NutritionLocalStore(settings, householdId = "household-1", weekKey = weekKey)
                .observeGroceryItems()
                .first(),
        )
    }

    @Test
    fun applyPayload_updatesGroceryFromRemoteBlob() = runTest {
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, householdId = "household-1", weekKey = weekKey)
        val payload = store.encodeGrocery(listOf(GroceryItem("1", "Beans", true)))

        store.applyPayload("grocery", payload)

        assertEquals("Beans", store.observeGroceryItems().first().single().label)
        assertEquals(true, store.observeGroceryItems().first().single().isChecked)
    }

    @Test
    fun personalAndHouseholdScopes_useSeparateKeys() = runTest {
        val settings = MapSettings()
        val personal = NutritionLocalStore(settings, householdId = null, weekKey = weekKey)
        val shared = NutritionLocalStore(settings, householdId = "household-1", weekKey = weekKey)

        personal.saveGroceryItems(listOf(GroceryItem("p", "Personal", false)))
        shared.saveGroceryItems(listOf(GroceryItem("s", "Shared", false)))

        assertEquals("Personal", personal.observeGroceryItems().first().single().label)
        assertEquals("Shared", shared.observeGroceryItems().first().single().label)
    }

    @Test
    fun applyPayload_mealPlan_roundTrips() = runTest {
        val settings = MapSettings()
        val store = NutritionLocalStore(settings, householdId = "household-1", weekKey = weekKey)
        val plan = WeeklyMealPlan(weekKey = weekKey)
        val payload = store.encodeMealPlan(plan)

        store.applyPayload("meal_plan", payload)

        assertEquals(weekKey, store.observeMealPlan().first().weekKey)
    }
}
