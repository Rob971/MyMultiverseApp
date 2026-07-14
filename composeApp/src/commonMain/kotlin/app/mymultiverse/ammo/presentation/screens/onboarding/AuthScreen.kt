package app.mymultiverse.ammo.presentation.screens.onboarding

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.domain.AppBuildInfo
import app.mymultiverse.ammo.presentation.components.GlobalLanguageAction
import app.mymultiverse.ammo.presentation.components.JourneyBanner
import app.mymultiverse.ammo.presentation.components.JourneySsoButtonLabel
import app.mymultiverse.ammo.presentation.components.JourneyTertiaryButton
import app.mymultiverse.ammo.presentation.components.ScreenLayout
import app.mymultiverse.ammo.presentation.components.AmmoRoundLogo
import app.mymultiverse.ammo.presentation.screens.auth.LoginError
import app.mymultiverse.ammo.presentation.screens.auth.LoginMessage
import app.mymultiverse.ammo.presentation.screens.auth.isScreenLevelOnly
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.auth_continue_apple
import ammo.composeapp.generated.resources.auth_continue_google
import ammo.composeapp.generated.resources.auth_continue_with_email
import ammo.composeapp.generated.resources.auth_error_config_missing
import ammo.composeapp.generated.resources.auth_error_generic
import ammo.composeapp.generated.resources.auth_provider_coming_soon
import ammo.composeapp.generated.resources.auth_subtitle
import ammo.composeapp.generated.resources.auth_title
import ammo.composeapp.generated.resources.home_app_version
import ammo.composeapp.generated.resources.home_app_version_rc
import ammo.composeapp.generated.resources.home_copyright_notice
import ammo.composeapp.generated.resources.home_designer_credit
import ammo.composeapp.generated.resources.home_trademark_notice
import ammo.composeapp.generated.resources.onboarding_auth_invite_banner
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object AuthTestTags {
    const val SCREEN = "auth_screen"
    const val INVITE_BANNER = "auth_invite_banner"
    const val GOOGLE_BUTTON = "auth_google_button"
    const val APPLE_BUTTON = "auth_apple_button"
    const val EMAIL_BUTTON = "auth_email_button"
    const val ERROR = "auth_error"
    const val VERSION = "auth_version_label"
    const val DESIGNER = "auth_designer_label"
    const val COPYRIGHT = "auth_copyright_label"
    const val TRADEMARK = "auth_trademark_label"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    pendingInviteToken: String?,
    showConfigMissing: Boolean,
    onContinueWithEmail: () -> Unit,
    screenModel: OnboardingScreenModel = koinInject(),
) {
    val uiState by screenModel.uiState.collectAsState()

    LaunchedEffect(pendingInviteToken) {
        screenModel.loadInvitePreview(pendingInviteToken)
    }

    val feedbackMessage = when {
        showConfigMissing -> stringResource(Res.string.auth_error_config_missing)
        uiState.message is LoginMessage.Error -> {
            val error = (uiState.message as LoginMessage.Error).type
            if (error.isScreenLevelOnly()) {
                when (error) {
                    LoginError.ConfigMissing -> stringResource(Res.string.auth_error_config_missing)
                    LoginError.ProviderComingSoon -> stringResource(Res.string.auth_provider_coming_soon)
                    else -> stringResource(Res.string.auth_error_generic)
                }
            } else {
                stringResource(Res.string.auth_error_generic)
            }
        }
        else -> null
    }

    val inviteHouseholdName = uiState.inviteHouseholdName

    val versionLabel = if (AppBuildInfo.IS_PRERELEASE) {
        stringResource(Res.string.home_app_version_rc, AppBuildInfo.VERSION_NAME)
    } else {
        stringResource(Res.string.home_app_version, AppBuildInfo.VERSION_NAME)
    }
    val copyrightYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

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
        // Inline adaptive-width logic so the Column can use fillMaxHeight() for weight-based
        // vertical centering, which requires a height-bounded parent.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ScreenLayout.horizontalPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val formModifier = if (ScreenLayout.isWideWidth(maxWidth)) {
                Modifier.widthIn(max = ScreenLayout.formMaxWidth)
            } else {
                Modifier.fillMaxWidth()
            }
            Column(
                modifier = formModifier
                    .fillMaxHeight()
                    .testTag(AuthTestTags.SCREEN),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))

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
                    text = stringResource(Res.string.auth_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = JourneySemanticColors.inkSecondary(),
                    textAlign = TextAlign.Center,
                )

                if (!inviteHouseholdName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    JourneyBanner(
                        headline = stringResource(Res.string.onboarding_auth_invite_banner, inviteHouseholdName),
                        supportingLine = null,
                        modifier = Modifier.testTag(AuthTestTags.INVITE_BANNER),
                    )
                } else if (uiState.invitePreviewState is InvitePreviewState.Loading && !pendingInviteToken.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(
                        color = JourneySemanticColors.brandTeal(),
                        modifier = Modifier.height(28.dp),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                OnboardingSsoButton(
                    text = stringResource(Res.string.auth_continue_google),
                    provider = AppIconRole.SsoGoogle,
                    onClick = screenModel::signInWithGoogle,
                    enabled = !uiState.isLoading && !showConfigMissing,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.testTag(AuthTestTags.GOOGLE_BUTTON),
                )
                if (uiState.showAppleSignIn) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OnboardingSsoButton(
                        text = stringResource(Res.string.auth_continue_apple),
                        provider = AppIconRole.SsoApple,
                        onClick = screenModel::signInWithApple,
                        enabled = !uiState.isLoading && !showConfigMissing,
                        isLoading = false,
                        modifier = Modifier.testTag(AuthTestTags.APPLE_BUTTON),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                JourneyTertiaryButton(
                    onClick = onContinueWithEmail,
                    enabled = !uiState.isLoading && !showConfigMissing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AuthTestTags.EMAIL_BUTTON),
                    label = stringResource(Res.string.auth_continue_with_email),
                )

                if (feedbackMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = feedbackMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(AuthTestTags.ERROR),
                    )
                }

                Spacer(modifier = Modifier.weight(3f))

                Text(
                    text = versionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = JourneySemanticColors.inkMuted(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AuthTestTags.VERSION),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.home_designer_credit),
                    style = MaterialTheme.typography.labelSmall,
                    color = JourneySemanticColors.inkMuted(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AuthTestTags.DESIGNER),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.home_copyright_notice, copyrightYear),
                    style = MaterialTheme.typography.labelSmall,
                    color = JourneySemanticColors.inkMuted(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AuthTestTags.COPYRIGHT),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.home_trademark_notice),
                    style = MaterialTheme.typography.labelSmall,
                    color = JourneySemanticColors.inkMuted(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AuthTestTags.TRADEMARK),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OnboardingSsoButton(
    text: String,
    provider: AppIconRole,
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.height(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            JourneySsoButtonLabel(
                text = text,
                provider = provider,
                useContentColor = true,
            )
        }
    }
}
