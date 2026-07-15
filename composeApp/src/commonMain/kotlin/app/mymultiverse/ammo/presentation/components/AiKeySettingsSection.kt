package app.mymultiverse.ammo.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.home_ai_key_clear
import ammo.composeapp.generated.resources.home_ai_key_cleared_snackbar
import ammo.composeapp.generated.resources.home_ai_key_how_to_get
import ammo.composeapp.generated.resources.home_ai_key_label
import ammo.composeapp.generated.resources.home_ai_key_placeholder
import ammo.composeapp.generated.resources.home_ai_key_save
import ammo.composeapp.generated.resources.home_ai_key_save_update
import ammo.composeapp.generated.resources.home_ai_key_saved_snackbar
import ammo.composeapp.generated.resources.home_ai_key_status_active
import ammo.composeapp.generated.resources.home_ai_key_status_not_configured
import ammo.composeapp.generated.resources.home_ai_key_what_it_does
import ammo.composeapp.generated.resources.home_settings_ai_section
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object AiKeySettingsSectionTestTags {
    const val ROOT = "ai_key_settings_section"
    const val INPUT = "ai_key_input"
    const val SAVE_BUTTON = "ai_key_save_button"
    const val CLEAR_BUTTON = "ai_key_clear_button"
    const val FEEDBACK = "ai_key_feedback"
    const val STATUS = "ai_key_status"
}

private const val GEMINI_KEY_URL = "https://aistudio.google.com/app/apikey"
private const val FEEDBACK_DURATION_MS = 2_500L

/**
 * Account-settings section for the Gemini API key with clear status, setup link,
 * and save/clear actions.
 *
 * Visual design:
 * - Terracotta-accented card surface to signal AI / premium feature
 * - Header row: Sparkles icon + section title + live status chip (Active / Not configured)
 * - One-sentence description of what the key enables
 * - Tappable "Get free key from Google AI Studio" link
 * - Password-masked text field with eye toggle
 * - "Save key" (new entry) or "Update key" (replacing existing) primary button
 * - "Clear" tertiary button appears only when a key is already saved
 * - Inline teal feedback text auto-dismissed after 2.5 s
 */
@Composable
fun AiKeySettingsSection(
    modifier: Modifier = Modifier,
    aiSettings: AiAssistantSettings = koinInject(),
) {
    val currentKey by aiSettings.geminiApiKey.collectAsState()
    val isConfigured = currentKey.isNotBlank()

    // Reset the field to the saved key (masked) when the stored value changes.
    var keyInput by remember(currentKey) { mutableStateOf(currentKey) }
    var keyVisible by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val savedLabel = stringResource(Res.string.home_ai_key_saved_snackbar)
    val clearedLabel = stringResource(Res.string.home_ai_key_cleared_snackbar)
    val inputTrimmed = keyInput.trim()
    val isNewKey = inputTrimmed.isNotBlank() && inputTrimmed != currentKey

    LaunchedEffect(Unit) {
        aiSettings.refreshFromRemote()
    }

    fun showFeedback(text: String) {
        feedbackText = text
        scope.launch {
            delay(FEEDBACK_DURATION_MS)
            feedbackText = null
        }
    }

    FamilyLogisticsCardSurface(
        accentColor = SharedJourneyColors.TerracottaOrange,
        modifier = modifier.testTag(AiKeySettingsSectionTestTags.ROOT),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Header row: icon + title + live status ───────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                JourneyIcon(
                    role = AppIconRole.AiAccent,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.home_settings_ai_section),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = JourneySemanticColors.inkDeep(),
                    modifier = Modifier.weight(1f),
                )
                // Live status chip
                val statusText = if (isConfigured) {
                    stringResource(Res.string.home_ai_key_status_active)
                } else {
                    stringResource(Res.string.home_ai_key_status_not_configured)
                }
                val statusColor = if (isConfigured) {
                    SharedJourneyColors.MediterraneanTeal
                } else {
                    JourneySemanticColors.inkMuted()
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                    modifier = Modifier.testTag(AiKeySettingsSectionTestTags.STATUS),
                )
            }

            // ── Description + get-key link ───────────────────────────────
            Text(
                text = stringResource(Res.string.home_ai_key_what_it_does),
                style = MaterialTheme.typography.bodySmall,
                color = JourneySemanticColors.inkMuted(),
            )

            val linkText = stringResource(Res.string.home_ai_key_how_to_get)
            val annotatedLink = buildAnnotatedString {
                pushLink(
                    LinkAnnotation.Url(
                        url = GEMINI_KEY_URL,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = SharedJourneyColors.MediterraneanTeal,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline,
                            ),
                        ),
                    ),
                )
                append(linkText)
                pop()
            }
            Text(
                text = annotatedLink,
                style = MaterialTheme.typography.bodySmall,
            )

            // ── Key input field ──────────────────────────────────────────
            JourneyTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = { Text(stringResource(Res.string.home_ai_key_label)) },
                placeholder = { Text(stringResource(Res.string.home_ai_key_placeholder)) },
                visualTransformation = if (keyVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isNewKey) {
                            aiSettings.setGeminiApiKey(inputTrimmed)
                            showFeedback(savedLabel)
                        }
                    },
                ),
                trailingIcon = {
                    JourneyIconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            imageVector = if (keyVisible) AppIcons.VisibilityOff else AppIcons.Visibility,
                            contentDescription = null,
                            tint = JourneySemanticColors.inkMuted(),
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(AiKeySettingsSectionTestTags.INPUT),
            )

            // ── Actions ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Save / Update button — label changes based on whether key already exists
                val saveLabel = if (isConfigured) {
                    stringResource(Res.string.home_ai_key_save_update)
                } else {
                    stringResource(Res.string.home_ai_key_save)
                }
                JourneyPrimaryButton(
                    onClick = {
                        if (isNewKey) {
                            aiSettings.setGeminiApiKey(inputTrimmed)
                            showFeedback(savedLabel)
                        }
                    },
                    enabled = isNewKey,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(AiKeySettingsSectionTestTags.SAVE_BUTTON),
                ) {
                    Text(saveLabel)
                }

                AnimatedVisibility(visible = isConfigured) {
                    JourneyTertiaryButton(
                        onClick = {
                            aiSettings.clearGeminiApiKey()
                            keyInput = ""
                            showFeedback(clearedLabel)
                        },
                        label = stringResource(Res.string.home_ai_key_clear),
                        modifier = Modifier.testTag(AiKeySettingsSectionTestTags.CLEAR_BUTTON),
                    )
                }
            }

            // ── Inline feedback ──────────────────────────────────────────
            AnimatedVisibility(visible = feedbackText != null) {
                Text(
                    text = feedbackText.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = SharedJourneyColors.MediterraneanTeal,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag(AiKeySettingsSectionTestTags.FEEDBACK),
                )
            }
        }
    }
}
