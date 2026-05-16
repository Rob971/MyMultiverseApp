package com.mymultiverse.kmp.presentation.screens.home

data class JourneyDreamUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val progress: Float,
    val participantInitials: List<String>,
    val tasks: List<JourneyTaskUi> = emptyList(),
    val familyStreak: Int = 0,
    // S.M.A.R.T. Principles
    val specificGoal: String? = null,
    val measurableOutcome: String? = null,
    val achievablePlan: String? = null,
    val relevanceToFamily: String? = null,
    val timeBoundDeadline: String? = null,
    val colorHex: String? = null, // Custom color for the goal
)

data class JourneyTaskUi(
    val id: String,
    val title: String,
    val planning: String,
    val isCompleted: Boolean,
    val label: String,
    val scheduledDays: List<Int> = emptyList(),
    val reminderTime: String? = null,
    val claimedByInitials: String? = null,
    val cheersCount: Int = 0,
)
