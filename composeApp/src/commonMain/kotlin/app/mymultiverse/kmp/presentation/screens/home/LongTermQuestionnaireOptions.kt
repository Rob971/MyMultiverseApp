package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.runtime.Composable
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*

@Composable
fun longTermMilestoneOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_MILESTONE_MOVE, Res.string.longterm_opt_milestone_move),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_MILESTONE_RENO, Res.string.longterm_opt_milestone_reno),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_MILESTONE_EVENT, Res.string.longterm_opt_milestone_event),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_MILESTONE_TRANSITION, Res.string.longterm_opt_milestone_transition),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_MILESTONE_DECLUTTER, Res.string.longterm_opt_milestone_declutter),
)

@Composable
fun longTermRoadblockOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_ROADBLOCK_TIME, Res.string.longterm_opt_roadblock_time),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_ROADBLOCK_ALIGNMENT, Res.string.longterm_opt_roadblock_alignment),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_ROADBLOCK_OVERWHELMED, Res.string.longterm_opt_roadblock_overwhelmed),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_ROADBLOCK_ACCOUNTABILITY, Res.string.longterm_opt_roadblock_accountability),
)

@Composable
fun longTermTimelineOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_TIMELINE_ASAP, Res.string.longterm_opt_timeline_asap),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_TIMELINE_MEDIUM, Res.string.longterm_opt_timeline_medium),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_TIMELINE_LONG, Res.string.longterm_opt_timeline_long),
)

@Composable
fun longTermBudgetStyleOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_BUDGET_TIGHT, Res.string.longterm_opt_budget_tight),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_BUDGET_MODERATE, Res.string.longterm_opt_budget_moderate),
    LocalizedSelectOption(LongTermQuestionnaireValues.OPT_BUDGET_FULL, Res.string.longterm_opt_budget_full),
)
