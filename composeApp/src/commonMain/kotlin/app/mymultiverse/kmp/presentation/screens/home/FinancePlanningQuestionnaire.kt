package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FinancePlanningQuestionnaire(
    financeSplit: String,
    onFinanceSplitChange: (String) -> Unit,
    billManager: String,
    onBillManagerChange: (String) -> Unit,
    dailyAnnoyance: String,
    onDailyAnnoyanceChange: (String) -> Unit,
    partnerASpendingStyle: String,
    onPartnerASpendingStyleChange: (String) -> Unit,
    partnerBSpendingStyle: String,
    onPartnerBSpendingStyleChange: (String) -> Unit,
    moneyTalkFrequency: String,
    onMoneyTalkFrequencyChange: (String) -> Unit,
    primaryGoal: String,
    onPrimaryGoalChange: (String) -> Unit,
    irregularExpensePlan: String,
    onIrregularExpensePlanChange: (String) -> Unit,
    billSplitStrategy: String,
    onBillSplitStrategyChange: (String) -> Unit,
    settleWorkflow: String,
    onSettleWorkflowChange: (String) -> Unit,
    recurringBills: List<String>,
    onRecurringBillsChange: (List<String>) -> Unit,
    billPainPoint: String,
    onBillPainPointChange: (String) -> Unit,
    partnerAIncome: String,
    onPartnerAIncomeChange: (String) -> Unit,
    partnerBIncome: String,
    onPartnerBIncomeChange: (String) -> Unit,
    customSplitPercentages: String,
    onCustomSplitPercentagesChange: (String) -> Unit,
    monthlyHousingSpend: String,
    onMonthlyHousingSpendChange: (String) -> Unit,
    monthlyUtilitiesSpend: String,
    onMonthlyUtilitiesSpendChange: (String) -> Unit,
    monthlyConnectivitySpend: String,
    onMonthlyConnectivitySpendChange: (String) -> Unit,
    monthlySubscriptionsSpend: String,
    onMonthlySubscriptionsSpendChange: (String) -> Unit,
    monthlyInsuranceSpend: String,
    onMonthlyInsuranceSpendChange: (String) -> Unit,
    monthlyKidsPetsSpend: String,
    onMonthlyKidsPetsSpendChange: (String) -> Unit,
    monthlyOtherSpend: String,
    onMonthlyOtherSpendChange: (String) -> Unit,
    onGenerateFinancialBlueprint: () -> Unit,
) {
    val billSplitStrategyOptions = financeBillSplitStrategyOptions()
    val settleWorkflowOptions = financeSettleWorkflowOptions()
    val recurringBillOptions = financeRecurringBillOptions()
    val billPainPointOptions = financeBillPainPointOptions()
    val financeSplitOptions = financeSplitOptions()
    val billManagerOptions = financeBillManagerOptions()
    val annoyanceOptions = financeAnnoyanceOptions()
    val spendingScaleOptions = financeSpendingScaleOptions()
    val moneyTalkOptions = financeMoneyTalkOptions()
    val goalOptions = financeGoalOptions()
    val irregularExpenseOptions = financeIrregularExpenseOptions()
    val monthlySpendTotal = listOf(
        monthlyHousingSpend,
        monthlyUtilitiesSpend,
        monthlyConnectivitySpend,
        monthlySubscriptionsSpend,
        monthlyInsuranceSpend,
        monthlyKidsPetsSpend,
        monthlyOtherSpend,
    ).sumOf { it.toAmountValue() }
    val canGenerateBlueprint = billSplitStrategy.isNotBlank() &&
        settleWorkflow.isNotBlank() &&
        recurringBills.isNotEmpty() &&
        billPainPoint.isNotBlank() &&
        (billSplitStrategy != FinanceQuestionnaireValues.SPLIT_PROPORTIONAL ||
            (partnerAIncome.isNotBlank() && partnerBIncome.isNotBlank())) &&
        (billSplitStrategy != FinanceQuestionnaireValues.SPLIT_CUSTOM ||
            customSplitPercentages.isNotBlank()) &&
        financeSplit.isNotBlank() &&
        billManager.isNotBlank() &&
        dailyAnnoyance.isNotBlank() &&
        partnerASpendingStyle.isNotBlank() &&
        partnerBSpendingStyle.isNotBlank() &&
        moneyTalkFrequency.isNotBlank() &&
        primaryGoal.isNotBlank() &&
        irregularExpensePlan.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(Res.string.finance_q_assessment_title),
            style = MaterialTheme.typography.titleMedium,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black,
        )
        Text(
            stringResource(Res.string.finance_q_assessment_desc),
            style = MaterialTheme.typography.bodySmall,
            color = SharedJourneyColors.InkMuted,
        )

        Text(
            stringResource(Res.string.finance_q_bill_tracking_title),
            style = MaterialTheme.typography.titleMedium,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black,
        )

        LocalizedQuestionBlock(
            title = Res.string.finance_q_split_expenses_title,
            description = Res.string.finance_q_split_expenses_desc,
        ) {
            LocalizedOptionChips(
                options = billSplitStrategyOptions,
                selectedValue = billSplitStrategy,
                onSelected = onBillSplitStrategyChange,
            )
        }

        if (billSplitStrategy == FinanceQuestionnaireValues.SPLIT_PROPORTIONAL) {
            LocalizedQuestionBlock(
                title = Res.string.finance_q_partner_incomes_title,
                description = Res.string.finance_q_partner_incomes_desc,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = partnerAIncome,
                        onValueChange = onPartnerAIncomeChange,
                        placeholder = { Text(stringResource(Res.string.finance_q_partner_a_income_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = questionnaireTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                    )
                    TextField(
                        value = partnerBIncome,
                        onValueChange = onPartnerBIncomeChange,
                        placeholder = { Text(stringResource(Res.string.finance_q_partner_b_income_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = questionnaireTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            }
        }

        if (billSplitStrategy == FinanceQuestionnaireValues.SPLIT_CUSTOM) {
            LocalizedQuestionBlock(
                title = Res.string.finance_q_fixed_split_title,
                description = Res.string.finance_q_fixed_split_desc,
            ) {
                TextField(
                    value = customSplitPercentages,
                    onValueChange = onCustomSplitPercentagesChange,
                    placeholder = { Text(stringResource(Res.string.finance_q_fixed_split_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = questionnaireTextFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                )
            }
        }

        LocalizedQuestionBlock(
            title = Res.string.finance_q_settle_title,
            description = Res.string.finance_q_settle_desc,
        ) {
            LocalizedOptionChips(
                options = settleWorkflowOptions,
                selectedValue = settleWorkflow,
                onSelected = onSettleWorkflowChange,
            )
        }

        LocalizedQuestionBlock(
            title = Res.string.finance_q_recurring_bills_title,
            description = Res.string.finance_q_recurring_bills_desc,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                recurringBillOptions.forEach { option ->
                    val isSelected = recurringBills.contains(option.storedValue)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                val next = if (checked) {
                                    recurringBills + option.storedValue
                                } else {
                                    recurringBills - option.storedValue
                                }
                                onRecurringBillsChange(next)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = SharedJourneyColors.MediterraneanTeal),
                        )
                        Text(
                            stringResource(option.label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SharedJourneyColors.InkDeep,
                        )
                    }
                }
            }
        }

        LocalizedQuestionBlock(
            title = Res.string.finance_q_bill_pain_title,
            description = Res.string.finance_q_bill_pain_desc,
        ) {
            LocalizedOptionChips(
                options = billPainPointOptions,
                selectedValue = billPainPoint,
                onSelected = onBillPainPointChange,
            )
        }

        HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

        Text(
            stringResource(Res.string.finance_q_monthly_snapshot_title),
            style = MaterialTheme.typography.titleMedium,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black,
        )
        Text(
            stringResource(Res.string.finance_q_monthly_snapshot_desc),
            style = MaterialTheme.typography.bodySmall,
            color = SharedJourneyColors.InkMuted,
        )

        LocalizedMonthlySpendField(
            label = Res.string.finance_spend_housing,
            value = monthlyHousingSpend,
            onValueChange = onMonthlyHousingSpendChange,
        )
        LocalizedMonthlySpendField(
            label = Res.string.finance_spend_utilities,
            value = monthlyUtilitiesSpend,
            onValueChange = onMonthlyUtilitiesSpendChange,
        )
        LocalizedMonthlySpendField(
            label = Res.string.finance_spend_connectivity,
            value = monthlyConnectivitySpend,
            onValueChange = onMonthlyConnectivitySpendChange,
        )
        LocalizedMonthlySpendField(
            label = Res.string.finance_spend_subscriptions,
            value = monthlySubscriptionsSpend,
            onValueChange = onMonthlySubscriptionsSpendChange,
        )
        LocalizedMonthlySpendField(
            label = Res.string.finance_spend_insurance,
            value = monthlyInsuranceSpend,
            onValueChange = onMonthlyInsuranceSpendChange,
        )
        LocalizedMonthlySpendField(
            label = Res.string.finance_spend_kids_pets,
            value = monthlyKidsPetsSpend,
            onValueChange = onMonthlyKidsPetsSpendChange,
        )
        LocalizedMonthlySpendField(
            label = Res.string.finance_spend_other,
            value = monthlyOtherSpend,
            onValueChange = onMonthlyOtherSpendChange,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                stringResource(
                    Res.string.finance_q_monthly_spend_total,
                    monthlySpendTotal.toCurrencyText(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(14.dp),
                textAlign = TextAlign.Center,
            )
        }

        HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

        Text(
            stringResource(Res.string.finance_q_relationship_title),
            style = MaterialTheme.typography.titleMedium,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black,
        )

        LocalizedQuestionBlock(
            title = Res.string.finance_q_finance_split_title,
            description = Res.string.finance_q_finance_split_desc,
        ) {
            LocalizedOptionChips(
                options = financeSplitOptions,
                selectedValue = financeSplit,
                onSelected = onFinanceSplitChange,
            )
        }

        LocalizedQuestionBlock(
            title = Res.string.finance_q_bill_manager_title,
            description = Res.string.finance_q_bill_manager_desc,
        ) {
            LocalizedOptionChips(
                options = billManagerOptions,
                selectedValue = billManager,
                onSelected = onBillManagerChange,
            )
        }

        LocalizedQuestionBlock(
            title = Res.string.finance_q_daily_annoyance_title,
            description = Res.string.finance_q_daily_annoyance_desc,
        ) {
            LocalizedOptionChips(
                options = annoyanceOptions,
                selectedValue = dailyAnnoyance,
                onSelected = onDailyAnnoyanceChange,
            )
        }

        HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

        Text(
            stringResource(Res.string.finance_q_phase2_title),
            style = MaterialTheme.typography.titleMedium,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black,
        )
        Text(
            stringResource(Res.string.finance_q_phase2_desc),
            style = MaterialTheme.typography.bodySmall,
            color = SharedJourneyColors.InkMuted,
        )

        LocalizedQuestionBlock(
            title = Res.string.finance_q_partner_a_spending_title,
            description = Res.string.finance_q_partner_a_spending_desc,
        ) {
            LocalizedOptionChips(
                options = spendingScaleOptions,
                selectedValue = partnerASpendingStyle,
                onSelected = onPartnerASpendingStyleChange,
            )
        }

        LocalizedQuestionBlock(
            title = Res.string.finance_q_partner_b_spending_title,
            description = Res.string.finance_q_partner_b_spending_desc,
        ) {
            LocalizedOptionChips(
                options = spendingScaleOptions,
                selectedValue = partnerBSpendingStyle,
                onSelected = onPartnerBSpendingStyleChange,
            )
        }

        LocalizedQuestionBlock(
            title = Res.string.finance_q_money_talk_title,
            description = Res.string.finance_q_money_talk_desc,
        ) {
            LocalizedOptionChips(
                options = moneyTalkOptions,
                selectedValue = moneyTalkFrequency,
                onSelected = onMoneyTalkFrequencyChange,
            )
        }

        HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

        Text(
            stringResource(Res.string.finance_q_phase3_title),
            style = MaterialTheme.typography.titleMedium,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black,
        )

        LocalizedQuestionBlock(
            title = Res.string.finance_q_primary_goal_title,
            description = Res.string.finance_q_primary_goal_desc,
        ) {
            LocalizedOptionChips(
                options = goalOptions,
                selectedValue = primaryGoal,
                onSelected = onPrimaryGoalChange,
            )
        }

        LocalizedQuestionBlock(
            title = Res.string.finance_q_irregular_expense_title,
            description = Res.string.finance_q_irregular_expense_desc,
        ) {
            LocalizedOptionChips(
                options = irregularExpenseOptions,
                selectedValue = irregularExpensePlan,
                onSelected = onIrregularExpensePlanChange,
            )
        }

        Button(
            onClick = onGenerateFinancialBlueprint,
            enabled = canGenerateBlueprint,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal),
        ) {
            Icon(AppIcons.Sparkles, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(Res.string.finance_q_generate_button),
                fontWeight = FontWeight.Bold,
            )
        }

        if (!canGenerateBlueprint) {
            Text(
                stringResource(Res.string.finance_q_complete_hint),
                style = MaterialTheme.typography.labelSmall,
                color = SharedJourneyColors.InkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun questionnaireTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
    unfocusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
    focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
    unfocusedIndicatorColor = Color.Transparent,
)

private fun String.toAmountValue(): Double {
    return filter { it.isDigit() || it == '.' }
        .takeIf { it.isNotBlank() }
        ?.toDoubleOrNull()
        ?: 0.0
}

private fun Double.toCurrencyText(): String {
    return "\$${toInt()}/mo"
}
