package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_sync_status_offline
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_sync_status_pending
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_sync_status_synced
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_sync_status_syncing
import org.jetbrains.compose.resources.stringResource

object NutritionSyncStatusTestTags {
    const val BANNER = "nutrition_sync_status_banner"
    const val ICON = "nutrition_sync_status_icon"
}

@Composable
fun NutritionSyncStatusBanner(
    status: NutritionSyncStatus,
    modifier: Modifier = Modifier,
) {
    val message = when (status) {
        NutritionSyncStatus.Idle -> return
        NutritionSyncStatus.Syncing -> stringResource(Res.string.nutrition_sync_status_syncing)
        is NutritionSyncStatus.PendingPush -> stringResource(
            Res.string.nutrition_sync_status_pending,
            status.pendingCount,
        )
        NutritionSyncStatus.RemoteUnavailable -> stringResource(Res.string.nutrition_sync_status_offline)
        NutritionSyncStatus.Synced -> stringResource(Res.string.nutrition_sync_status_synced)
    }

    val iconTint = JourneySemanticColors.inkDeep()
    val accent = SharedJourneyColors.MediterraneanTeal

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(NutritionSyncStatusTestTags.BANNER),
        color = accent.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (status) {
                NutritionSyncStatus.Syncing -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .testTag(NutritionSyncStatusTestTags.ICON),
                        strokeWidth = 2.dp,
                        color = accent,
                    )
                }
                NutritionSyncStatus.Synced -> {
                    Icon(
                        imageVector = AppIcons.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .testTag(NutritionSyncStatusTestTags.ICON),
                        tint = accent,
                    )
                }
                is NutritionSyncStatus.PendingPush -> {
                    Icon(
                        imageVector = AppIcons.Refresh,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .testTag(NutritionSyncStatusTestTags.ICON),
                        tint = iconTint,
                    )
                }
                NutritionSyncStatus.RemoteUnavailable -> {
                    Icon(
                        imageVector = AppIcons.Refresh,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .testTag(NutritionSyncStatusTestTags.ICON),
                        tint = JourneySemanticColors.inkMuted(),
                    )
                }
                NutritionSyncStatus.Idle -> Unit
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = iconTint,
            )
        }
    }
}
