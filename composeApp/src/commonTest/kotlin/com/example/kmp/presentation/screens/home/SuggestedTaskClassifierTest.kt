package com.example.kmp.presentation.screens.home

import com.example.kmp.domain.model.JourneyCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class SuggestedTaskClassifierTest {

    private val copy = SuggestedTaskCopy(
        grocery = "Grocery",
        prep = "Prep",
        lunch = "Lunch",
        menu = "Menu",
        splitRule = "Split Rule",
        settle = "Settle",
        goal = "Goal",
        ledger = "Ledger",
        deEscalator = "De-escalator",
        dateNight = "Date Night",
        checkIn = "Check-In",
        care = "Care",
        plan = "Plan",
        budget = "Budget",
        friction = "Friction",
        roles = "Roles",
        followUp = "Follow-up",
        nextAction = "Next Action",
        groceryPlanning = "grocery planning",
        prepPlanning = "prep planning",
        menuPlanning = "menu planning",
        splitRulePlanning = "split planning",
        settlePlanning = "settle planning",
        ledgerPlanning = "ledger planning",
        deEscalatorPlanning = "de-escalator planning",
        dateNightPlanning = "date night planning",
        checkInPlanning = "check-in planning",
        categoryDefaultPlanning = "category default planning",
        longTermNextActionPlanning = "next action planning",
        longTermBudgetPlanning = "budget planning",
        longTermFrictionPlanning = "friction planning",
        longTermRolesPlanning = "roles planning",
        longTermFollowUpPlanning = "follow-up planning",
        longTermPlanPlanning = "project plan planning",
    )

    @Test
    fun mealPlanningTasksMapToMealSpecificSections() {
        assertEquals(SuggestedTaskSection.Grocery, suggestedTaskSection(JourneyCategory.MealPlanning, "Grocery list for local shops"))
        assertEquals(SuggestedTaskSection.Prep, suggestedTaskSection(JourneyCategory.MealPlanning, "Weekend prep: batch-cook rice"))
        assertEquals(SuggestedTaskSection.Lunch, suggestedTaskSection(JourneyCategory.MealPlanning, "Scale leftovers for lunch"))
        assertEquals(SuggestedTaskSection.Menu, suggestedTaskSection(JourneyCategory.MealPlanning, "Monday: pantry pasta"))
    }

    @Test
    fun financeTasksMapToLedgerWorkflowSections() {
        assertEquals(SuggestedTaskSection.SplitRule, suggestedTaskSection(JourneyCategory.HouseholdFinance, "Apply split rule: 60/40"))
        assertEquals(SuggestedTaskSection.Settle, suggestedTaskSection(JourneyCategory.HouseholdFinance, "Send smart-settle reminder"))
        assertEquals(SuggestedTaskSection.Goal, suggestedTaskSection(JourneyCategory.HouseholdFinance, "Track the primary goal"))
        assertEquals(SuggestedTaskSection.Ledger, suggestedTaskSection(JourneyCategory.HouseholdFinance, "Create ledger categories"))
    }

    @Test
    fun wellnessTasksMapToRelationshipSections() {
        assertEquals(SuggestedTaskSection.DeEscalator, suggestedTaskSection(JourneyCategory.HealthWellness, "Send the de-escalated message"))
        assertEquals(SuggestedTaskSection.DateNight, suggestedTaskSection(JourneyCategory.HealthWellness, "Plan the at-home date"))
        assertEquals(SuggestedTaskSection.CheckIn, suggestedTaskSection(JourneyCategory.HealthWellness, "Run the temperature check again"))
        assertEquals(SuggestedTaskSection.Care, suggestedTaskSection(JourneyCategory.HealthWellness, "Do one appreciation action"))
    }

    @Test
    fun longTermProjectTasksMapToProjectSections() {
        assertEquals(SuggestedTaskSection.NextAction, suggestedTaskSection(JourneyCategory.LongTermProjects, "Today: write the finish line"))
        assertEquals(SuggestedTaskSection.Budget, suggestedTaskSection(JourneyCategory.LongTermProjects, "Set the budget guardrail"))
        assertEquals(SuggestedTaskSection.Friction, suggestedTaskSection(JourneyCategory.LongTermProjects, "Name the current roadblock"))
        assertEquals(SuggestedTaskSection.Roles, suggestedTaskSection(JourneyCategory.LongTermProjects, "Assign owners"))
        assertEquals(SuggestedTaskSection.FollowUp, suggestedTaskSection(JourneyCategory.LongTermProjects, "Schedule the first check-in"))
        assertEquals(SuggestedTaskSection.Plan, suggestedTaskSection(JourneyCategory.LongTermProjects, "Create a three-column board"))
    }

    @Test
    fun localizedCopyIsUsedForPersistedLabelsAndPlanningText() {
        assertEquals("Grocery", suggestedTaskLabel(JourneyCategory.MealPlanning, "Grocery list", copy))
        assertEquals("grocery planning", suggestedTaskPlanning(JourneyCategory.MealPlanning, "Grocery list", copy))
        assertEquals("Split Rule", suggestedTaskLabel(JourneyCategory.HouseholdFinance, "Apply split rule", copy))
        assertEquals("split planning", suggestedTaskPlanning(JourneyCategory.HouseholdFinance, "Apply split rule", copy))
        assertEquals("Next Action", suggestedTaskLabel(JourneyCategory.LongTermProjects, "Today: pick one action", copy))
        assertEquals("next action planning", suggestedTaskPlanning(JourneyCategory.LongTermProjects, "Today: pick one action", copy))
    }
}
