package app.mymultiverse.ammo.domain.platform

import kotlin.test.Test
import kotlin.test.assertEquals

class ReleaseChannelTest {

    @Test
    fun fromVersionName_alphaVersion_returnsAlpha() {
        assertEquals(ReleaseChannel.Alpha, ReleaseChannel.fromVersionName("1.6.0-alpha.42"))
    }

    @Test
    fun fromVersionName_alphaVersionEarlyBuild_returnsAlpha() {
        assertEquals(ReleaseChannel.Alpha, ReleaseChannel.fromVersionName("1.0.0-alpha.1"))
    }

    @Test
    fun fromVersionName_betaVersion_returnsBeta() {
        assertEquals(ReleaseChannel.Beta, ReleaseChannel.fromVersionName("1.6.0-beta.5"))
    }

    @Test
    fun fromVersionName_betaVersionLargeNumber_returnsBeta() {
        assertEquals(ReleaseChannel.Beta, ReleaseChannel.fromVersionName("2.0.0-beta.100"))
    }

    @Test
    fun fromVersionName_productionVersion_returnsProduction() {
        assertEquals(ReleaseChannel.Production, ReleaseChannel.fromVersionName("1.6.0"))
    }

    @Test
    fun fromVersionName_productionMajorMinorPatch_returnsProduction() {
        assertEquals(ReleaseChannel.Production, ReleaseChannel.fromVersionName("2.3.1"))
    }

    @Test
    fun fromVersionName_rcVersion_returnsProduction() {
        // rc is not a recognised prerelease channel — treated as production.
        assertEquals(ReleaseChannel.Production, ReleaseChannel.fromVersionName("1.6.0-rc.1"))
    }

    @Test
    fun fromVersionName_emptyString_returnsProduction() {
        assertEquals(ReleaseChannel.Production, ReleaseChannel.fromVersionName(""))
    }
}
