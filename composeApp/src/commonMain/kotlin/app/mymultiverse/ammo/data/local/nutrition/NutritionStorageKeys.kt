package app.mymultiverse.ammo.data.local.nutrition

internal object NutritionStorageKeys {
    fun grocery(householdId: String?, weekKey: String): String =
        scoped("grocery", householdId, weekKey)

    fun aiGrocery(householdId: String?, weekKey: String): String =
        scoped("ai_grocery", householdId, weekKey)

    fun mealPlan(householdId: String?, weekKey: String): String =
        scoped("meal_plan", householdId, weekKey)

    fun groceryBase(householdId: String?, weekKey: String): String =
        scoped("grocery_base", householdId, weekKey)

    fun aiGroceryBase(householdId: String?, weekKey: String): String =
        scoped("ai_grocery_base", householdId, weekKey)

    const val SYNC_OUTBOX: String = "nutrition_sync_outbox"

    private fun scoped(kind: String, householdId: String?, weekKey: String): String =
        if (householdId == null) {
            "nutrition_${kind}_$weekKey"
        } else {
            "nutrition_${householdId}_${kind}_$weekKey"
        }
}
