package app.mymultiverse.ammo.data.invite

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object HouseholdPushEvents {
    private val _memberJoinedHouseholdIds = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val memberJoinedHouseholdIds: SharedFlow<String> = _memberJoinedHouseholdIds.asSharedFlow()

    private var pendingMemberJoinedHouseholdId: String? = null

    fun emitMemberJoined(householdId: String) {
        val trimmed = householdId.trim()
        if (trimmed.isEmpty()) return
        pendingMemberJoinedHouseholdId = trimmed
        _memberJoinedHouseholdIds.tryEmit(trimmed)
    }

    fun consumePendingMemberJoinedHouseholdId(): String? =
        pendingMemberJoinedHouseholdId.also { pendingMemberJoinedHouseholdId = null }
}
