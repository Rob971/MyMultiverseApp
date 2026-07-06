package app.mymultiverse.ammo.data.invite

data class GroceryItemAddedPush(
    val householdId: String,
    val weekKey: String,
    val actorName: String,
    val itemLabel: String,
    val addedCount: Int,
)
