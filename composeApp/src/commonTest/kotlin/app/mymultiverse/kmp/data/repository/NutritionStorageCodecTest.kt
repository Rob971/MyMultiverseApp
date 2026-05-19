package app.mymultiverse.kmp.data.repository

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NutritionStorageCodecTest {

    @Test
    fun encodeDecodeGrocery_roundTripsItems() {
        val items = listOf(
            GroceryItem(id = "1", label = "Milk", isChecked = false),
            GroceryItem(id = "2", label = "Tomatoes", isChecked = true),
        )

        val encoded = NutritionStorageCodec.encodeGrocery(items)
        val decoded = NutritionStorageCodec.decodeGrocery(encoded)

        assertEquals(items, decoded)
    }

    @Test
    fun decodeGrocery_returnsEmptyForNullOrBlank() {
        assertEquals(emptyList(), NutritionStorageCodec.decodeGrocery(null))
        assertEquals(emptyList(), NutritionStorageCodec.decodeGrocery("   "))
    }

    @Test
    fun decodeGrocery_skipsMalformedRecords() {
        val raw = "ok\u001Fmilk\u001Ffalse\u001Ebroken\u001Eid2\u001Flabel\u001Ftrue"
        val decoded = NutritionStorageCodec.decodeGrocery(raw)

        assertEquals(2, decoded.size)
        assertEquals("ok", decoded[0].id)
        assertEquals("id2", decoded[1].id)
    }

    @Test
    fun encodeGrocery_sanitizesFieldSeparatorInLabels() {
        val items = listOf(GroceryItem(id = "1", label = "weird\u001Flabel", isChecked = false))
        val decoded = NutritionStorageCodec.decodeGrocery(NutritionStorageCodec.encodeGrocery(items))

        assertEquals("weird label", decoded.single().label)
    }

    @Test
    fun encodeDecodeMealPlan_roundTripsSevenDays() {
        val weekKey = "2026-05-19"
        val plan = WeeklyMealPlan(
            weekKey = weekKey,
            days = List(WeeklyMealPlan.DAYS_IN_WEEK) { index ->
                DayMeals(lunch = "Lunch $index", dinner = "Dinner $index")
            },
        )

        val encoded = NutritionStorageCodec.encodeMealPlan(plan)
        val decoded = NutritionStorageCodec.decodeMealPlan(weekKey, encoded)

        assertEquals(plan, decoded)
    }

    @Test
    fun decodeMealPlan_padsShortDataToSevenDays() {
        val weekKey = "2026-05-19"
        val raw = "soup\u001Fstew"

        val decoded = NutritionStorageCodec.decodeMealPlan(weekKey, raw)

        assertEquals(WeeklyMealPlan.DAYS_IN_WEEK, decoded.days.size)
        assertEquals(DayMeals(lunch = "soup", dinner = "stew"), decoded.days.first())
        assertEquals(DayMeals(), decoded.days.last())
    }

    @Test
    fun decodeMealPlan_returnsEmptyWeekWhenRawMissing() {
        val decoded = NutritionStorageCodec.decodeMealPlan("2026-05-19", null)

        assertEquals("2026-05-19", decoded.weekKey)
        assertTrue(decoded.days.all { it == DayMeals() })
    }

    @Test
    fun encodeMealPlan_sanitizesFieldSeparatorInMealText() {
        val weekKey = "2026-05-19"
        val plan = WeeklyMealPlan(
            weekKey = weekKey,
            days = listOf(DayMeals(lunch = "a\u001Fb", dinner = "c\u001Fd")) +
                List(WeeklyMealPlan.DAYS_IN_WEEK - 1) { DayMeals() },
        )

        val decoded = NutritionStorageCodec.decodeMealPlan(weekKey, NutritionStorageCodec.encodeMealPlan(plan))

        assertEquals("a b", decoded.days.first().lunch)
        assertEquals("c d", decoded.days.first().dinner)
    }
}
