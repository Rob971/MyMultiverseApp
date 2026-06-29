package app.mymultiverse.ammo.presentation.screens.invite

internal fun JoinHouseholdError.affectsEmailField(): Boolean = this == JoinHouseholdError.InvalidEmail

internal fun JoinHouseholdError.affectsOtpField(): Boolean = when (this) {
    JoinHouseholdError.OtpInvalid,
    JoinHouseholdError.OtpExpired,
    JoinHouseholdError.OtpRateLimited,
    -> true
    else -> false
}

internal fun JoinHouseholdError.isScreenLevelOnly(): Boolean = when (this) {
    JoinHouseholdError.Generic,
    JoinHouseholdError.ConfigMissing,
    JoinHouseholdError.ProviderComingSoon,
    -> true
    else -> false
}
