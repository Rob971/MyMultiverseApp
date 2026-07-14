package app.mymultiverse.ammo.presentation.navigation

import app.mymultiverse.ammo.domain.model.sharing.Household
import app.mymultiverse.ammo.domain.sharing.orDefaultNutritionFeatures

fun Household.toNavigationContext(): HouseholdContext =
    HouseholdContext(
        id = id,
        name = name,
        ownerId = ownerId,
        ownerDisplayName = ownerDisplayName,
        nutritionFeatures = nutritionFeatures.orDefaultNutritionFeatures(),
        avatarUrl = avatarUrl,
    )
