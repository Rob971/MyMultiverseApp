package app.mymultiverse.ammo.domain.location

/**
 * Coarse geographic region detected from the device, used to suggest an app language
 * on first launch without requiring any user action.
 *
 * @param countryCode ISO 3166-1 alpha-2 country code, uppercase (e.g. "IT", "FR").
 * @param adminArea   Administrative region name in English when available — required for
 *                    sub-national language selection (e.g. Campania within Italy).
 */
data class DeviceRegion(
    val countryCode: String,
    val adminArea: String? = null,
)
