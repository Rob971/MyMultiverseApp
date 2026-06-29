package app.mymultiverse.ammo.domain.nutrition

sealed class MealPlanGenerationScope {
    data object FullWeek : MealPlanGenerationScope()
    data class SingleDay(val dayIndex: Int) : MealPlanGenerationScope()
}

enum class NutritionAiMode {
    Advice,
    GroceryList,
    MealPlan,
}
