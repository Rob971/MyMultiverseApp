package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.AppBuildInfo
import app.mymultiverse.kmp.presentation.components.LanguagePicker
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_sign_out
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_app_version
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_app_version_rc
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_delete_account
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_export_personal_data
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_settings_title
import org.jetbrains.compose.resources.stringResource

object HomeAccountSheetTestTags {
    const val SHEET = "home_account_sheet"
    const val SIGN_OUT = HomeTestTags.SIGN_OUT_BUTTON
    const val EXPORT = HomeTestTags.EXPORT_DATA_BUTTON
    const val DELETE = HomeTestTags.DELETE_ACCOUNT_BUTTON
    const val VERSION = HomeTestTags.APP_VERSION_LABEL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAccountSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    onExportPersonalData: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SharedJourneyColors.GlassWhite,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag(HomeAccountSheetTestTags.SHEET),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.home_settings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SharedJourneyColors.InkDeep,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            LanguagePicker()

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = SharedJourneyColors.InkMuted.copy(alpha = 0.25f),
            )

            TextButton(
                onClick = {
                    onDismiss()
                    onExportPersonalData()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HomeAccountSheetTestTags.EXPORT),
            ) {
                Text(
                    stringResource(Res.string.home_export_personal_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SharedJourneyColors.InkDeep,
                )
            }

            TextButton(
                onClick = {
                    onDismiss()
                    onDeleteAccount()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HomeAccountSheetTestTags.DELETE),
            ) {
                Text(
                    stringResource(Res.string.home_delete_account),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SharedJourneyColors.TerracottaOrange,
                )
            }

            TextButton(
                onClick = {
                    onDismiss()
                    onSignOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HomeAccountSheetTestTags.SIGN_OUT),
            ) {
                Text(
                    stringResource(Res.string.auth_sign_out),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SharedJourneyColors.InkDeep,
                )
            }

            val versionLabel =
                if (AppBuildInfo.IS_PRERELEASE) {
                    stringResource(Res.string.home_app_version_rc, AppBuildInfo.VERSION_NAME)
                } else {
                    stringResource(Res.string.home_app_version, AppBuildInfo.VERSION_NAME)
                }
            Text(
                text = versionLabel,
                style = MaterialTheme.typography.labelSmall,
                color = SharedJourneyColors.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .testTag(HomeAccountSheetTestTags.VERSION),
                textAlign = TextAlign.Center,
            )
        }
    }
}
