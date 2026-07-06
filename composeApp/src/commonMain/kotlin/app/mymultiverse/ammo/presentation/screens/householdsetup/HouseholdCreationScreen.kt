package app.mymultiverse.ammo.presentation.screens.householdsetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.components.GlobalLanguageAction
import app.mymultiverse.ammo.presentation.components.JourneyPrimaryButton
import app.mymultiverse.ammo.presentation.components.JourneyTextField
import app.mymultiverse.ammo.presentation.components.JourneyTextFieldDefaults
import app.mymultiverse.ammo.presentation.components.ScreenLayout
import app.mymultiverse.ammo.presentation.components.AmmoRoundLogo
import app.mymultiverse.ammo.presentation.components.keyboardAwareScroll
import app.mymultiverse.ammo.presentation.components.rememberFieldScrollIntoViewModifier
import app.mymultiverse.ammo.presentation.screens.home.HouseholdNameAvailability
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.household_gate_create_button
import ammo.composeapp.generated.resources.household_gate_error_generic
import ammo.composeapp.generated.resources.household_gate_name_label
import ammo.composeapp.generated.resources.household_gate_subtitle
import ammo.composeapp.generated.resources.household_gate_title
import ammo.composeapp.generated.resources.household_name_available
import ammo.composeapp.generated.resources.household_name_checking
import ammo.composeapp.generated.resources.household_name_invalid
import ammo.composeapp.generated.resources.household_name_taken
import ammo.composeapp.generated.resources.household_setup_default_name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object HouseholdCreationTestTags {
    const val SCREEN = "household_creation_screen"
    const val NAME_FIELD = "household_creation_name_field"
    const val CREATE_BUTTON = "household_creation_create_button"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdCreationScreen(
    onHouseholdCreated: (householdId: String) -> Unit,
    screenModel: HouseholdSetupScreenModel = koinInject(),
) {
    val uiState by screenModel.uiState.collectAsState()
    val createdHouseholdId by screenModel.createdHouseholdId.collectAsState()
    val focusRequester = remember { FocusRequester() }

    val defaultHouseholdName = screenModel.suggestedNamePart?.let { namePart ->
        stringResource(Res.string.household_setup_default_name, namePart)
    }

    LaunchedEffect(defaultHouseholdName) {
        defaultHouseholdName?.let(screenModel::applyDefaultHouseholdNameIfEmpty)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(createdHouseholdId) {
        createdHouseholdId?.let { householdId ->
            screenModel.consumeCreatedHouseholdId()
            onHouseholdCreated(householdId)
        }
    }

    val nameAvailabilityText = householdNameAvailabilityText(uiState.nameAvailability)
    val gateErrorText = if (uiState.gateError != null) {
        stringResource(Res.string.household_gate_error_generic)
    } else {
        null
    }
    val scrollState = rememberScrollState()
    val nameScrollIntoView = rememberFieldScrollIntoViewModifier()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = { GlobalLanguageAction() },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ScreenLayout.horizontalPadding)
                .keyboardAwareScroll(scrollState)
                .testTag(HouseholdCreationTestTags.SCREEN),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(ScreenLayout.contentTopPadding))
            AmmoRoundLogo(modifier = Modifier.size(88.dp))
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.household_gate_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = JourneySemanticColors.inkDeep(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.household_gate_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = JourneySemanticColors.inkSecondary(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))

            JourneyTextField(
                value = uiState.householdNameInput,
                onValueChange = screenModel::onHouseholdNameChange,
                label = { Text(stringResource(Res.string.household_gate_name_label)) },
                enabled = !uiState.isCreating,
                isError = uiState.nameAvailability == HouseholdNameAvailability.Taken ||
                    uiState.nameAvailability == HouseholdNameAvailability.Invalid,
                supportingText = nameAvailabilityText?.let { text ->
                    {
                        Text(
                            text = text,
                            color = when (uiState.nameAvailability) {
                                HouseholdNameAvailability.Available -> SharedJourneyColors.MediterraneanTeal
                                HouseholdNameAvailability.Checking -> JourneySemanticColors.inkMuted()
                                else -> SharedJourneyColors.TerracottaOrange
                            },
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { screenModel.createHousehold() },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .then(nameScrollIntoView)
                    .testTag(HouseholdCreationTestTags.NAME_FIELD),
            )

            if (gateErrorText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = gateErrorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(JourneyTextFieldDefaults.fieldSpacing))
            JourneyPrimaryButton(
                onClick = screenModel::createHousehold,
                enabled = uiState.canCreate,
                isLoading = uiState.isCreating,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HouseholdCreationTestTags.CREATE_BUTTON),
            ) {
                Text(stringResource(Res.string.household_gate_create_button))
            }
            Spacer(modifier = Modifier.height(ScreenLayout.contentBottomPadding))
        }
    }
}

@Composable
private fun householdNameAvailabilityText(
    availability: HouseholdNameAvailability,
): String? =
    when (availability) {
        HouseholdNameAvailability.Checking -> stringResource(Res.string.household_name_checking)
        HouseholdNameAvailability.Available -> stringResource(Res.string.household_name_available)
        HouseholdNameAvailability.Taken -> stringResource(Res.string.household_name_taken)
        HouseholdNameAvailability.Invalid -> stringResource(Res.string.household_name_invalid)
        HouseholdNameAvailability.Unknown -> null
    }
