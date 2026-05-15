package com.example.kmp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Journey(
    val id: String,
    val title: String,
    val subtitle: String,
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
