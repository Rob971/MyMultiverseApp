package com.example.kmp.domain.model

data class JourneyCategoryExperience(
    val dreamPrompt: String,
    val outcome: String,
    val questionsIntro: String,
    val aiActionLabel: String,
    val planReviewTitle: String,
)

val JourneyCategory.experience: JourneyCategoryExperience
    get() = when (this) {
        JourneyCategory.CalendarLogistics -> JourneyCategoryExperience(
            dreamPrompt = "Coordinate a week, event, appointment rhythm, or errands without one partner carrying the calendar.",
            outcome = "A shared logistics plan with owners, reminders, and the next schedule decision.",
            questionsIntro = "Answer the coordination questions so the AI can separate dates, owners, reminders, and dependencies.",
            aiActionLabel = "Generate logistics plan",
            planReviewTitle = "Calendar Coordination Plan",
        )
        JourneyCategory.HouseholdManagement -> JourneyCategoryExperience(
            dreamPrompt = "Reset a household routine, maintenance backlog, cleaning rhythm, or supplies system.",
            outcome = "A practical home operations plan with recurring routines and fair ownership.",
            questionsIntro = "Answer the home context questions so the AI can balance effort, cadence, and ownership.",
            aiActionLabel = "Generate household operations plan",
            planReviewTitle = "Household Operations Plan",
        )
        JourneyCategory.MealPlanning -> JourneyCategoryExperience(
            dreamPrompt = "Plan meals, groceries, prep blocks, or fridge-clearance ideas that fit real household constraints.",
            outcome = "A weekly meal plan with grocery guidance, prep blocks, and realistic cooking constraints.",
            questionsIntro = "Answer the meal questions so the AI can respect allergies, time, portions, and local shopping context.",
            aiActionLabel = "Generate weekly meal plan",
            planReviewTitle = "Meal Plan",
        )
        JourneyCategory.HouseholdFinance -> JourneyCategoryExperience(
            dreamPrompt = "Reduce money stress, clarify bill ownership, split costs fairly, or create a shared spending rhythm.",
            outcome = "A household finance blueprint with split rules, bill cadence, spending snapshot, and settle-up workflow.",
            questionsIntro = "Answer the finance questions so the AI can build a neutral ledger, not a generic budget.",
            aiActionLabel = "Generate financial blueprint",
            planReviewTitle = "Financial Blueprint",
        )
        JourneyCategory.HealthWellness -> JourneyCategoryExperience(
            dreamPrompt = "Improve connection, translate conflict, plan care gestures, or protect energy as a couple.",
            outcome = "A couples wellness plan with a de-escalation script, care action, radar, and next check-in.",
            questionsIntro = "Answer the wellness questions so the AI can coach tone, timing, and support without taking sides.",
            aiActionLabel = "Generate couples wellness plan",
            planReviewTitle = "Couples Wellness Plan",
        )
        JourneyCategory.LongTermProjects -> JourneyCategoryExperience(
            dreamPrompt = "Move a big life milestone forward without overwhelm, unclear roles, or decision fatigue.",
            outcome = "A milestone roadmap with decisions, owners, budget guardrails, and next best actions.",
            questionsIntro = "Answer the project questions so the AI can turn the milestone into a staged, trackable roadmap.",
            aiActionLabel = "Generate milestone blueprint",
            planReviewTitle = "Milestone Blueprint",
        )
    }
