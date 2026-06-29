package app.mymultiverse.ammo.presentation.screens.nutrition

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.domain.nutrition.NutritionAiMode
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import org.koin.compose.koinInject

object AiHelperSheetTestTags {
    const val SHEET = "ai_helper_sheet"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHelperSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    launchContext: AiHelperLaunchContext,
    onApplied: () -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = JourneySemanticColors.cardSurface(),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .testTag(AiHelperSheetTestTags.SHEET),
        ) {
            NutritionAiAssistantContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight * 0.6f),
                launchContext = launchContext,
                compact = true,
                screenModel = screenModel,
                onMealPlanApplied = onApplied,
            )
        }
    }
}
