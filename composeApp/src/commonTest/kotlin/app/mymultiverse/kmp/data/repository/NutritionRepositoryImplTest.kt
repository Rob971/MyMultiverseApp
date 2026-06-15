package app.mymultiverse.kmp.data.repository

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionRepositoryImplTest {

    private val weekKey = "2026-05-18"

    @Test
    fun saveGroceryItems_persistsAndEmits() = runTest {
        val settings = MapSettings()
        val repository = NutritionRepositoryImpl(settings, weekKey)
        val items = listOf(GroceryItem(id = "a", label = "Bread", isChecked = true))

        repository.saveGroceryItems(items)

        assertEquals(items, repository.observeGroceryItems().first())
        assertEquals(
            items,
            NutritionStorageCodec.decodeGrocery(settings.getStringOrNull("nutrition_grocery_$weekKey")),
        )
    }

    @Test
    fun saveMealPlan_persistsAndEmits() = runTest {
        val settings = MapSettings()
        val repository = NutritionRepositoryImpl(settings, weekKey)
        val plan = WeeklyMealPlan(
            weekKey = weekKey,
            days = List(WeeklyMealPlan.DAYS_IN_WEEK) { DayMeals(lunch = "Salad", dinner = "Soup") },
        )

        repository.saveMealPlan(plan)

        assertEquals(plan, repository.observeMealPlan().first())
    }

    @Test
    fun loadsPersistedGroceryOnConstruction() = runTest {
        val settings = MapSettings()
        val stored = listOf(GroceryItem(id = "1", label = "Eggs", isChecked = false))
        settings.putString("nutrition_grocery_$weekKey", NutritionStorageCodec.encodeGrocery(stored))

        val repository = NutritionRepositoryImpl(settings, weekKey)

        assertEquals(stored, repository.observeGroceryItems().first())
    }

    @Test
    fun differentWeekKeys_useSeparateStorageKeys() = runTest {
        val settings = MapSettings()
        val weekA = "2026-05-11"
        val weekB = "2026-05-18"
        val repoA = NutritionRepositoryImpl(settings, weekA)
        val repoB = NutritionRepositoryImpl(settings, weekB)

        repoA.saveGroceryItems(listOf(GroceryItem("1", "Week A", false)))
        repoB.saveGroceryItems(listOf(GroceryItem("2", "Week B", false)))

        assertEquals("Week A", repoA.observeGroceryItems().first().single().label)
        assertEquals("Week B", repoB.observeGroceryItems().first().single().label)
    }

    @Test
    fun saveAiGroceryItems_persistsSeparatelyFromUserGrocery() = runTest {
        val settings = MapSettings()
        val repository = NutritionRepositoryImpl(settings, weekKey)
        val userItems = listOf(GroceryItem("u1", "Bread", false))
        val aiItems = listOf(GroceryItem("a1", "Spinach", false))

        repository.saveGroceryItems(userItems)
        repository.saveAiGroceryItems(aiItems)

        assertEquals(userItems, repository.observeGroceryItems().first())
        assertEquals(aiItems, repository.observeAiGroceryItems().first())
    }

    @Test
    fun loadsPersistedMealPlanOnConstruction() = runTest {
        val settings = MapSettings()
        val stored = WeeklyMealPlan(
            weekKey = weekKey,
            days = listOf(DayMeals(lunch = "Pasta", dinner = "Fish")) +
                List(WeeklyMealPlan.DAYS_IN_WEEK - 1) { DayMeals() },
        )
        settings.putString("nutrition_meal_plan_$weekKey", NutritionStorageCodec.encodeMealPlan(stored))

        val repository = NutritionRepositoryImpl(settings, weekKey)

        assertEquals(stored, repository.observeMealPlan().first())
    }
}
