package app.mymultiverse.kmp.presentation.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import app.mymultiverse.kmp.presentation.components.JourneyTextField
import app.mymultiverse.kmp.presentation.components.JourneyTextFieldDefaults
import app.mymultiverse.kmp.presentation.components.JourneyPrimaryButton
import app.mymultiverse.kmp.presentation.components.JourneySecondaryButton
import app.mymultiverse.kmp.presentation.components.JourneyTertiaryButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_error_email_unconfirmed
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_error_generic
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_error_invalid_credentials
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_error_invalid_email
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_error_signup_disabled
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_error_user_already_exists
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_error_weak_password
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_password_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_provider_coming_soon
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_sign_in_button
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_sign_up_button
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_subtitle_sign_in
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_subtitle_sign_up
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_success_email_confirmation
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_switch_to_sign_in
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_switch_to_sign_up
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object LoginTestTags {
    const val EMAIL_FIELD = "login_email_field"
    const val PASSWORD_FIELD = "login_password_field"
    const val SUBMIT_BUTTON = "login_submit_button"
    const val GOOGLE_BUTTON = "login_google_button"
    const val APPLE_BUTTON = "login_apple_button"
}

@Composable
fun LoginScreen(
    showConfigMissing: Boolean,
    screenModel: LoginScreenModel = koinInject(),
) {
    val uiState by screenModel.uiState.collectAsState()
    val feedbackMessage = when {
        showConfigMissing -> stringResource(Res.string.auth_error_config_missing) to false
        uiState.message is LoginMessage.EmailConfirmationSent ->
            stringResource(Res.string.auth_success_email_confirmation) to true
        uiState.message is LoginMessage.Error -> {
            val error = (uiState.message as LoginMessage.Error).type
            if (error.isScreenLevelOnly()) {
                when (error) {
                    LoginError.ConfigMissing -> stringResource(Res.string.auth_error_config_missing) to false
                    LoginError.ProviderComingSoon -> stringResource(Res.string.auth_provider_coming_soon) to false
                    LoginError.SignUpDisabled -> stringResource(Res.string.auth_error_signup_disabled) to false
                    else -> stringResource(Res.string.auth_error_generic) to false
                }
            } else {
                null
            }
        }
        else -> null
    }
    val fieldError = (uiState.message as? LoginMessage.Error)?.type
    val emailFieldErrorText = when (fieldError) {
        LoginError.InvalidEmail -> stringResource(Res.string.auth_error_invalid_email)
        LoginError.UserAlreadyExists -> stringResource(Res.string.auth_error_user_already_exists)
        LoginError.EmailNotConfirmed -> stringResource(Res.string.auth_error_email_unconfirmed)
        else -> null
    }
    val passwordFieldErrorText = when (fieldError) {
        LoginError.WeakPassword -> stringResource(Res.string.auth_error_weak_password)
        LoginError.InvalidCredentials -> stringResource(Res.string.auth_error_invalid_credentials)
        else -> null
    }

    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ScreenLayout.horizontalPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            VesuvianHeartLogo(modifier = Modifier.height(96.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(Res.string.auth_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SharedJourneyColors.InkDeep,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    if (uiState.isSignUpMode) {
                        Res.string.auth_subtitle_sign_up
                    } else {
                        Res.string.auth_subtitle_sign_in
                    },
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = SharedJourneyColors.InkSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))

            JourneySecondaryButton(
                onClick = screenModel::signInWithGoogle,
                enabled = !uiState.isLoading && !showConfigMissing,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LoginTestTags.GOOGLE_BUTTON),
            ) {
                Text(stringResource(Res.string.auth_continue_google))
            }
            Spacer(modifier = Modifier.height(12.dp))
            JourneySecondaryButton(
                onClick = screenModel::signInWithApple,
                enabled = !uiState.isLoading && !showConfigMissing,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LoginTestTags.APPLE_BUTTON),
            ) {
                Text(stringResource(Res.string.auth_continue_apple))
            }
            Spacer(modifier = Modifier.height(20.dp))

            JourneyTextField(
                value = uiState.email,
                onValueChange = screenModel::onEmailChange,
                label = { Text(stringResource(Res.string.auth_email_label)) },
                enabled = !uiState.isLoading && !showConfigMissing,
                isError = emailFieldErrorText != null,
                supportingText = emailFieldErrorText?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LoginTestTags.EMAIL_FIELD),
            )
            Spacer(modifier = Modifier.height(JourneyTextFieldDefaults.fieldSpacing))
            JourneyTextField(
                value = uiState.password,
                onValueChange = screenModel::onPasswordChange,
                label = { Text(stringResource(Res.string.auth_password_label)) },
                enabled = !uiState.isLoading && !showConfigMissing,
                isError = passwordFieldErrorText != null,
                supportingText = passwordFieldErrorText?.let { error -> { Text(error) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { screenModel.submitEmailAuth() },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LoginTestTags.PASSWORD_FIELD),
            )

            if (feedbackMessage != null) {
                val (text, isSuccess) = feedbackMessage
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = text,
                    color = if (isSuccess) {
                        SharedJourneyColors.MediterraneanTeal
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            JourneyPrimaryButton(
                onClick = screenModel::submitEmailAuth,
                enabled = !showConfigMissing && uiState.canSubmitEmailAuth,
                isLoading = uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LoginTestTags.SUBMIT_BUTTON),
            ) {
                Text(
                    stringResource(
                        if (uiState.isSignUpMode) {
                            Res.string.auth_sign_up_button
                        } else {
                            Res.string.auth_sign_in_button
                        },
                    ),
                )
            }
            JourneyTertiaryButton(
                onClick = screenModel::toggleSignUpMode,
                enabled = !uiState.isLoading && !showConfigMissing,
                label = stringResource(
                    if (uiState.isSignUpMode) {
                        Res.string.auth_switch_to_sign_in
                    } else {
                        Res.string.auth_switch_to_sign_up
                    },
                ),
            )
        }
    }
}
