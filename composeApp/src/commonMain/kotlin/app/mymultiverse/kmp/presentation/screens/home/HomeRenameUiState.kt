package app.mymultiverse.kmp.presentation.screens.home

data class HomeRenameUiState(
    val isVisible: Boolean = false,
    val nameInput: String = "",
    val isSaving: Boolean = false,
    val nameAvailability: HouseholdNameAvailability = HouseholdNameAvailability.Unknown,
)
