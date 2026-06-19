package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.model.sharing.HouseholdGateError
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus

sealed interface HomePhase {
    data object Loading : HomePhase

    data object Onboarding : HomePhase

    data object Welcome : HomePhase

    data class Error(val cause: HouseholdGateError) : HomePhase
}

fun HouseholdMembershipStatus.toHomePhase(): HomePhase =
    when (this) {
        HouseholdMembershipStatus.Loading -> HomePhase.Loading
        HouseholdMembershipStatus.None -> HomePhase.Onboarding
        is HouseholdMembershipStatus.Active -> HomePhase.Welcome
        is HouseholdMembershipStatus.Error -> HomePhase.Error(cause)
    }
