package app.mymultiverse.ammo.domain.model.sharing

data class HouseholdMembership(
    val household: Household,
    val role: HouseholdMemberRole,
)

sealed interface HouseholdMembershipStatus {
    data object Loading : HouseholdMembershipStatus

    data object None : HouseholdMembershipStatus

    data class Active(
        val membership: HouseholdMembership,
    ) : HouseholdMembershipStatus {
        val household: Household get() = membership.household
        val role: HouseholdMemberRole get() = membership.role
    }

    data class Error(
        val cause: HouseholdGateError,
    ) : HouseholdMembershipStatus
}

sealed interface HouseholdGateError {
    data object Generic : HouseholdGateError
    data object NotConfigured : HouseholdGateError
    data object AlreadyActive : HouseholdGateError
    data object HouseholdRequired : HouseholdGateError
}
