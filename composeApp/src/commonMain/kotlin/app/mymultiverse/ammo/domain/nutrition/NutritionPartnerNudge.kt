package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind

object NutritionPartnerNudge {
    fun canShow(
        members: List<HouseholdMember>,
        canWrite: Boolean,
        weekOffset: Int,
    ): Boolean = canWrite &&
        weekOffset == 0 &&
        members
            .filter { it.kind == HouseholdMemberKind.Person }
            .distinctBy { it.referenceId }
            .size >= 2
}
