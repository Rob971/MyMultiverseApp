package app.mymultiverse.ammo.presentation.screens.auth

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
import app.mymultiverse.ammo.presentation.components.GlobalLanguageAction
import app.mymultiverse.ammo.presentation.components.JourneyTextField
import app.mymultiverse.ammo.presentation.components.JourneyTextFieldDefaults
import app.mymultiverse.ammo.presentation.components.JourneyButtonLabel
import app.mymultiverse.ammo.presentation.components.JourneyPrimaryButton
import app.mymultiverse.ammo.presentation.components.JourneySecondaryButton
import app.mymultiverse.ammo.presentation.components.JourneySsoButtonLabel
import app.mymultiverse.ammo.presentation.components.JourneyTertiaryButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.components.AdaptiveFormWidth
import app.mymultiverse.ammo.presentation.components.AmmoRoundLogo
import app.mymultiverse.ammo.presentation.components.ScreenLayout
import app.mymultiverse.ammo.presentation.components.keyboardAwareScroll
import app.mymultiverse.ammo.presentation.components.rememberFieldScrollIntoViewModifier
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.auth_back_to_sso
import ammo.composeapp.generated.resources.auth_continue_apple
import ammo.composeapp.generated.resources.auth_continue_google
import ammo.composeapp.generated.resources.auth_email_label
import ammo.composeapp.generated.resources.auth_error_config_missing
import ammo.composeapp.generated.resources.auth_error_email_unconfirmed
import ammo.composeapp.generated.resources.auth_error_generic
import ammo.composeapp.generated.resources.auth_error_invalid_credentials
import ammo.composeapp.generated.resources.auth_error_invalid_email
import ammo.composeapp.generated.resources.auth_error_signup_disabled
import ammo.composeapp.generated.resources.auth_error_user_already_exists
import ammo.composeapp.generated.resources.auth_error_weak_password
import ammo.composeapp.generated.resources.auth_password_label
import ammo.composeapp.generated.resources.auth_provider_coming_soon
import ammo.composeapp.generated.resources.auth_sign_in_button
import ammo.composeapp.generated.resources.auth_sign_up_button
import ammo.composeapp.generated.resources.auth_subtitle
import ammo.composeapp.generated.resources.auth_subtitle_sign_in
import ammo.composeapp.generated.resources.auth_subtitle_sign_up
import ammo.composeapp.generated.resources.auth_success_email_confirmation
import ammo.composeapp.generated.resources.auth_switch_to_sign_in
import ammo.composeapp.generated.resources.auth_switch_to_sign_up
import ammo.composeapp.generated.resources.auth_title
import ammo.composeapp.generated.resources.home_designer_credit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object LoginTestTags {
    const val SCREEN = "login_screen"
    const val EMAIL_FIELD = "login_email_field"
    const val PASSWORD_FIELD = "login_password_field"
    const val SUBMIT_BUTTON = "login_submit_button"
    const val GOOGLE_BUTTON = "login_google_button"
    const val APPLE_BUTTON = "login_apple_button"
    const val BACK_TO_SSO = "login_back_to_sso"
    const val DESIGNER = "login_designer"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    showConfigMissing: Boolean,
    showBackToSso: Boolean = false,
    onBackToSso: () -> Unit = {},
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
    val scrollState = rememberScrollState()
    val emailScrollIntoView = rememberFieldScrollIntoViewModifier()
    val passwordScrollIntoView = rememberFieldScrollIntoViewModifier()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { },
                actions = { GlobalLanguageAction() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        AdaptiveFormWidth(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ScreenLayout.horizontalPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .keyboardAwareScroll(scrollState)
                    .testTag(LoginTestTags.SCREEN),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
            Spacer(modifier = Modifier.height(ScreenLayout.contentTopPadding))
            AmmoRoundLogo(modifier = Modifier.size(96.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(Res.string.auth_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = JourneySemanticColors.inkDeep(),
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
                color = JourneySemanticColors.inkSecondary(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))

            if (showBackToSso) {
                JourneyTertiaryButton(
                    onClick = onBackToSso,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(LoginTestTags.BACK_TO_SSO),
                    label = stringResource(Res.string.auth_back_to_sso),
                )
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                JourneySecondaryButton(
                    onClick = screenModel::signInWithGoogle,
                    enabled = !uiState.isLoading && !showConfigMissing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(LoginTestTags.GOOGLE_BUTTON),
                ) {
                    JourneySsoButtonLabel(
                        text = stringResource(Res.string.auth_continue_google),
                        provider = AppIconRole.SsoGoogle,
                        useContentColor = true,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                JourneySecondaryButton(
                    onClick = screenModel::signInWithApple,
                    enabled = !uiState.isLoading && !showConfigMissing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(LoginTestTags.APPLE_BUTTON),
                ) {
                    JourneySsoButtonLabel(
                        text = stringResource(Res.string.auth_continue_apple),
                        provider = AppIconRole.SsoApple,
                        useContentColor = true,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

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
                    .then(emailScrollIntoView)
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
                    .then(passwordScrollIntoView)
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
                JourneyButtonLabel(
                    text = stringResource(
                        if (uiState.isSignUpMode) {
                            Res.string.auth_sign_up_button
                        } else {
                            Res.string.auth_sign_in_button
                        },
                    ),
                    icon = AppIcons.Person,
                    role = AppIconRole.OnAccent,
                    useContentColor = true,
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.home_designer_credit),
                style = MaterialTheme.typography.labelSmall,
                color = JourneySemanticColors.inkMuted(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LoginTestTags.DESIGNER),
            )
            Spacer(modifier = Modifier.height(ScreenLayout.contentBottomPadding))
            }
        }
    }
}
