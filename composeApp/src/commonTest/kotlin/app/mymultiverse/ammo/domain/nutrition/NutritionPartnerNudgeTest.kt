package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NutritionPartnerNudgeTest {
    private val soloMember = listOf(
        HouseholdMember(
            id = "m1",
            householdId = "h1",
            kind = HouseholdMemberKind.Person,
            displayName = "Alex",
            role = HouseholdMemberRole.Owner,
            referenceId = "u1",
        ),
    )

    private val twoMembers = soloMember + HouseholdMember(
        id = "m2",
        householdId = "h1",
        kind = HouseholdMemberKind.Person,
        displayName = "Partner",
        role = HouseholdMemberRole.Editor,
        referenceId = "u2",
    )

    @Test
    fun canShow_whenTwoPeopleAndCurrentWeek() {
        assertTrue(
            NutritionPartnerNudge.canShow(
                members = twoMembers,
                canWrite = true,
                weekOffset = 0,
            ),
        )
    }

    @Test
    fun canShow_falseWhenViewerOrFutureWeek() {
        assertFalse(
            NutritionPartnerNudge.canShow(
                members = twoMembers,
                canWrite = false,
                weekOffset = 0,
            ),
        )
        assertFalse(
            NutritionPartnerNudge.canShow(
                members = twoMembers,
                canWrite = true,
                weekOffset = 1,
            ),
        )
    }

    @Test
    fun canShow_falseWhenSoloHousehold() {
        assertFalse(
            NutritionPartnerNudge.canShow(
                members = soloMember,
                canWrite = true,
                weekOffset = 0,
            ),
        )
    }
}
