package app.mymultiverse.ammo.presentation.navigation

import app.mymultiverse.ammo.domain.model.sharing.Household
import app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.ammo.domain.sharing.DefaultNutritionSharingFeatures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HouseholdNavigationTest {

    @Test
    fun toNavigationContext_mapsIdentityFieldsAndPreservesConfiguredFeatures() {
        val household = Household(
            id = "household-1",
            name = "Our household",
            ownerId = "owner-1",
            ownerDisplayName = "Owner",
            nutritionFeatures = setOf(NutritionSharingFeature.Grocery),
        )

        val context = household.toNavigationContext()

        assertEquals("household-1", context.id)
        assertEquals("Our household", context.name)
        assertEquals("owner-1", context.ownerId)
        assertEquals("Owner", context.ownerDisplayName)
        assertEquals(setOf(NutritionSharingFeature.Grocery), context.nutritionFeatures)
        assertTrue(context.includesNutritionFeature(NutritionSharingFeature.Grocery))
        assertFalse(context.includesNutritionFeature(NutritionSharingFeature.MealPlan))
    }

    @Test
    fun toNavigationContext_appliesDefaultNutritionFeaturesWhenHouseholdHasNone() {
        val household = Household(
            id = "household-legacy",
            name = "Legacy household",
            ownerId = "owner-1",
            ownerDisplayName = null,
            nutritionFeatures = emptySet(),
        )

        assertEquals(DefaultNutritionSharingFeatures, household.toNavigationContext().nutritionFeatures)
    }
}
