package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import app.mymultiverse.kmp.domain.manager.LanguageManager
import app.mymultiverse.kmp.domain.manager.SupportedAppLanguages
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePicker(modifier: Modifier = Modifier) {
    val languageManager = koinInject<LanguageManager>()
    val currentLanguage by languageManager.currentLanguage.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.semantics {
                contentDescription = "App language: $currentLanguage"
            },
        ) {
            Text(
                text = currentLanguage.uppercase(),
                fontWeight = FontWeight.Bold,
                color = SharedJourneyColors.MediterraneanTeal,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SharedJourneyColors.SunDrenchedWhite),
        ) {
            SupportedAppLanguages.options.forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = name,
                            color = if (code == currentLanguage) {
                                SharedJourneyColors.MediterraneanTeal
                            } else {
                                SharedJourneyColors.InkDeep
                            },
                        )
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
