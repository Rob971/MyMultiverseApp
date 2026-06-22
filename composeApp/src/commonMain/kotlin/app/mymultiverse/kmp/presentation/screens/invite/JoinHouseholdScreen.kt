package app.mymultiverse.kmp.presentation.screens.invite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import app.mymultiverse.kmp.presentation.components.JourneyTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.VesuvianHeartLogo
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_continue_apple
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_continue_google
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_email_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_error_config_missing
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_provider_coming_soon
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_apple_email_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_back_to_email
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_continue_button
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_email_helper
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_email_warning
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_error_already_accepted
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_error_declined
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_error_expired
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_error_generic
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_error_invalid_email
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_error_not_found
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_error_otp_expired
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_error_otp_invalid
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_error_otp_rate_limited
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_loading_preview
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_or_divider
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_otp_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_otp_resend
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_otp_resend_wait
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_otp_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_otp_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object JoinHouseholdTestTags {
    const val SCREEN = "join_household_screen"
    const val LOADING = "join_household_loading"
    const val ERROR = "join_household_error"
    const val EMAIL_FIELD = "join_household_email_field"
    const val OTP_FIELD = "join_household_otp_field"
    const val CONTINUE_BUTTON = "join_household_continue_button"
    const val VERIFY_BUTTON = "join_household_verify_button"
    const val GOOGLE_BUTTON = "join_household_google_button"
    const val APPLE_BUTTON = "join_household_apple_button"
    const val RESEND_BUTTON = "join_household_resend_button"
}

@Composable
fun JoinHouseholdScreen(
    inviteToken: String,
    showConfigMissing: Boolean,
    screenModel: JoinHouseholdScreenModel = koinInject(),
) {
    val uiState by screenModel.uiState.collectAsState()

    LaunchedEffect(inviteToken) {
        if (inviteToken.isNotBlank()) {
            screenModel.loadPreview(inviteToken)
        }
    }

    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent) { padding ->
        when (val previewState = uiState.previewState) {
            JoinPreviewState.Loading -> JoinHouseholdLoading(padding)
            is JoinPreviewState.Error -> JoinHouseholdPreviewError(
                padding = padding,
                error = previewState.type,
            )
            is JoinPreviewState.Ready -> JoinHouseholdContent(
                padding = padding,
                previewState = previewState,
                uiState = uiState,
                showConfigMissing = showConfigMissing,
                screenModel = screenModel,
            )
        }
    }
}

@Composable
private fun JoinHouseholdLoading(padding: androidx.compose.foundation.layout.PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .testTag(JoinHouseholdTestTags.LOADING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = SharedJourneyColors.MediterraneanTeal)
        Text(
            text = stringResource(Res.string.invite_join_loading_preview),
            modifier = Modifier.padding(top = 12.dp),
            color = SharedJourneyColors.InkMuted,
        )
    }
}

