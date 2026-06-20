package app.mymultiverse.kmp.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class AppVersionFormatterTest {

    @Test
    fun stableVersionUsesNameOnly() {
        assertEquals("1.0.0", AppVersionFormatter.formatVersionName("1.0.0"))
        assertEquals("1.0.11", AppVersionFormatter.formatVersionName("1.0.11", prerelease = null))
        assertEquals("1.0.11", AppVersionFormatter.formatVersionName("1.0.11", prerelease = ""))
    }

    @Test
    fun prereleaseAppendsSemverSuffix() {
        assertEquals("1.1.0-beta.1", AppVersionFormatter.formatVersionName("1.1.0", prerelease = "beta.1"))
        assertEquals("1.1.0-rc.2", AppVersionFormatter.formatVersionName("1.1.0", prerelease = "rc.2"))
    }
}
