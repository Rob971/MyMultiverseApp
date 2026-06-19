package app.mymultiverse.kmp.presentation.screens.home

data class HomeOnboardingUiState(
    val householdNameInput: String = "",
    val isCreating: Boolean = false,
    val nameAvailability: HouseholdNameAvailability = HouseholdNameAvailability.Unknown,
)
