package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.runtime.Composable
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*

@Composable
fun wellnessLoveLanguageOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_LOVE_ACTS, Res.string.wellness_opt_love_acts),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_LOVE_WORDS, Res.string.wellness_opt_love_words),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_LOVE_QUALITY, Res.string.wellness_opt_love_quality),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_LOVE_TOUCH, Res.string.wellness_opt_love_touch),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_LOVE_GIFTS, Res.string.wellness_opt_love_gifts),
)

@Composable
fun wellnessTimeOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_TIME_5, Res.string.wellness_opt_time_5),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_TIME_15, Res.string.wellness_opt_time_15),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_TIME_30, Res.string.wellness_opt_time_30),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_TIME_60, Res.string.wellness_opt_time_60),
)

@Composable
fun wellnessBudgetOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_BUDGET_0, Res.string.wellness_opt_budget_0),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_BUDGET_10, Res.string.wellness_opt_budget_10),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_BUDGET_25, Res.string.wellness_opt_budget_25),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_BUDGET_50, Res.string.wellness_opt_budget_50),
)

@Composable
fun wellnessDrainOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_DRAIN_WORK, Res.string.wellness_opt_drain_work),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_DRAIN_KIDS, Res.string.wellness_opt_drain_kids),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_DRAIN_CHORES, Res.string.wellness_opt_drain_chores),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_DRAIN_HEALTH, Res.string.wellness_opt_drain_health),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_DRAIN_MONEY, Res.string.wellness_opt_drain_money),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_DRAIN_FAMILY, Res.string.wellness_opt_drain_family),
)

@Composable
fun wellnessDurationOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_DURATION_30, Res.string.wellness_opt_duration_30),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_DURATION_60, Res.string.wellness_opt_duration_60),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_DURATION_120, Res.string.wellness_opt_duration_120),
    LocalizedSelectOption(WellnessQuestionnaireValues.OPT_DURATION_EVENING, Res.string.wellness_opt_duration_evening),
)
