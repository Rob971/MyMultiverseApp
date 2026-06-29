package app.mymultiverse.ammo.domain.sharing

import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvite
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollaborationInviteRulesTest {

    @Test
    fun activeInvites_filtersExpiredRows() {
        val invites = listOf(
            HouseholdInvite(
                id = "active",
                householdId = "household-1",
                householdName = "Home",
                email = "a@example.com",
                role = HouseholdMemberRole.Editor,
                expiresAtEpochMillis = 9_999_999_999_999,
            ),
            HouseholdInvite(
                id = "expired",
                householdId = "household-1",
                householdName = "Home",
                email = "b@example.com",
                role = HouseholdMemberRole.Viewer,
                expiresAtEpochMillis = 1,
            ),
        )

        val active = invites.activeInvites(nowEpochMillis = 1_000)

        assertEquals(1, active.size)
        assertEquals("active", active.single().id)
    }

    @Test
    fun emailsMatch_isCaseInsensitive() {
        assertTrue(emailsMatch("Friend@Example.com", "friend@example.com"))
    }
}
