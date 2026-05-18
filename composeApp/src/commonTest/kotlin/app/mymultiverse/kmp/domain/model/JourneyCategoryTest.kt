package app.mymultiverse.kmp.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class JourneyCategoryTest {

    @Test
    fun storageKeysRoundTripToExpectedCategories() {
        JourneyCategory.entries.forEach { category ->
            assertEquals(category, JourneyCategory.fromStorageKey(category.storageKey))
        }
    }

    @Test
    fun unknownStorageKeyFallsBackToLongTermProjects() {
        assertEquals(JourneyCategory.LongTermProjects, JourneyCategory.fromStorageKey(null))
        assertEquals(JourneyCategory.LongTermProjects, JourneyCategory.fromStorageKey("UNKNOWN"))
    }
}
