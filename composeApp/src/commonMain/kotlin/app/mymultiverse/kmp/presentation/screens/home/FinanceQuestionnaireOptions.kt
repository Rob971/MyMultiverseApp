package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.runtime.Composable
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*

@Composable
fun financeBillSplitStrategyOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_SPLIT_50_50, Res.string.finance_opt_split_50_50),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_SPLIT_PROPORTIONAL, Res.string.finance_opt_split_proportional),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_SPLIT_CUSTOM, Res.string.finance_opt_split_custom),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_SPLIT_ASSIGN, Res.string.finance_opt_split_assign),
)

@Composable
fun financeSettleWorkflowOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_SETTLE_VENMO, Res.string.finance_opt_settle_venmo),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_SETTLE_LUMP, Res.string.finance_opt_settle_lump),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_SETTLE_JOINT, Res.string.finance_opt_settle_joint),
)

@Composable
fun financeRecurringBillOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_BILL_HOUSING, Res.string.finance_opt_bill_housing),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_BILL_UTILITIES, Res.string.finance_opt_bill_utilities),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_BILL_CONNECTIVITY, Res.string.finance_opt_bill_connectivity),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_BILL_SUBSCRIPTIONS, Res.string.finance_opt_bill_subscriptions),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_BILL_INSURANCE, Res.string.finance_opt_bill_insurance),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_BILL_KIDS_PETS, Res.string.finance_opt_bill_kids_pets),
)

@Composable
fun financeBillPainPointOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_PAIN_LATE, Res.string.finance_opt_pain_late),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_PAIN_NAGGING, Res.string.finance_opt_pain_nagging),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_PAIN_FLUCTUATE, Res.string.finance_opt_pain_fluctuate),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_PAIN_PM, Res.string.finance_opt_pain_pm),
)

@Composable
fun financeSplitOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_POOL_COMBINED, Res.string.finance_opt_pool_combined),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_POOL_SEPARATE, Res.string.finance_opt_pool_separate),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_POOL_MIX, Res.string.finance_opt_pool_mix),
)

@Composable
fun financeBillManagerOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_MANAGER_A, Res.string.finance_opt_manager_a),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_MANAGER_B, Res.string.finance_opt_manager_b),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_MANAGER_TOGETHER, Res.string.finance_opt_manager_together),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_MANAGER_CHAOTIC, Res.string.finance_opt_manager_chaotic),
)

@Composable
fun financeAnnoyanceOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_ANNOY_BILLS_DUE, Res.string.finance_opt_annoy_bills_due),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_ANNOY_TRACKING, Res.string.finance_opt_annoy_tracking),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_ANNOY_OVERSPEND, Res.string.finance_opt_annoy_overspend),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_ANNOY_ARGUMENTS, Res.string.finance_opt_annoy_arguments),
)

@Composable
fun financeSpendingScaleOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption("1", Res.string.finance_opt_scale_1),
    LocalizedSelectOption("2", Res.string.finance_opt_scale_2),
    LocalizedSelectOption("3", Res.string.finance_opt_scale_3),
    LocalizedSelectOption("4", Res.string.finance_opt_scale_4),
    LocalizedSelectOption("5", Res.string.finance_opt_scale_5),
)

@Composable
fun financeMoneyTalkOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_TALK_WRONG, Res.string.finance_opt_talk_wrong),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_TALK_REGULAR, Res.string.finance_opt_talk_regular),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_TALK_AVOID, Res.string.finance_opt_talk_avoid),
)

@Composable
fun financeGoalOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_GOAL_EMERGENCY, Res.string.finance_opt_goal_emergency),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_GOAL_DEBT, Res.string.finance_opt_goal_debt),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_GOAL_MILESTONE, Res.string.finance_opt_goal_milestone),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_GOAL_PEACE, Res.string.finance_opt_goal_peace),
)

@Composable
fun financeIrregularExpenseOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_IRREGULAR_YES, Res.string.finance_opt_irregular_yes),
    LocalizedSelectOption(FinanceQuestionnaireValues.OPT_IRREGULAR_NO, Res.string.finance_opt_irregular_no),
)
