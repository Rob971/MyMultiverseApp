package app.mymultiverse.kmp.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class AppVersionFormatterTest {

    @Test
    fun stableVersionUsesLtsString() {
        assertEquals("1.0.0", AppVersionFormatter.formatVersionName("1.0.0", candidate = 0))
        assertEquals("1.0.1", AppVersionFormatter.formatVersionName("1.0.1", candidate = 0))
    }

    @Test
    fun releaseCandidateUsesThirdSegmentCounter() {
        assertEquals("1.0.1", AppVersionFormatter.formatVersionName("1.0.0", candidate = 1))
        assertEquals("1.0.5", AppVersionFormatter.formatVersionName("1.0.0", candidate = 5))
    }
}
