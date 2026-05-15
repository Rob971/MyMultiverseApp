package com.example.kmp.presentation.screens.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.kmp.domain.model.Greeting
import com.example.kmp.domain.usecase.GetGreetingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getGreetingUseCase: GetGreetingUseCase,
) : ScreenModel {
    private val _greeting = MutableStateFlow<Greeting?>(null)
    val greeting: StateFlow<Greeting?> = _greeting.asStateFlow()

    private val _journeys =
        MutableStateFlow(
            listOf(
                JourneyDreamUi(
                    id = "vesuvian-vitality",
                    title = "Vesuvian Vitality",
                    subtitle = "Salvatore & Anna's Healthy Nutritional Plan",
                    progress = 0.35f,
                    familyStreak = 12,
                    participantInitials = listOf("S", "A"),
                    specificGoal = "Transform daily nutrition and activity using the 'Italia Portici' Mediterranean framework.",
                    measurableOutcome = "5 core habits tracked weekly: Market Shopping, 50/25/25 Ratios, Moderation, Active Habits, and Prep.",
                    achievablePlan = "Starting with gradual changes: transition to sugar-free espresso over 7 days and Saturday-only market visits.",
                    relevanceToFamily = "To ensure long-term 'Salute e Benessere' for Salvatore, Anna, and the future of their family.",
                    timeBoundDeadline = "90-Day Initial Vitality Phase",
                    colorHex = "E2725B",
                    tasks = listOf(
                        JourneyTaskUi(
                            id = "v-1",
                            title = "La Spesa (Vesuvian Shopping)",
                            planning = "Visit Mercato di Portici every Saturday morning. Prioritize Piennolo tomatoes, Friarielli, and fresh Pesce Azzurro.",
                            isCompleted = true,
                            label = "Weekly",
                            scheduledDays = listOf(6),
                            reminderTime = "09:00",
                            claimedByInitials = "S",
                            cheersCount = 5
                        ),
                        JourneyTaskUi(
                            id = "v-2",
                            title = "Daily Ratios (50/25/25 Balance)",
                            planning = "Ensure half the plate is veg/fruit. Start with dinner only, then expand to lunch once consistent.",
                            isCompleted = false,
                            label = "Daily",
                            scheduledDays = listOf(1, 2, 3, 4, 5, 6, 7),
                            reminderTime = "19:00",
                            claimedByInitials = "A",
                            cheersCount = 2
                        ),
                        JourneyTaskUi(
                            id = "v-3",
                            title = "Dolce Vita Strategy",
                            planning = "Weekly Pizza Marinara on Fridays. Transition to sugar-free espresso over 7 days. Sunday Sfogliatella reward.",
                            isCompleted = true,
                            label = "Mixed",
                            scheduledDays = listOf(5, 7),
                            reminderTime = "20:30",
                            cheersCount = 12
                        ),
                        JourneyTaskUi(
                            id = "v-4",
                            title = "Porticese Lifestyle",
                            planning = "Daily sunset walk at Reggia di Portici. Tuesday/Thursday waterfront jogs at Granatello.",
                            isCompleted = false,
                            label = "Daily",
                            scheduledDays = listOf(1, 2, 3, 4, 5, 6, 7),
                            reminderTime = "18:00",
                            claimedByInitials = "S",
                            cheersCount = 4
                        ),
                        JourneyTaskUi(
                            id = "v-5",
                            title = "Logistics & Prep",
                            planning = "Sunday batch-cooking for beans and whole-grain pasta. Keep Acqua della Madonna in reusable bottles.",
                            isCompleted = false,
                            label = "Weekly",
                            scheduledDays = listOf(7),
                            reminderTime = "10:30",
                            cheersCount = 1
                        )
                    )
                ),
                JourneyDreamUi(
                    id = "financial-masterplan",
                    title = "Financial Masterplan",
                    subtitle = "Costruiamo il Nostro Futuro Qui (Napoli)",
                    progress = 0.20f,
                    familyStreak = 5,
                    participantInitials = listOf("S", "A"),
                    specificGoal = "Secure a $10k home down payment in Naples and manage core categories (Food, Utilities, Activities).",
                    measurableOutcome = "Monthly reviews, categorized 'log-as-you-go' tracking, and automated single transfers to a measurable modern fund.",
                    achievablePlan = "Implement a model (50/50 Equal Split, Percentage Proportional, or Hybrid Mix & Match) for healthy savings.",
                    relevanceToFamily = "Ensures family well-being, transparency (no secrets), and value-driven decisions built on respect and trust.",
                    timeBoundDeadline = "Annual Roadmap with assigned dates to achieve Utilities, Food, Trips, Gym, and Nature Walks.",
                    colorHex = "C9A66B",
                    tasks = listOf(
                        JourneyTaskUi(
                            id = "f-1",
                            title = "Monthly Check-in",
                            planning = "Define exactly what we want for the next 30 days. Assign dates to achieve goals.",
                            isCompleted = true,
                            label = "Monthly",
                            scheduledDays = listOf(1),
                            reminderTime = "18:00",
                            claimedByInitials = "S",
                            cheersCount = 3
                        ),
                        JourneyTaskUi(
                            id = "f-2",
                            title = "Log as You Go",
                            planning = "Categorize daily spending into Utilities, Food, and Activities (Gym, Nature walks).",
                            isCompleted = false,
                            label = "Daily",
                            scheduledDays = listOf(1, 2, 3, 4, 5, 6, 7),
                            reminderTime = "21:00",
                            claimedByInitials = "A",
                            cheersCount = 8
                        ),
                        JourneyTaskUi(
                            id = "f-3",
                            title = "Secure Transfer",
                            planning = "Execute the single monthly transfer to the Home Down Payment fund (Napoli future).",
                            isCompleted = false,
                            label = "Monthly",
                            scheduledDays = listOf(5),
                            reminderTime = "10:00",
                            cheersCount = 15
                        ),
                        JourneyTaskUi(
                            id = "f-4",
                            title = "Annual Roadmap Review",
                            planning = "Review Sinking Funds: Vacation, Gifts, Car Insurance, and House maintenance.",
                            isCompleted = true,
                            label = "Weekly",
                            scheduledDays = listOf(7),
                            reminderTime = "11:00",
                            cheersCount = 2
                        ),
                        JourneyTaskUi(
                            id = "f-5",
                            title = "Transparency Talk",
                            planning = "Brief weekly talk to ensure no secrets and respect in all financial choices.",
                            isCompleted = false,
                            label = "Weekly",
                            scheduledDays = listOf(4),
                            reminderTime = "20:00",
                            claimedByInitials = "S",
                            cheersCount = 6
                        )
                    )
                ),
                JourneyDreamUi(
                    id = "motore-unita",
                    title = "Il Motore dell'Unità",
                    subtitle = "Maria & Antonio's Dolce Vita Sana",
                    progress = 0.50f,
                    familyStreak = 8,
                    participantInitials = listOf("M", "A"),
                    specificGoal = "Build functional strength, endurance, and heart health through seasonal outdoor activities in Naples.",
                    measurableOutcome = "Quarterly focus shifts (Strength, Sea, Endurance, Balance) with specific 2025 targets.",
                    achievablePlan = "Follow the '80% Rule' (va bene!) to maintain consistency over perfection in our training.",
                    relevanceToFamily = "Strengthening the 'Motor of Unity' and Love through shared movement and healthy habits.",
                    timeBoundDeadline = "2025 Seasonal Roadmap (Kayak Gaiola Target)",
                    colorHex = "4F7942",
                    tasks = listOf(
                        JourneyTaskUi(
                            id = "m-1",
                            title = "Primavera: Forza e Salute",
                            planning = "3x weekly functional strength at the gym or Crossfit. Focus on muscle tone and mobility.",
                            isCompleted = true,
                            label = "Weekly",
                            scheduledDays = listOf(1, 3, 5),
                            reminderTime = "07:30",
                            claimedByInitials = "A",
                            cheersCount = 12
                        ),
                        JourneyTaskUi(
                            id = "m-2",
                            title = "Estate: Energia e Mare",
                            planning = "Open water swimming at Posillipo and running along the Lungomare. Prepare for Kayak Gaiola.",
                            isCompleted = false,
                            label = "Daily",
                            scheduledDays = listOf(2, 4, 6, 7),
                            reminderTime = "08:00",
                            cheersCount = 20
                        ),
                        JourneyTaskUi(
                            id = "m-3",
                            title = "Autunno: Cuore e Resistenza",
                            planning = "Weekend hiking at Vesuvius and Sentiero degli Dei to build long-distance endurance.",
                            isCompleted = false,
                            label = "Weekly",
                            scheduledDays = listOf(6, 7),
                            reminderTime = "09:00",
                            claimedByInitials = "M",
                            cheersCount = 8
                        ),
                        JourneyTaskUi(
                            id = "m-4",
                            title = "Inverno: Equilibrio e Piacere",
                            planning = "Fresh market visits and cultural breakfasts (no sugar coffee). Maintain rest day walks.",
                            isCompleted = true,
                            label = "Daily",
                            scheduledDays = listOf(1, 2, 3, 4, 5, 6, 7),
                            reminderTime = "08:30",
                            cheersCount = 4
                        ),
                        JourneyTaskUi(
                            id = "m-5",
                            title = "Emulazione Supporto (80% Rule)",
                            planning = "Daily support check-in. If we only hit 80% today? Va bene! Consistency is key.",
                            isCompleted = true,
                            label = "Daily",
                            scheduledDays = listOf(1, 2, 3, 4, 5, 6, 7),
                            reminderTime = "21:30",
                            claimedByInitials = "A",
                            cheersCount = 15
                        )
                    )
                )
            ),
        )
    val journeys: StateFlow<List<JourneyDreamUi>> = _journeys.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launch {
            _greeting.value = getGreetingUseCase()
        }
    }

    fun toggleTask(journeyId: String, taskId: String) {
        val currentJourneys = _journeys.value
        val updatedJourneys = currentJourneys.map { journey ->
            if (journey.id == journeyId) {
                val newTasks = journey.tasks.map { task ->
                    if (task.id == taskId) {
                        task.copy(isCompleted = !task.isCompleted)
                    } else task
                }
                val completedCount = newTasks.count { it.isCompleted }
                journey.copy(
                    tasks = newTasks,
                    progress = completedCount.toFloat() / newTasks.size
                )
            } else journey
        }
        _journeys.value = updatedJourneys
    }

    fun cheerTask(journeyId: String, taskId: String) {
        val currentJourneys = _journeys.value
        val updatedJourneys = currentJourneys.map { journey ->
            if (journey.id == journeyId) {
                journey.copy(tasks = journey.tasks.map { task ->
                    if (task.id == taskId) {
                        task.copy(cheersCount = task.cheersCount + 1)
                    } else task
                })
            } else journey
        }
        _journeys.value = updatedJourneys
    }
}