@Composable
private fun JoinHouseholdPreviewError(
    padding: androidx.compose.foundation.layout.PaddingValues,
    error: JoinPreviewError,
) {
    val message = when (error) {
        JoinPreviewError.NotFound -> stringResource(Res.string.invite_join_error_not_found)
        JoinPreviewError.Expired -> stringResource(Res.string.invite_join_error_expired)
        JoinPreviewError.Declined -> stringResource(Res.string.invite_join_error_declined)
        JoinPreviewError.AlreadyAccepted -> stringResource(Res.string.invite_join_error_already_accepted)
        JoinPreviewError.Generic -> stringResource(Res.string.invite_join_error_generic)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = ScreenLayout.horizontalPadding)
            .testTag(JoinHouseholdTestTags.ERROR),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun JoinHouseholdContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    previewState: JoinPreviewState.Ready,
    uiState: JoinHouseholdUiState,
    showConfigMissing: Boolean,
    screenModel: JoinHouseholdScreenModel,
) {
    val preview = previewState.preview
    val actionError = when {
        showConfigMissing -> stringResource(Res.string.auth_error_config_missing) to false
        uiState.message is JoinHouseholdMessage.Error -> when ((uiState.message as JoinHouseholdMessage.Error).type) {
            JoinHouseholdError.ConfigMissing -> stringResource(Res.string.auth_error_config_missing) to false
            JoinHouseholdError.ProviderComingSoon -> stringResource(Res.string.auth_provider_coming_soon) to false
            JoinHouseholdError.InvalidEmail -> stringResource(Res.string.invite_join_error_invalid_email) to false
            JoinHouseholdError.OtpInvalid -> stringResource(Res.string.invite_join_error_otp_invalid) to false
            JoinHouseholdError.OtpExpired -> stringResource(Res.string.invite_join_error_otp_expired) to false
            JoinHouseholdError.OtpRateLimited -> stringResource(Res.string.invite_join_error_otp_rate_limited) to false
            JoinHouseholdError.Generic -> stringResource(Res.string.invite_join_error_generic) to false
        }
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = ScreenLayout.horizontalPadding)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .testTag(JoinHouseholdTestTags.SCREEN),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        VesuvianHeartLogo(modifier = Modifier.height(88.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.invite_join_title, preview.householdName),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SharedJourneyColors.InkDeep,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.invite_join_subtitle, preview.inviterName),
            style = MaterialTheme.typography.bodyLarge,
            color = SharedJourneyColors.InkDeep.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (uiState.step) {
            JoinOtpStep.Email -> JoinHouseholdEmailStep(
                uiState = uiState,
                invitedEmail = preview.inviteeEmail,
                showConfigMissing = showConfigMissing,
                screenModel = screenModel,
            )
            JoinOtpStep.Code -> JoinHouseholdOtpStep(
                uiState = uiState,
                showConfigMissing = showConfigMissing,
                screenModel = screenModel,
            )
        }

        if (actionError != null) {
            val (text, _) = actionError
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun JoinHouseholdEmailStep(
    uiState: JoinHouseholdUiState,
    invitedEmail: String,
    showConfigMissing: Boolean,
    screenModel: JoinHouseholdScreenModel,
) {
    JourneyTextField(
        value = uiState.email,
        onValueChange = screenModel::onEmailChange,
        label = { Text(stringResource(Res.string.auth_email_label)) },
        supportingText = {
            Text(stringResource(Res.string.invite_join_email_helper))
        },
        enabled = !uiState.isLoading && !showConfigMissing,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = { screenModel.continueWithEmail() },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(JoinHouseholdTestTags.EMAIL_FIELD),
    )

    if (uiState.showEmailWarning) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.invite_join_email_warning, invitedEmail),
            style = MaterialTheme.typography.bodySmall,
            color = SharedJourneyColors.TerracottaOrange,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = screenModel::continueWithEmail,
        enabled = uiState.canContinueEmail && !showConfigMissing,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(JoinHouseholdTestTags.CONTINUE_BUTTON),
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
        } else {
            Text(stringResource(Res.string.invite_join_continue_button))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    JoinHouseholdSocialSignIn(
        uiState = uiState,
        showConfigMissing = showConfigMissing,
        screenModel = screenModel,
    )
}

@Composable
private fun JoinHouseholdOtpStep(
    uiState: JoinHouseholdUiState,
    showConfigMissing: Boolean,
    screenModel: JoinHouseholdScreenModel,
) {
    Text(
        text = stringResource(Res.string.invite_join_otp_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = SharedJourneyColors.InkDeep,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = stringResource(Res.string.invite_join_otp_subtitle, uiState.email),
        style = MaterialTheme.typography.bodyMedium,
        color = SharedJourneyColors.InkMuted,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(16.dp))

    JourneyTextField(
        value = uiState.otpCode,
        onValueChange = screenModel::onOtpCodeChange,
        label = { Text(stringResource(Res.string.invite_join_otp_label)) },
        enabled = !uiState.isLoading && !showConfigMissing,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = { screenModel.verifyOtp() },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(JoinHouseholdTestTags.OTP_FIELD),
    )

    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = screenModel::verifyOtp,
        enabled = uiState.canVerifyOtp && !showConfigMissing,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(JoinHouseholdTestTags.VERIFY_BUTTON),
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
        } else {
            Text(stringResource(Res.string.invite_join_continue_button))
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    TextButton(
        onClick = screenModel::resendOtp,
        enabled = uiState.canResendOtp && !showConfigMissing,
        modifier = Modifier.testTag(JoinHouseholdTestTags.RESEND_BUTTON),
    ) {
        Text(
            if (uiState.resendCooldownSeconds > 0) {
                stringResource(Res.string.invite_join_otp_resend_wait, uiState.resendCooldownSeconds)
            } else {
                stringResource(Res.string.invite_join_otp_resend)
            },
        )
    }
    TextButton(onClick = screenModel::backToEmailStep) {
        Text(stringResource(Res.string.invite_join_back_to_email))
    }
}

@Composable
private fun JoinHouseholdSocialSignIn(
    uiState: JoinHouseholdUiState,
    showConfigMissing: Boolean,
    screenModel: JoinHouseholdScreenModel,
) {
    Text(
        text = stringResource(Res.string.invite_join_or_divider),
        style = MaterialTheme.typography.labelLarge,
        color = SharedJourneyColors.InkMuted,
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = screenModel::signInWithGoogle,
        enabled = !uiState.isLoading && !showConfigMissing,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(JoinHouseholdTestTags.GOOGLE_BUTTON),
    ) {
        Text(stringResource(Res.string.auth_continue_google))
    }

    if (uiState.showAppleSignIn) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = screenModel::signInWithApple,
            enabled = !uiState.isLoading && !showConfigMissing,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(JoinHouseholdTestTags.APPLE_BUTTON),
        ) {
            Text(stringResource(Res.string.auth_continue_apple))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.invite_join_apple_email_hint),
            style = MaterialTheme.typography.bodySmall,
            color = SharedJourneyColors.InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
