package app.mymultiverse.kmp.presentation.navigation

import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.sharing.orDefaultNutritionFeatures

fun Household.toNavigationContext(): HouseholdContext =
    HouseholdContext(
        id = id,
        name = name,
        ownerId = ownerId,
        ownerDisplayName = ownerDisplayName,
        nutritionFeatures = nutritionFeatures.orDefaultNutritionFeatures(),
    )
