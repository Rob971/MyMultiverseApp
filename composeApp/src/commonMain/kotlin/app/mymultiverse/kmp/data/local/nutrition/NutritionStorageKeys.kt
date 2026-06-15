package app.mymultiverse.kmp.data.local.nutrition

internal object NutritionStorageKeys {
    fun grocery(spaceId: String?, weekKey: String): String =
        scoped("grocery", spaceId, weekKey)

    fun aiGrocery(spaceId: String?, weekKey: String): String =
        scoped("ai_grocery", spaceId, weekKey)

    fun mealPlan(spaceId: String?, weekKey: String): String =
        scoped("meal_plan", spaceId, weekKey)

    const val SYNC_OUTBOX: String = "nutrition_sync_outbox"

    private fun scoped(kind: String, spaceId: String?, weekKey: String): String =
        if (spaceId == null) {
            "nutrition_${kind}_$weekKey"
        } else {
            "nutrition_${spaceId}_${kind}_$weekKey"
        }
}
