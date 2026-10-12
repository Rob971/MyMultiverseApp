package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem

enum class NutritionCollaborationActivityKind {
    GroceryAdded,
    GroceryChecked,
    GroceryRemoved,
}

data class NutritionCollaborationActivity(
    val actorUserId: String?,
    val kind: NutritionCollaborationActivityKind,
    val itemLabel: String,
    /**
     * The item as it existed before a [NutritionCollaborationActivityKind.GroceryRemoved],
     * so a viewer can offer to re-add it with its original id, label and checked state.
     * Null for added/checked activities.
     */
    val removedItem: GroceryItem? = null,
)

object NutritionCollaborationActivityDetector {
    fun detectGroceryChanges(
        before: List<GroceryItem>,
        after: List<GroceryItem>,
        actorUserId: String?,
    ): List<NutritionCollaborationActivity> {
        val beforeById = before.associateBy { it.id }
        val afterIds = after.mapTo(mutableSetOf()) { it.id }
        val activities = mutableListOf<NutritionCollaborationActivity>()

        for (item in before) {
            if (item.id !in afterIds) {
                activities += NutritionCollaborationActivity(
                    actorUserId = actorUserId,
                    kind = NutritionCollaborationActivityKind.GroceryRemoved,
                    itemLabel = item.label,
                    removedItem = item,
                )
            }
        }

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
