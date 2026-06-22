package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.screenContentArea
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object NutritionAiTestTags {
    const val SCROLL_LIST = "nutrition_ai_scroll"
    const val CRITERIA_FIELD = "nutrition_ai_criteria"
    const val GENERATE_BUTTON = "nutrition_ai_generate"
    const val ANSWER_CARD = "nutrition_ai_answer"
    const val APPLY_MEAL_PLAN_BUTTON = "nutrition_ai_apply_meal_plan"
    const val CLEAR_AI_GROCERY_BUTTON = "nutrition_ai_clear_grocery"
    const val MODE_ADVICE = "nutrition_ai_mode_advice"
    const val MODE_GROCERY = "nutrition_ai_mode_grocery"
    const val MODE_MEAL_PLAN = "nutrition_ai_mode_meal_plan"
    const val SCOPE_FULL_WEEK = "nutrition_ai_scope_full_week"
    const val SCOPE_TODAY = "nutrition_ai_scope_today"
    const val MORE_OPTIONS_TOGGLE = "nutrition_ai_more_options"
    const val IDLE_EMPTY = "nutrition_ai_idle_empty"
    const val MEAL_PLAN_PREVIEW_ROW_PREFIX = "nutrition_ai_meal_preview_"
}

@Composable
fun NutritionAiAdviceScreen(
    onBack: () -> Unit,
    initialMode: NutritionAiMode? = null,
    screenModel: NutritionScreenModel = koinInject(),
) {
    // Deep-link / legacy fallback — inline flows use AiHelperSheet on Plan and Groceries tabs.
    val snackbarHostState = remember { SnackbarHostState() }

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_ai_title),
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        NutritionAiAssistantContent(
            modifier = Modifier.screenContentArea(padding),
            initialMode = initialMode,
            compact = false,
            accentColor = SharedJourneyColors.MediterraneanTeal,
            screenModel = screenModel,
            snackbarHostState = snackbarHostState,
        )
    }
}
