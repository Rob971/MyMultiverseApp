package app.mymultiverse.kmp.presentation.screens.household

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.sharing.HouseholdGateError
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.presentation.components.JourneyBanner
import app.mymultiverse.kmp.presentation.components.LanguagePicker
import app.mymultiverse.kmp.presentation.components.PendingInvitesCard
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_sign_out
import kmpvoyagercleanarchitecture.composeapp.generated.resources.household_gate_create_button
import kmpvoyagercleanarchitecture.composeapp.generated.resources.household_gate_create_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.household_gate_error_already_active
import kmpvoyagercleanarchitecture.composeapp.generated.resources.household_gate_error_generic
import kmpvoyagercleanarchitecture.composeapp.generated.resources.household_gate_error_not_configured
import kmpvoyagercleanarchitecture.composeapp.generated.resources.household_gate_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.household_gate_name_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.household_gate_retry
import kmpvoyagercleanarchitecture.composeapp.generated.resources.household_gate_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.household_gate_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object HouseholdGateTestTags {
    const val LOADING = "household_gate_loading"
    const val ERROR = "household_gate_error"
    const val RETRY_BUTTON = "household_gate_retry"
    const val CREATE_NAME_FIELD = "household_gate_create_name"
    const val CREATE_BUTTON = "household_gate_create_button"
    const val SIGN_OUT_BUTTON = "household_gate_sign_out"
}

@Composable
fun HouseholdGateScreen(
    screenModel: HouseholdGateScreenModel = koinInject(),
) {
    val uiState by screenModel.uiState.collectAsState()

    when (val status = uiState.membershipStatus) {
        HouseholdMembershipStatus.Loading -> HouseholdGateLoading(onSignOut = screenModel::signOut)
        is HouseholdMembershipStatus.Error -> HouseholdGateErrorContent(
            error = status.cause,
            onRetry = screenModel::refreshMembership,
            onSignOut = screenModel::signOut,
        )
        HouseholdMembershipStatus.None -> HouseholdGateOnboardingContent(
            uiState = uiState,
            onNameChange = screenModel::onHouseholdNameChange,
            onCreate = screenModel::createHousehold,
            onAcceptInvite = screenModel::acceptInvite,
            onDeclineInvite = screenModel::declineInvite,
            onSignOut = screenModel::signOut,
        )
        is HouseholdMembershipStatus.Active -> Unit
    }
}

@Composable
private fun HouseholdGateLoading(onSignOut: () -> Unit) {
    Scaffold(
        topBar = { HouseholdGateTopBar(onSignOut = onSignOut) },
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(HouseholdGateTestTags.LOADING),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(color = SharedJourneyColors.MediterraneanTeal)
            Text(
                text = stringResource(Res.string.household_gate_loading),
                modifier = Modifier.padding(top = 12.dp),
                color = SharedJourneyColors.InkMuted,
            )
        }
    }
}

@Composable
private fun HouseholdGateErrorContent(
    error: HouseholdGateError,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
) {
    val message = when (error) {
        HouseholdGateError.Generic -> stringResource(Res.string.household_gate_error_generic)
        HouseholdGateError.NotConfigured -> stringResource(Res.string.household_gate_error_not_configured)
        HouseholdGateError.AlreadyActive -> stringResource(Res.string.household_gate_error_already_active)
        HouseholdGateError.HouseholdRequired -> stringResource(Res.string.household_gate_error_generic)
    }

    Scaffold(
        topBar = { HouseholdGateTopBar(onSignOut = onSignOut) },
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(HouseholdGateTestTags.ERROR),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = message,
                color = SharedJourneyColors.TerracottaOrange,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .testTag(HouseholdGateTestTags.RETRY_BUTTON),
            ) {
                Text(stringResource(Res.string.household_gate_retry))
            }
        }
    }
}

@Composable
private fun HouseholdGateOnboardingContent(
    uiState: HouseholdGateUiState,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onAcceptInvite: (String) -> Unit,
    onDeclineInvite: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    Scaffold(
        topBar = { HouseholdGateTopBar(onSignOut = onSignOut) },
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(screenListPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            JourneyBanner(
                headline = stringResource(Res.string.household_gate_title),
                supportingLine = stringResource(Res.string.household_gate_subtitle),
                description = "",
            )

            PendingInvitesCard(
                invites = uiState.pendingInvites,
                onAccept = onAcceptInvite,
                onDecline = onDeclineInvite,
            )

            Text(
                text = stringResource(Res.string.household_gate_create_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SharedJourneyColors.InkDeep,
            )

            OutlinedTextField(
                value = uiState.householdNameInput,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HouseholdGateTestTags.CREATE_NAME_FIELD),
                label = { Text(stringResource(Res.string.household_gate_name_label)) },
                singleLine = true,
                enabled = !uiState.isCreating,
            )

            Button(
                onClick = onCreate,
                enabled = uiState.householdNameInput.isNotBlank() && !uiState.isCreating,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HouseholdGateTestTags.CREATE_BUTTON),
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(stringResource(Res.string.household_gate_create_button))
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HouseholdGateTopBar(onSignOut: () -> Unit) {
    TopAppBar(
        title = { },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        actions = {
            TextButton(
                onClick = onSignOut,
                modifier = Modifier.testTag(HouseholdGateTestTags.SIGN_OUT_BUTTON),
            ) {
                Text(stringResource(Res.string.auth_sign_out))
            }
            LanguagePicker()
        },
    )
}
