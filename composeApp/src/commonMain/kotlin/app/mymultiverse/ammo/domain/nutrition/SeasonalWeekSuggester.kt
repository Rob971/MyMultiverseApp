package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan

/** Suggests a week of lunches and dinners from foods in season where and when the user is. */
interface SeasonalWeekSuggester {
    /** 7 lunches and 7 dinners, returned in the same shape as a generated meal plan preview. */
    suspend fun suggestWeek(currentPlan: WeeklyMealPlan): Result<NutritionAiPlanner.MealPlanGeneration>

    /** Loads this month's in-season catalog ahead of time; a no-op while the cached one is current. */
    suspend fun prefetchCatalog() = Unit
}
