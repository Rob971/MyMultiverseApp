package app.mymultiverse.kmp.domain.model.sharing

data class HouseholdMembership(
    val household: Household,
    val role: SpaceMemberRole,
)

sealed interface HouseholdMembershipStatus {
    data object Loading : HouseholdMembershipStatus

    data object None : HouseholdMembershipStatus

    data class Active(
        val membership: HouseholdMembership,
    ) : HouseholdMembershipStatus {
        val household: Household get() = membership.household
        val role: SpaceMemberRole get() = membership.role
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
