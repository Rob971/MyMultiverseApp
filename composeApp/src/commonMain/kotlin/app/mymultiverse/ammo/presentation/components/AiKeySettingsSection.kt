package app.mymultiverse.ammo.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
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
import ammo.composeapp.generated.resources.home_ai_key_saved_snackbar
import ammo.composeapp.generated.resources.home_ai_key_what_it_does
import ammo.composeapp.generated.resources.home_settings_ai_section
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
}

private const val GEMINI_KEY_URL = "https://aistudio.google.com/app/apikey"
private const val FEEDBACK_DURATION_MS = 2_500L

/**
 * Account-settings section that lets the user enter and persist their Gemini API key.
 *
 * Shows:
 * - "AI Ingredients" section header
 * - What the key enables and a tappable link to aistudio.google.com
 * - Password-masked text field with a show/hide toggle
 * - Save / Clear action buttons with brief inline confirmation
 */
@Composable
fun AiKeySettingsSection(
    modifier: Modifier = Modifier,
    aiSettings: AiAssistantSettings = koinInject(),
) {
    val currentKey by aiSettings.geminiApiKey.collectAsState()
    val isConfigured = currentKey.isNotBlank()

    // Seed the text field with the saved key; reset whenever the stored key changes.
    var keyInput by remember(currentKey) { mutableStateOf(currentKey) }
    var keyVisible by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val savedLabel = stringResource(Res.string.home_ai_key_saved_snackbar)
    val clearedLabel = stringResource(Res.string.home_ai_key_cleared_snackbar)

    fun showFeedback(text: String) {
        feedbackText = text
        scope.launch {
            delay(FEEDBACK_DURATION_MS)
            feedbackText = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AiKeySettingsSectionTestTags.ROOT),
    ) {
        FamilyLogisticsSectionHeader(
            title = stringResource(Res.string.home_settings_ai_section),
        )

        Text(
            text = stringResource(Res.string.home_ai_key_what_it_does),
            style = MaterialTheme.typography.bodySmall,
            color = JourneySemanticColors.inkMuted(),
            modifier = Modifier.padding(bottom = 4.dp),
        )

        // Tappable link to the API key page using the modern LinkAnnotation API
        val linkText = stringResource(Res.string.home_ai_key_how_to_get)
        val annotatedLink = buildAnnotatedString {
            pushLink(
                LinkAnnotation.Url(
                    url = GEMINI_KEY_URL,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = SharedJourneyColors.MediterraneanTeal,
                            fontWeight = FontWeight.SemiBold,
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
            modifier = Modifier.padding(bottom = 8.dp),
        )

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
                    val trimmed = keyInput.trim()
                    if (trimmed.isNotBlank()) {
                        aiSettings.setGeminiApiKey(trimmed)
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

        Spacer(modifier = Modifier.height(8.dp))

        val inputTrimmed = keyInput.trim()
        Row(modifier = Modifier.fillMaxWidth()) {
            JourneyPrimaryButton(
                onClick = {
                    if (inputTrimmed.isNotBlank()) {
                        aiSettings.setGeminiApiKey(inputTrimmed)
                        showFeedback(savedLabel)
                    }
                },
                enabled = inputTrimmed.isNotBlank() && inputTrimmed != currentKey,
                modifier = Modifier
                    .weight(1f)
                    .testTag(AiKeySettingsSectionTestTags.SAVE_BUTTON),
            ) {
                Text(stringResource(Res.string.home_ai_key_save))
            }
            AnimatedVisibility(visible = isConfigured) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
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
        }

        AnimatedVisibility(visible = feedbackText != null) {
            Text(
                text = feedbackText.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag(AiKeySettingsSectionTestTags.FEEDBACK),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
