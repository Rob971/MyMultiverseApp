package app.mymultiverse.ammo.domain.location

class FakeDeviceRegionService(
    private val regionToReturn: DeviceRegion? = null,
    private val localeCountry: String? = null,
    private val throwOnCall: Exception? = null,
) : DeviceRegionService {
    var callCount = 0

    override fun getLocaleCountryCode(): String? =
        localeCountry ?: regionToReturn?.countryCode

    override suspend fun getRegion(): DeviceRegion? {
        callCount++
        throwOnCall?.let { throw it }
        return regionToReturn
    }
}
