package app.mymultiverse.ammo.data.invite

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InvitePushPayloadTest {

    @Test
    fun inviteTokenFromData_readsHouseholdInvitePayload() {
        val token = InvitePushPayload.inviteTokenFromData(
            mapOf(
                InvitePushPayload.KEY_TYPE to InvitePushPayload.TYPE_HOUSEHOLD_INVITE,
                InvitePushPayload.KEY_INVITE_TOKEN to "token-abc",
            ),
        )

        assertEquals("token-abc", token)
    }

    @Test
    fun inviteTokenFromData_ignoresOtherNotificationTypes() {
        assertNull(
            InvitePushPayload.inviteTokenFromData(
                mapOf(
                    InvitePushPayload.KEY_TYPE to "nutrition_sync",
                    InvitePushPayload.KEY_INVITE_TOKEN to "token-abc",
                ),
            ),
        )
    }

    @Test
    fun inviteRedirectUrlFromData_buildsInviteDeepLink() {
        val url = InvitePushPayload.inviteRedirectUrlFromData(
            mapOf(
                InvitePushPayload.KEY_TYPE to InvitePushPayload.TYPE_HOUSEHOLD_INVITE,
                InvitePushPayload.KEY_INVITE_TOKEN to "token-abc",
            ),
        )

        assertEquals(InviteRedirectUrls.build("token-abc"), url)
    }

    @Test
    fun memberJoinedHouseholdIdFromData_readsMemberJoinedPayload() {
        val householdId = InvitePushPayload.memberJoinedHouseholdIdFromData(
            mapOf(
                InvitePushPayload.KEY_TYPE to InvitePushPayload.TYPE_MEMBER_JOINED,
                InvitePushPayload.KEY_HOUSEHOLD_ID to "household-1",
            ),
        )

        assertEquals("household-1", householdId)
    }

    @Test
    fun groceryListNudgeFromData_readsGroceryNudgePayload() {
        val nudge = InvitePushPayload.groceryListNudgeFromData(
            mapOf(
                InvitePushPayload.KEY_TYPE to InvitePushPayload.TYPE_GROCERY_LIST_NUDGE,
                InvitePushPayload.KEY_HOUSEHOLD_ID to "household-1",
                InvitePushPayload.KEY_WEEK_KEY to "2026-06-30",
                InvitePushPayload.KEY_NUDGER_NAME to "Alex",
            ),
        )

        assertEquals("household-1", nudge?.householdId)
        assertEquals("2026-06-30", nudge?.weekKey)
        assertEquals("Alex", nudge?.nudgerName)
    }
}
