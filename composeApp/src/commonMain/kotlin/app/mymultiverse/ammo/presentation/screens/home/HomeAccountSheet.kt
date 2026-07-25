package app.mymultiverse.ammo.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import app.mymultiverse.ammo.domain.AppBuildInfo
import app.mymultiverse.ammo.presentation.components.AiKeySettingsSection
import app.mymultiverse.ammo.presentation.components.HomeHouseholdButton
import app.mymultiverse.ammo.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.ammo.presentation.components.JourneyDestructiveTextButton
import app.mymultiverse.ammo.presentation.components.LanguagePicker
import app.mymultiverse.ammo.presentation.components.ThemePicker
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.auth_sign_out
import ammo.composeapp.generated.resources.home_app_version
import ammo.composeapp.generated.resources.home_check_for_updates
import ammo.composeapp.generated.resources.home_copyright_notice
import ammo.composeapp.generated.resources.home_trademark_notice
import ammo.composeapp.generated.resources.home_designer_credit
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ammo.composeapp.generated.resources.home_delete_account
import ammo.composeapp.generated.resources.home_export_personal_data
import ammo.composeapp.generated.resources.home_section_household
import ammo.composeapp.generated.resources.home_settings_title
import org.jetbrains.compose.resources.stringResource

object HomeAccountSheetTestTags {
    const val SHEET = "home_account_sheet"
    const val FAMILY_HUB = "home_account_family_hub"
    const val SIGN_OUT = HomeTestTags.SIGN_OUT_BUTTON
    const val EXPORT = HomeTestTags.EXPORT_DATA_BUTTON
    const val DELETE = HomeTestTags.DELETE_ACCOUNT_BUTTON
    const val VERSION = HomeTestTags.APP_VERSION_LABEL
    const val CHECK_FOR_UPDATES = "home_account_check_for_updates"
    const val COPYRIGHT = "home_account_copyright"
    const val TRADEMARK = "home_account_trademark"
    const val DESIGNER = "home_account_designer"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAccountSheet(
    visible: Boolean,
    householdName: String?,
    canRenameHousehold: Boolean,
    onDismiss: () -> Unit,
    onOpenHouseholdMembers: () -> Unit,
    onRenameHousehold: () -> Unit,
    onSignOut: () -> Unit,
    onExportPersonalData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onCheckForUpdates: () -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = JourneySemanticColors.cardSurface(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag(HomeAccountSheetTestTags.SHEET),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.home_settings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = JourneySemanticColors.inkDeep(),
                modifier = Modifier.padding(bottom = 8.dp),
            )

            if (!householdName.isNullOrBlank()) {
                FamilyLogisticsSectionHeader(
                    title = stringResource(Res.string.home_section_household),
                )
                HomeHouseholdButton(
                    householdName = householdName,
                    canManage = canRenameHousehold,
                    onOpenHousehold = {
                        onDismiss()
                        onOpenHouseholdMembers()
                    },
                    onRenameHousehold = {
                        onDismiss()
                        onRenameHousehold()
                    },
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .testTag(HomeAccountSheetTestTags.FAMILY_HUB),
                )
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 12.dp),
                    color = JourneySemanticColors.inkMuted().copy(alpha = 0.25f),
                )
            }

            ThemePicker(modifier = Modifier.padding(bottom = 12.dp))

            LanguagePicker()

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = JourneySemanticColors.inkMuted().copy(alpha = 0.25f),
            )

            AiKeySettingsSection()

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = JourneySemanticColors.inkMuted().copy(alpha = 0.25f),
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
                    color = JourneySemanticColors.inkDeep(),
                )
            }

            JourneyDestructiveTextButton(
                onClick = {
                    onDismiss()
                    onDeleteAccount()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HomeAccountSheetTestTags.DELETE),
                label = stringResource(Res.string.home_delete_account),
            )

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
                    color = JourneySemanticColors.inkDeep(),
                )
            }

            val versionLabel = stringResource(Res.string.home_app_version, AppBuildInfo.VERSION_NAME)
            val copyrightYear =
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
            Text(
                text = versionLabel,
                style = MaterialTheme.typography.labelSmall,
                color = JourneySemanticColors.inkMuted(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .testTag(HomeAccountSheetTestTags.VERSION),
                textAlign = TextAlign.Center,
            )
            TextButton(
                onClick = onCheckForUpdates,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HomeAccountSheetTestTags.CHECK_FOR_UPDATES),
            ) {
                Text(
                    text = stringResource(Res.string.home_check_for_updates),
                    style = MaterialTheme.typography.labelSmall,
                    color = JourneySemanticColors.inkMuted(),
                )
            }
            Text(
                text = stringResource(Res.string.home_designer_credit),
                style = MaterialTheme.typography.labelSmall,
                color = JourneySemanticColors.inkMuted(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .testTag(HomeAccountSheetTestTags.DESIGNER),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.home_copyright_notice, copyrightYear),
                style = MaterialTheme.typography.labelSmall,
                color = JourneySemanticColors.inkMuted(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag(HomeAccountSheetTestTags.COPYRIGHT),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.home_trademark_notice),
                style = MaterialTheme.typography.labelSmall,
                color = JourneySemanticColors.inkMuted(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 16.dp)
                    .testTag(HomeAccountSheetTestTags.TRADEMARK),
                textAlign = TextAlign.Center,
            )
        }
    }
}
