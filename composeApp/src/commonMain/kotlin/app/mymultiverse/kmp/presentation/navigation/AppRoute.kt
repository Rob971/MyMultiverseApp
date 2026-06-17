package app.mymultiverse.kmp.presentation.navigation

sealed class NutritionSection {
    data object Hub : NutritionSection()
    data object Grocery : NutritionSection()
    data object MealPlan : NutritionSection()
    data object AiAdvice : NutritionSection()
}

sealed class AppRoute {
    data object Home : AppRoute()

    data class HouseholdMembers(
        val household: HouseholdContext? = null,
    ) : AppRoute()

    data class Nutrition(
        val household: HouseholdContext? = null,
        val section: NutritionSection = NutritionSection.Hub,
    ) : AppRoute()
}
