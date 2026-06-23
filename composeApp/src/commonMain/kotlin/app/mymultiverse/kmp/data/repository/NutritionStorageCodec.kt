package app.mymultiverse.kmp.data.repository

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan

object NutritionStorageCodec {
    private const val RECORD = '\u001E'
    private const val FIELD = '\u001F'

    fun encodeGrocery(items: List<GroceryItem>): String =
        items.joinToString(RECORD.toString()) { item ->
            listOf(
                item.id,
                item.label.replace(FIELD.toString(), " "),
                item.isChecked.toString(),
                item.isPantryCheck.toString(),
            ).joinToString(FIELD.toString())
        }

    fun decodeGrocery(raw: String?): List<GroceryItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(RECORD).mapNotNull { record ->
            val parts = record.split(FIELD)
            if (parts.size < 3) return@mapNotNull null
            GroceryItem(
                id = parts[0],
                label = parts[1],
                isChecked = parts[2].toBooleanStrictOrNull() ?: false,
                isPantryCheck = parts.getOrNull(3)?.toBooleanStrictOrNull() ?: false,
            )
        }
    }

    fun encodeMealPlan(plan: WeeklyMealPlan): String =
        plan.days.joinToString(RECORD.toString()) { day ->
            listOf(
                day.lunch.replace(FIELD.toString(), " "),
                day.dinner.replace(FIELD.toString(), " "),
            ).joinToString(FIELD.toString())
        }

    fun decodeMealPlan(weekKey: String, raw: String?): WeeklyMealPlan {
        if (raw.isNullOrBlank()) return WeeklyMealPlan(weekKey = weekKey)
        val days = raw.split(RECORD).map { record ->
            val parts = record.split(FIELD)
            DayMeals(
                lunch = parts.getOrElse(0) { "" },
                dinner = parts.getOrElse(1) { "" },
            )
        }
        val normalized = List(WeeklyMealPlan.DAYS_IN_WEEK) { index ->
            days.getOrElse(index) { DayMeals() }
        }
        return WeeklyMealPlan(weekKey = weekKey, days = normalized)
    }
}
