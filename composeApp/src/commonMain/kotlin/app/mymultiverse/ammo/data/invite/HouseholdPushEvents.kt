package app.mymultiverse.ammo.data.invite

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object HouseholdPushEvents {
    private val _memberJoinedHouseholdIds = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val memberJoinedHouseholdIds: SharedFlow<String> = _memberJoinedHouseholdIds.asSharedFlow()

    private val _groceryListNudges = MutableSharedFlow<GroceryListNudgePush>(extraBufferCapacity = 1)
    val groceryListNudges: SharedFlow<GroceryListNudgePush> = _groceryListNudges.asSharedFlow()

    private val _groceryItemAdded = MutableSharedFlow<GroceryItemAddedPush>(extraBufferCapacity = 1)
    val groceryItemAdded: SharedFlow<GroceryItemAddedPush> = _groceryItemAdded.asSharedFlow()

    private val _mealPlanItemAdded = MutableSharedFlow<MealPlanItemAddedPush>(extraBufferCapacity = 1)
    val mealPlanItemAdded: SharedFlow<MealPlanItemAddedPush> = _mealPlanItemAdded.asSharedFlow()

    private var pendingMemberJoinedHouseholdId: String? = null
    private var pendingGroceryListNudge: GroceryListNudgePush? = null
    private var pendingGroceryItemAdded: GroceryItemAddedPush? = null
    private var pendingMealPlanItemAdded: MealPlanItemAddedPush? = null

    fun emitMemberJoined(householdId: String) {
        val trimmed = householdId.trim()
        if (trimmed.isEmpty()) return
        pendingMemberJoinedHouseholdId = trimmed
        _memberJoinedHouseholdIds.tryEmit(trimmed)
    }

    fun emitGroceryListNudge(nudge: GroceryListNudgePush) {
        if (nudge.householdId.isBlank()) return
        pendingGroceryListNudge = nudge
        _groceryListNudges.tryEmit(nudge)
    }

    fun emitGroceryItemAdded(push: GroceryItemAddedPush) {
        if (push.householdId.isBlank()) return
        pendingGroceryItemAdded = push
        _groceryItemAdded.tryEmit(push)
    }

    fun emitMealPlanItemAdded(push: MealPlanItemAddedPush) {
        if (push.householdId.isBlank()) return
        pendingMealPlanItemAdded = push
        _mealPlanItemAdded.tryEmit(push)
    }

    fun consumePendingMemberJoinedHouseholdId(): String? =
        pendingMemberJoinedHouseholdId.also { pendingMemberJoinedHouseholdId = null }

    fun consumePendingGroceryListNudge(): GroceryListNudgePush? =
        pendingGroceryListNudge.also { pendingGroceryListNudge = null }

    fun consumePendingGroceryItemAdded(): GroceryItemAddedPush? =
        pendingGroceryItemAdded.also { pendingGroceryItemAdded = null }

    fun consumePendingMealPlanItemAdded(): MealPlanItemAddedPush? =
        pendingMealPlanItemAdded.also { pendingMealPlanItemAdded = null }
}
