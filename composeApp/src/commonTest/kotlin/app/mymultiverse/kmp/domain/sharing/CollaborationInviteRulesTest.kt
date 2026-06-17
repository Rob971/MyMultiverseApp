package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollaborationInviteRulesTest {

    @Test
    fun activeInvites_filtersExpiredRows() {
        val invites = listOf(
            SpaceInvite(
                id = "active",
                spaceId = "space-1",
                spaceName = "Home",
                email = "a@example.com",
                role = SpaceMemberRole.Editor,
                expiresAtEpochMillis = 9_999_999_999_999,
            ),
            SpaceInvite(
                id = "expired",
                spaceId = "space-1",
                spaceName = "Home",
                email = "b@example.com",
                role = SpaceMemberRole.Viewer,
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
