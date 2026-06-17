package app.mymultiverse.kmp.data.supabase.dto

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HouseholdRpcMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun decodesEnsureHouseholdRpcPayload() {
        val row = json.decodeFromString<HouseholdRpcRow>(
            """
            {
              "household_id": "11111111-1111-1111-1111-111111111111",
              "household_name": "Rossi home",
              "owner_id": "22222222-2222-2222-2222-222222222222",
              "owner_display_name": "Roberto",
              "features": ["grocery", "meal_plan", "ai_advice"]
            }
            """.trimIndent(),
        )

        assertEquals("11111111-1111-1111-1111-111111111111", row.householdId)
        assertEquals("Rossi home", row.householdName)
        assertEquals("Roberto", row.ownerDisplayName)
        assertEquals(
            setOf(
                NutritionSharingFeature.Grocery,
                NutritionSharingFeature.MealPlan,
                NutritionSharingFeature.AiAdvice,
            ),
            (row.features ?: emptyList()).mapNotNull { it.toNutritionFeature() }.toSet(),
        )
    }

    @Test
    fun decodesNullFeaturesAsEmptyList() {
        val row = json.decodeFromString<HouseholdRpcRow>(
            """
            {
              "household_id": "household-1",
              "household_name": "Home",
              "owner_id": "owner-1",
              "features": null
            }
            """.trimIndent(),
        )

        assertEquals(null, row.features)
    }

    @Test
    fun decodesEmptyFeaturesAsEmptyList() {
        val row = json.decodeFromString<HouseholdRpcRow>(
            """
            {
              "household_id": "household-1",
              "household_name": "Home",
              "owner_id": "owner-1",
              "features": []
            }
            """.trimIndent(),
        )

        assertTrue(row.features.isNullOrEmpty())
    }

    private fun String.toNutritionFeature(): NutritionSharingFeature? =
        when (this) {
            "grocery" -> NutritionSharingFeature.Grocery
            "meal_plan" -> NutritionSharingFeature.MealPlan
            "ai_advice" -> NutritionSharingFeature.AiAdvice
            else -> null
        }
}
