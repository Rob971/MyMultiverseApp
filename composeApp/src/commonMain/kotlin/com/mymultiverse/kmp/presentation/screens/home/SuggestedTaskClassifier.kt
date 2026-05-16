package com.mymultiverse.kmp.presentation.screens.home

import com.mymultiverse.kmp.domain.model.JourneyCategory

internal data class SuggestedTaskCopy(
    val grocery: String,
    val prep: String,
    val lunch: String,
    val menu: String,
    val splitRule: String,
    val settle: String,
    val goal: String,
    val ledger: String,
    val deEscalator: String,
    val dateNight: String,
    val checkIn: String,
    val care: String,
    val plan: String,
    val budget: String,
    val friction: String,
    val roles: String,
    val followUp: String,
    val nextAction: String,
    val groceryPlanning: String,
    val prepPlanning: String,
    val menuPlanning: String,
    val splitRulePlanning: String,
    val settlePlanning: String,
    val ledgerPlanning: String,
    val deEscalatorPlanning: String,
    val dateNightPlanning: String,
    val checkInPlanning: String,
    val categoryDefaultPlanning: String,
    val longTermNextActionPlanning: String,
    val longTermBudgetPlanning: String,
    val longTermFrictionPlanning: String,
    val longTermRolesPlanning: String,
    val longTermFollowUpPlanning: String,
    val longTermPlanPlanning: String,
)

internal enum class SuggestedTaskSection {
    Grocery,
    Prep,
    Lunch,
    Menu,
    SplitRule,
    Settle,
    Goal,
    Ledger,
    DeEscalator,
    DateNight,
    CheckIn,
    Care,
    NextAction,
    Budget,
    Friction,
    Roles,
    FollowUp,
    Plan,
}

internal fun suggestedTaskLabel(category: JourneyCategory, taskTitle: String, copy: SuggestedTaskCopy): String {
    return when (suggestedTaskSection(category, taskTitle)) {
        SuggestedTaskSection.Grocery -> copy.grocery
        SuggestedTaskSection.Prep -> copy.prep
        SuggestedTaskSection.Lunch -> copy.lunch
        SuggestedTaskSection.Menu -> copy.menu
        SuggestedTaskSection.SplitRule -> copy.splitRule
        SuggestedTaskSection.Settle -> copy.settle
        SuggestedTaskSection.Goal -> copy.goal
        SuggestedTaskSection.Ledger -> copy.ledger
        SuggestedTaskSection.DeEscalator -> copy.deEscalator
        SuggestedTaskSection.DateNight -> copy.dateNight
        SuggestedTaskSection.CheckIn -> copy.checkIn
        SuggestedTaskSection.Care -> copy.care
        SuggestedTaskSection.NextAction -> copy.nextAction
        SuggestedTaskSection.Budget -> copy.budget
        SuggestedTaskSection.Friction -> copy.friction
        SuggestedTaskSection.Roles -> copy.roles
        SuggestedTaskSection.FollowUp -> copy.followUp
        SuggestedTaskSection.Plan -> copy.plan
    }
}

internal fun suggestedTaskPlanning(category: JourneyCategory, taskTitle: String, copy: SuggestedTaskCopy): String {
    return when (suggestedTaskSection(category, taskTitle)) {
        SuggestedTaskSection.Grocery -> copy.groceryPlanning
        SuggestedTaskSection.Prep -> copy.prepPlanning
        SuggestedTaskSection.Menu -> copy.menuPlanning
        SuggestedTaskSection.SplitRule -> copy.splitRulePlanning
        SuggestedTaskSection.Settle -> copy.settlePlanning
        SuggestedTaskSection.Ledger -> copy.ledgerPlanning
        SuggestedTaskSection.DeEscalator -> copy.deEscalatorPlanning
        SuggestedTaskSection.DateNight -> copy.dateNightPlanning
        SuggestedTaskSection.CheckIn -> copy.checkInPlanning
        SuggestedTaskSection.NextAction -> copy.longTermNextActionPlanning
        SuggestedTaskSection.Budget -> copy.longTermBudgetPlanning
        SuggestedTaskSection.Friction -> copy.longTermFrictionPlanning
        SuggestedTaskSection.Roles -> copy.longTermRolesPlanning
        SuggestedTaskSection.FollowUp -> copy.longTermFollowUpPlanning
        SuggestedTaskSection.Plan -> copy.longTermPlanPlanning
        SuggestedTaskSection.Lunch,
        SuggestedTaskSection.Goal,
        SuggestedTaskSection.Care -> copy.categoryDefaultPlanning
    }
}

internal fun suggestedTaskSection(category: JourneyCategory, taskTitle: String): SuggestedTaskSection {
    if (category != JourneyCategory.LongTermProjects) {
        return when (category) {
            JourneyCategory.MealPlanning -> when {
                taskTitle.contains("grocery", ignoreCase = true) -> SuggestedTaskSection.Grocery
                taskTitle.contains("prep", ignoreCase = true) ||
                    taskTitle.contains("batch", ignoreCase = true) -> SuggestedTaskSection.Prep
                taskTitle.contains("lunch", ignoreCase = true) -> SuggestedTaskSection.Lunch
                else -> SuggestedTaskSection.Menu
            }
            JourneyCategory.HouseholdFinance -> when {
                taskTitle.contains("split", ignoreCase = true) -> SuggestedTaskSection.SplitRule
                taskTitle.contains("settle", ignoreCase = true) ||
                    taskTitle.contains("reminder", ignoreCase = true) -> SuggestedTaskSection.Settle
                taskTitle.contains("goal", ignoreCase = true) -> SuggestedTaskSection.Goal
                else -> SuggestedTaskSection.Ledger
            }
            JourneyCategory.HealthWellness -> when {
                taskTitle.contains("message", ignoreCase = true) ||
                    taskTitle.contains("de-escalated", ignoreCase = true) -> SuggestedTaskSection.DeEscalator
                taskTitle.contains("date", ignoreCase = true) -> SuggestedTaskSection.DateNight
                taskTitle.contains("check", ignoreCase = true) -> SuggestedTaskSection.CheckIn
                else -> SuggestedTaskSection.Care
            }
            else -> SuggestedTaskSection.Plan
        }
    }

    return when {
        taskTitle.contains("Today", ignoreCase = true) -> SuggestedTaskSection.NextAction
        taskTitle.contains("budget", ignoreCase = true) -> SuggestedTaskSection.Budget
        taskTitle.contains("roadblock", ignoreCase = true) -> SuggestedTaskSection.Friction
        taskTitle.contains("owner", ignoreCase = true) ||
            taskTitle.contains("Assign", ignoreCase = true) -> SuggestedTaskSection.Roles
        taskTitle.contains("check-in", ignoreCase = true) ||
            taskTitle.contains("cadence", ignoreCase = true) -> SuggestedTaskSection.FollowUp
        taskTitle.contains("board", ignoreCase = true) ||
            taskTitle.contains("micro-task", ignoreCase = true) -> SuggestedTaskSection.Plan
        else -> SuggestedTaskSection.Plan
    }
}
