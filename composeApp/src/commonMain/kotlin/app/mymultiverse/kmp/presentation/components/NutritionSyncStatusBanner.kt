package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_sync_status_offline
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_sync_status_pending
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_sync_status_syncing
import org.jetbrains.compose.resources.stringResource

object NutritionSyncStatusTestTags {
    const val BANNER = "nutrition_sync_status_banner"
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
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(NutritionSyncStatusTestTags.BANNER),
        color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = SharedJourneyColors.InkDeep,
        )
    }
}
