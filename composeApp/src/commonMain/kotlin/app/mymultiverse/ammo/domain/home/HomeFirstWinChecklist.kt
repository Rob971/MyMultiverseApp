package app.mymultiverse.ammo.domain.home

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind

object HomeFirstWinChecklist {

    fun inviteStepComplete(
        members: List<HouseholdMember>,
        outboundInviteCount: Int,
    ): Boolean {
        val personCount = members
            .filter { it.kind == HouseholdMemberKind.Person }
            .distinctBy { it.referenceId }
            .size
        return personCount >= 2 || outboundInviteCount > 0
    }

    fun nutritionStepComplete(
        plannedMealSlots: Int,
        groceryItemCount: Int,
    ): Boolean = plannedMealSlots > 0 || groceryItemCount > 0

    fun shouldShow(
        hasActiveHousehold: Boolean,
        dismissed: Boolean,
        inviteComplete: Boolean,
        nutritionComplete: Boolean,
    ): Boolean = hasActiveHousehold &&
        !dismissed &&
        !(inviteComplete && nutritionComplete)
}
