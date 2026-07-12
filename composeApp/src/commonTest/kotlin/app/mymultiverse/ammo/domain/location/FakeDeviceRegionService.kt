package app.mymultiverse.ammo.domain.location

class FakeDeviceRegionService(
    private val regionToReturn: DeviceRegion? = null,
    private val throwOnCall: Exception? = null,
) : DeviceRegionService {
    var callCount = 0

    override suspend fun getRegion(): DeviceRegion? {
        callCount++
        throwOnCall?.let { throw it }
        return regionToReturn
    }
}
