package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroceryPartnerNudgeTest {
    @Test
    fun canShow_whenTwoPeopleAndCurrentWeek() {
        val members = listOf(
            person("1"),
            person("2"),
        )
        assertTrue(
            GroceryPartnerNudge.canShow(
                members = members,
                canWrite = true,
                weekOffset = 0,
            ),
        )
    }

    @Test
    fun canShow_falseWhenSoloHousehold() {
        assertFalse(
            GroceryPartnerNudge.canShow(
                members = listOf(person("1")),
                canWrite = true,
                weekOffset = 0,
            ),
        )
    }

    @Test
    fun canShow_falseWhenViewerOrFutureWeek() {
        val members = listOf(person("1"), person("2"))
        assertFalse(
            GroceryPartnerNudge.canShow(
                members = members,
                canWrite = false,
                weekOffset = 0,
            ),
        )
        assertFalse(
            GroceryPartnerNudge.canShow(
                members = members,
                canWrite = true,
                weekOffset = 1,
            ),
        )
    }

    private fun person(id: String) = HouseholdMember(
        id = id,
        householdId = "household-1",
        kind = HouseholdMemberKind.Person,
        displayName = "Member $id",
        role = HouseholdMemberRole.Editor,
        referenceId = id,
    )
}
