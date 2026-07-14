package app.mymultiverse.ammo.domain.nutrition

sealed class MealPlanGenerationScope {
    data object FullWeek : MealPlanGenerationScope()
    data class SingleDay(val dayIndex: Int) : MealPlanGenerationScope()
    /** Generates (and applies) only one meal slot for one day, leaving the other slot unchanged. */
    data class SingleMeal(val dayIndex: Int, val slot: MealSlot) : MealPlanGenerationScope()
}

enum class NutritionAiMode {
    Advice,
    GroceryList,
    MealPlan,
}
