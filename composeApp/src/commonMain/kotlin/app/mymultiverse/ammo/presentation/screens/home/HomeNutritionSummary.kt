package app.mymultiverse.ammo.presentation.screens.home

import app.mymultiverse.ammo.domain.home.HomeTonightDinner
import app.mymultiverse.ammo.domain.nutrition.NutritionHubSummary

data class HomeNutritionSummary(
    val weekKey: String,
    val groceryProgress: NutritionHubSummary.GroceryProgress?,
    val plannedMealSlots: Int,
    val tonightsDinner: HomeTonightDinner.State = HomeTonightDinner.State.Hidden,
)
