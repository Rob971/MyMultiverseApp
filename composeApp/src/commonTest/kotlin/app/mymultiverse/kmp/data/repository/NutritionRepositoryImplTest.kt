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
