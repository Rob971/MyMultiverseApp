package app.mymultiverse.kmp.domain.home

import app.mymultiverse.kmp.domain.model.sharing.HouseholdMember
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberKind
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeFirstWinChecklistTest {

    @Test
    fun inviteStepComplete_whenTwoPeople() {
        val members = listOf(
            member("1"),
            member("2"),
        )
        assertTrue(HomeFirstWinChecklist.inviteStepComplete(members, outboundInviteCount = 0))
    }

    @Test
    fun inviteStepComplete_whenOutboundInvitePending() {
        assertTrue(HomeFirstWinChecklist.inviteStepComplete(emptyList(), outboundInviteCount = 1))
    }

    @Test
    fun inviteStepComplete_falseWhenSoloAndNoInvites() {
        assertFalse(HomeFirstWinChecklist.inviteStepComplete(listOf(member("1")), outboundInviteCount = 0))
    }

    @Test
    fun nutritionStepComplete_whenMealsOrGroceriesExist() {
        assertTrue(HomeFirstWinChecklist.nutritionStepComplete(plannedMealSlots = 1, groceryItemCount = 0))
        assertTrue(HomeFirstWinChecklist.nutritionStepComplete(plannedMealSlots = 0, groceryItemCount = 2))
        assertFalse(HomeFirstWinChecklist.nutritionStepComplete(plannedMealSlots = 0, groceryItemCount = 0))
    }

    @Test
    fun shouldShow_hidesWhenDismissedOrComplete() {
        assertTrue(
            HomeFirstWinChecklist.shouldShow(
                hasActiveHousehold = true,
                dismissed = false,
                inviteComplete = false,
                nutritionComplete = false,
            ),
        )
        assertFalse(
            HomeFirstWinChecklist.shouldShow(
                hasActiveHousehold = true,
                dismissed = true,
                inviteComplete = false,
                nutritionComplete = false,
            ),
        )
        assertFalse(
            HomeFirstWinChecklist.shouldShow(
                hasActiveHousehold = true,
                dismissed = false,
                inviteComplete = true,
                nutritionComplete = true,
            ),
        )
    }

    private fun member(id: String) = HouseholdMember(
        id = id,
        householdId = "household-1",
        kind = HouseholdMemberKind.Person,
        displayName = "Member $id",
        role = app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole.Editor,
        referenceId = id,
    )
}
