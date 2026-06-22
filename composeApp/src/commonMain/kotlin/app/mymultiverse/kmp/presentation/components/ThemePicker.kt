package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.manager.AppThemePreference
import app.mymultiverse.kmp.domain.manager.ThemeManager
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_settings_appearance
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_theme_dark
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_theme_light
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_theme_system
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object ThemePickerTestTags {
    const val ROOT = "settings_theme_picker"
    const val SYSTEM = "settings_theme_system"
    const val LIGHT = "settings_theme_light"
    const val DARK = "settings_theme_dark"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePicker(modifier: Modifier = Modifier) {
    val themeManager = koinInject<ThemeManager>()
    val preference by themeManager.currentPreference.collectAsState()

    val options = listOf(
        Triple(AppThemePreference.SYSTEM, stringResource(Res.string.home_theme_system), ThemePickerTestTags.SYSTEM),
        Triple(AppThemePreference.LIGHT, stringResource(Res.string.home_theme_light), ThemePickerTestTags.LIGHT),
        Triple(AppThemePreference.DARK, stringResource(Res.string.home_theme_dark), ThemePickerTestTags.DARK),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(ThemePickerTestTags.ROOT),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.home_settings_appearance),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = JourneySemanticColors.inkDeep(),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (value, label, testTag) ->
                SegmentedButton(
                    selected = preference == value,
                    onClick = { themeManager.changeThemePreference(value) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    modifier = Modifier
                        .testTag(testTag)
                        .padding(horizontal = 1.dp),
                    label = {
                        Text(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                )
            }
        }
    }
}
