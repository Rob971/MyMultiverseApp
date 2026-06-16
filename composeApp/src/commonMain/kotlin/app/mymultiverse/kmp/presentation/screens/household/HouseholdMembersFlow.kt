package app.mymultiverse.kmp.presentation.screens.household

import androidx.compose.runtime.Composable
import app.mymultiverse.kmp.presentation.navigation.HouseholdContext

@Composable
fun HouseholdMembersFlow(
    household: HouseholdContext?,
    onBack: () -> Unit,
    onHouseholdReady: (HouseholdContext) -> Unit,
) {
    if (household == null) {
        HouseholdMembersGate(
            onBack = onBack,
            onReady = onHouseholdReady,
        )
        return
    }

    HouseholdMembersScreen(
        household = household,
        onBack = onBack,
    )
}
