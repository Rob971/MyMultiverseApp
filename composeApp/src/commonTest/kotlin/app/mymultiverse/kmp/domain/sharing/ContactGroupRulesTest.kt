package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContactGroupRulesTest {

    @Test
    fun persistentGroup_isAlwaysActive() {
        val group = ContactGroup(
            id = "g1",
            name = "Family",
            lifecycle = GroupLifecycle.Persistent,
            ownerId = "u1",
        )

        assertTrue(group.isActive(nowEpochMillis = 0))
    }

    @Test
    fun eventGroup_isInactiveAfterExpiry() {
        val group = ContactGroup(
            id = "g2",
            name = "Ski trip",
            lifecycle = GroupLifecycle.Event,
            ownerId = "u1",
            expiresAtEpochMillis = 1_000L,
        )

        assertTrue(group.isActive(nowEpochMillis = 500))
        assertFalse(group.isActive(nowEpochMillis = 1_500))
    }

    @Test
    fun activeOnly_filtersExpiredEventGroups() {
        val groups = listOf(
            ContactGroup("a", "Trip", GroupLifecycle.Event, "u1", expiresAtEpochMillis = 100),
            ContactGroup("b", "Family", GroupLifecycle.Persistent, "u1"),
        )

        val active = groups.activeOnly(nowEpochMillis = 200)

        assertEquals(1, active.size)
        assertEquals("b", active.single().id)
    }
}
