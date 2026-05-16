package com.example.kmp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Journey(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: JourneyCategory = JourneyCategory.LongTermProjects,
    val progress: Float,
    val participantInitials: List<String>,
    val tasks: List<JourneyTask> = emptyList(),
    val familyStreak: Int = 0,
    val specificGoal: String? = null,
    val measurableOutcome: String? = null,
    val achievablePlan: String? = null,
    val relevanceToFamily: String? = null,
    val timeBoundDeadline: String? = null,
    val colorHex: String? = null,
)

@Serializable
enum class JourneyCategory(
    val storageKey: String,
    val displayName: String,
    val description: String,
    val defaultColorHex: String,
) {
    CalendarLogistics(
        storageKey = "CALENDAR_LOGISTICS",
        displayName = "Calendar & Logistics",
        description = "Schedules, appointments, events, errands and coordination.",
        defaultColorHex = "005F6B",
    ),
    HouseholdManagement(
        storageKey = "HOUSEHOLD_MANAGEMENT",
        displayName = "Household Management & Maintenance",
        description = "Cleaning, repairs, home routines, supplies and recurring upkeep.",
        defaultColorHex = "E2725B",
    ),
    MealPlanning(
        storageKey = "MEAL_PLANNING",
        displayName = "Meal Planning & Kitchen Operations",
        description = "Menus, grocery planning, cooking prep and kitchen inventory.",
        defaultColorHex = "F4D03F",
    ),
    HealthWellness(
        storageKey = "HEALTH_WELLNESS",
        displayName = "Health, Wellness & Self-Care",
        description = "Habits, appointments, movement, rest and personal care.",
        defaultColorHex = "6B9A5E",
    ),
    LongTermProjects(
        storageKey = "LONG_TERM_PROJECTS",
        displayName = "Long-Term Projects & Life Milestones",
        description = "Big goals, milestones, plans and longer horizon projects.",
        defaultColorHex = "8E6BBE",
    );

    companion object {
        fun fromStorageKey(value: String?): JourneyCategory {
            return entries.firstOrNull { it.storageKey == value } ?: LongTermProjects
        }
    }
}

@Serializable
data class JourneyTask(
    val id: String,
    val journeyId: String,
    val title: String,
    val planning: String,
    val isCompleted: Boolean,
    val label: String,
    val scheduledDays: List<Int> = emptyList(),
    val reminderTime: String? = null,
    val claimedByInitials: String? = null,
    val cheersCount: Int = 0,
)

@Serializable
data class SmartGoalProposal(
    val title: String,
    val subtitle: String,
    val specific: String,
    val measurable: String,
    val achievable: String,
    val relevant: String,
    val timeBound: String,
    val suggestedTasks: List<String>
)

sealed class ArchitectState {
    object Idle : ArchitectState()
    object Refining : ArchitectState()
    data class Proposed(val proposal: SmartGoalProposal) : ArchitectState()
    data class Error(val message: String) : ArchitectState()
}
