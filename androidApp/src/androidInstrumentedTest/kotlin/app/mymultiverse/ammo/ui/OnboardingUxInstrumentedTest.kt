package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.data.home.HomeFirstWinChecklistStore
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.presentation.screens.auth.LoginScreen
import app.mymultiverse.ammo.presentation.screens.auth.LoginScreenModel
import app.mymultiverse.ammo.presentation.screens.auth.LoginTestTags
import app.mymultiverse.ammo.presentation.screens.householdsetup.HouseholdCreationScreen
import app.mymultiverse.ammo.presentation.screens.householdsetup.HouseholdCreationTestTags
import app.mymultiverse.ammo.presentation.screens.householdsetup.HouseholdSetupScreenModel
import app.mymultiverse.ammo.presentation.screens.onboarding.AuthScreen
import app.mymultiverse.ammo.presentation.screens.onboarding.AuthTestTags
import app.mymultiverse.ammo.presentation.screens.onboarding.OnboardingScreenModel
import app.mymultiverse.ammo.presentation.theme.AppTheme
import app.mymultiverse.ammo.ui.InstrumentedComposeTest.waitFor
import com.russhwolf.settings.MapSettings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingUxInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun authScreen_showsSsoAndEmailFallback() {
        composeRule.setContent {
            AppTheme {
                AuthScreen(
                    pendingInviteToken = null,
                    showConfigMissing = false,
                    onContinueWithEmail = {},
                    screenModel = onboardingScreenModel(),
                )
            }
        }

        composeRule.onNodeWithTag(AuthTestTags.SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(AuthTestTags.GOOGLE_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(AuthTestTags.EMAIL_BUTTON).assertIsDisplayed()
    }

    @Test
    fun authScreen_emailFallback_opensLoginForm() {
        var showEmailAuth by mutableStateOf(false)

        composeRule.setContent {
            AppTheme {
                if (showEmailAuth) {
                    LoginScreen(
                        showConfigMissing = false,
                        showBackToSso = true,
                        onBackToSso = { showEmailAuth = false },
                        screenModel = LoginScreenModel(
                            authRepository = InstrumentedFakeAuthRepository(AuthState.Unauthenticated),
                            logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "instrumented")),
                        ),
                    )
                } else {
                    AuthScreen(
                        pendingInviteToken = null,
                        showConfigMissing = false,
                        onContinueWithEmail = { showEmailAuth = true },
                        screenModel = onboardingScreenModel(),
                    )
                }
            }
        }

        composeRule.onNodeWithTag(AuthTestTags.EMAIL_BUTTON).performClick()
        composeRule.waitFor {
            composeRule.onAllNodesWithTag(LoginTestTags.SCREEN).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(LoginTestTags.EMAIL_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(LoginTestTags.PASSWORD_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(LoginTestTags.BACK_TO_SSO).performClick()
        composeRule.waitFor {
            composeRule.onAllNodesWithTag(AuthTestTags.SCREEN).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(AuthTestTags.GOOGLE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun authScreen_pendingInvite_showsInviteBanner() {
        val collaborationRepository = InstrumentedHouseholdCollaborationRepository().apply {
            previewInviteResult = Result.success(
                HouseholdInvitePreview(
                    inviteId = "invite-1",
                    householdId = "household-1",
                    householdName = "Rivera Family",
                    inviterName = "Alex",
                    inviteeEmail = "guest@example.com",
                    role = HouseholdMemberRole.Editor,
                    expiresAtEpochMillis = null,
                ),
            )
        }

        composeRule.setContent {
            AppTheme {
                AuthScreen(
                    pendingInviteToken = "preview-token",
                    showConfigMissing = false,
                    onContinueWithEmail = {},
                    screenModel = OnboardingScreenModel(
                        authRepository = InstrumentedFakeAuthRepository(AuthState.Unauthenticated),
                        collaborationRepository = collaborationRepository,
                    ),
                )
            }
        }

        composeRule.waitFor {
            composeRule.onAllNodesWithTag(AuthTestTags.INVITE_BANNER).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(AuthTestTags.INVITE_BANNER).assertIsDisplayed()
    }

    @Test
    fun householdCreation_createDisabledUntilNameAvailable() {
        val screenModel = householdSetupScreenModel()

        composeRule.setContent {
            AppTheme {
                HouseholdCreationScreen(
                    onHouseholdCreated = {},
                    screenModel = screenModel,
                )
            }
        }

        composeRule.onNodeWithTag(HouseholdCreationTestTags.SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(HouseholdCreationTestTags.NAME_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(HouseholdCreationTestTags.CREATE_BUTTON).assertIsNotEnabled()

        composeRule.onNodeWithTag(HouseholdCreationTestTags.NAME_FIELD).performTextInput("Rivera Family")
        composeRule.waitFor(timeoutMillis = 3_000) {
            screenModel.uiState.value.nameAvailability ==
                app.mymultiverse.ammo.presentation.screens.home.HouseholdNameAvailability.Available
        }
        composeRule.onNodeWithTag(HouseholdCreationTestTags.CREATE_BUTTON).assertIsEnabled()
    }

    private fun onboardingScreenModel(): OnboardingScreenModel =
        OnboardingScreenModel(
            authRepository = InstrumentedFakeAuthRepository(AuthState.Unauthenticated),
            collaborationRepository = InstrumentedHouseholdCollaborationRepository(),
        )

    private fun householdSetupScreenModel(): HouseholdSetupScreenModel {
        val authRepository = InstrumentedFakeAuthRepository(
            AuthState.Authenticated(
                AuthUser(
                    id = "user-1",
                    email = "tester@example.com",
                    displayName = "Alex",
                ),
            ),
        )
        val householdRepository = InstrumentedHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        val nutritionRepository = InstrumentedNutritionRepository(weekKey = "2026-W25")
        val sessionCoordinator = InstrumentedNutritionSessionCoordinator(nutritionRepository)
        val logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "instrumented"))

        return HouseholdSetupScreenModel(
            authRepository = authRepository,
            householdRepository = householdRepository,
            collaborationRepository = InstrumentedHouseholdCollaborationRepository(),
            sessionCoordinator = sessionCoordinator,
            firstWinChecklistStore = HomeFirstWinChecklistStore(MapSettings()),
            logger = logger,
        )
    }
}
