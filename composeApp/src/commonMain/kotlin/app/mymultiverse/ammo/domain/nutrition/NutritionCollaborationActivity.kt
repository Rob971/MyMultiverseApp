package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem

enum class NutritionCollaborationActivityKind {
    GroceryAdded,
    GroceryChecked,
}

data class NutritionCollaborationActivity(
    val actorUserId: String?,
    val kind: NutritionCollaborationActivityKind,
    val itemLabel: String,
)

object NutritionCollaborationActivityDetector {
    fun detectGroceryChanges(
        before: List<GroceryItem>,
        after: List<GroceryItem>,
        actorUserId: String?,
    ): List<NutritionCollaborationActivity> {
        val beforeById = before.associateBy { it.id }
        val activities = mutableListOf<NutritionCollaborationActivity>()

        for (item in after) {
            if (item.id !in beforeById) {
                activities += NutritionCollaborationActivity(
                    actorUserId = actorUserId,
                    kind = NutritionCollaborationActivityKind.GroceryAdded,
                    itemLabel = item.label,
                )
            }
        }

        for (item in after) {
            val previous = beforeById[item.id] ?: continue
            if (!previous.isChecked && item.isChecked) {
                activities += NutritionCollaborationActivity(
                    actorUserId = actorUserId,
                    kind = NutritionCollaborationActivityKind.GroceryChecked,
                    itemLabel = item.label,
                )
            }
        }

        return activities
    }
}
