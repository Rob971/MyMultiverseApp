package app.mymultiverse.ammo.data.invite

data class MealPlanItemAddedPush(
    val householdId: String,
    val weekKey: String,
    val actorName: String,
    val itemLabel: String,
    val addedCount: Int,
    val dayIndex: Int,
    val mealSlot: String,
)
