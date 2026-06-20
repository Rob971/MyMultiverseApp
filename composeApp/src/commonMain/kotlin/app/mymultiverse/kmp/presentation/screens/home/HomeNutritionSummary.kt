package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.nutrition.NutritionHubSummary

data class HomeNutritionSummary(
    val weekKey: String,
    val groceryProgress: NutritionHubSummary.GroceryProgress?,
    val plannedMealSlots: Int,
)
