package app.mymultiverse.kmp.presentation.navigation

import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode

sealed class NutritionSection {
    data object Hub : NutritionSection()
    data object Grocery : NutritionSection()
    data object MealPlan : NutritionSection()
    data object AiAdvice : NutritionSection()
}

sealed class AppRoute {
    /** SSO sign-in gate (unauthenticated). */
    data object Onboarding : AppRoute()

    /** First-time household name capture after SSO when the user has no family yet. */
    data object HouseholdSetup : AppRoute()

    /** Main app shell once the user belongs to a household (or joined via invite). */
    data class Dashboard(
        val householdId: String? = null,
    ) : AppRoute()

    data object Home : AppRoute()

    data class HouseholdMembers(
        val household: HouseholdContext? = null,
    ) : AppRoute()

    data class Nutrition(
        val household: HouseholdContext? = null,
        val section: NutritionSection = NutritionSection.Hub,
        val initialAiMode: NutritionAiMode? = null,
    ) : AppRoute()
}
