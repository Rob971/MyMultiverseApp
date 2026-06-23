package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.manager.LanguageManager
import app.mymultiverse.kmp.domain.manager.SupportedAppLanguages
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_settings_language
import kmpvoyagercleanarchitecture.composeapp.generated.resources.language_napulitano_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.language_picker_current
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object LanguagePickerTestTags {
    const val ROOT = "settings_language_picker"
    const val TRIGGER = "settings_language_trigger"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePicker(modifier: Modifier = Modifier) {
    val languageManager = koinInject<LanguageManager>()
    val currentLanguage by languageManager.currentLanguage.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = SupportedAppLanguages.labelFor(currentLanguage)
    val pickerDescription = stringResource(Res.string.language_picker_current, currentLabel)
    val napulitanoSubtitle = stringResource(Res.string.language_napulitano_subtitle)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(LanguagePickerTestTags.ROOT),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.home_settings_language),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = JourneySemanticColors.inkDeep(),
        )
        Box {
            TextButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .testTag(LanguagePickerTestTags.TRIGGER)
                    .semantics {
                        contentDescription = pickerDescription
                    },
            ) {
                Text(
                    text = currentLabel,
                    fontWeight = FontWeight.Bold,
                    color = JourneySemanticColors.brandTeal(),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(JourneySemanticColors.cardSurface()),
            ) {
                SupportedAppLanguages.options.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = name,
                                    color = if (code == currentLanguage) {
                                        JourneySemanticColors.brandTeal()
                                    } else {
                                        JourneySemanticColors.inkDeep()
                                    },
                                )
                                if (code == SupportedAppLanguages.DEFAULT_CODE) {
                                    Text(
                                        text = napulitanoSubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JourneySemanticColors.inkMuted(),
                                        modifier = Modifier.padding(top = 2.dp),
                                    )
                                }
                            }
                        },
                        onClick = {
                            languageManager.changeLanguage(code)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
