package app.mymultiverse.ammo.data.invite

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object HouseholdPushEvents {
    private val _memberJoinedHouseholdIds = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val memberJoinedHouseholdIds: SharedFlow<String> = _memberJoinedHouseholdIds.asSharedFlow()

    private val _groceryListNudges = MutableSharedFlow<GroceryListNudgePush>(extraBufferCapacity = 1)
    val groceryListNudges: SharedFlow<GroceryListNudgePush> = _groceryListNudges.asSharedFlow()

    private var pendingMemberJoinedHouseholdId: String? = null
    private var pendingGroceryListNudge: GroceryListNudgePush? = null

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

    fun consumePendingMemberJoinedHouseholdId(): String? =
        pendingMemberJoinedHouseholdId.also { pendingMemberJoinedHouseholdId = null }

    fun consumePendingGroceryListNudge(): GroceryListNudgePush? =
        pendingGroceryListNudge.also { pendingGroceryListNudge = null }
}
