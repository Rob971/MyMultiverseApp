package com.example.kmp.presentation.screens.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.kmp.domain.model.*
import com.example.kmp.domain.service.AiService
import com.example.kmp.domain.manager.LanguageManager
import com.example.kmp.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getGreetingUseCase: GetGreetingUseCase,
    private val getJourneysUseCase: GetJourneysUseCase,
    private val upsertJourneyUseCase: UpsertJourneyUseCase,
    private val deleteJourneyUseCase: DeleteJourneyUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val cheerTaskUseCase: CheerTaskUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val refreshJourneysUseCase: RefreshJourneysUseCase,
    private val aiService: AiService,
    private val languageManager: LanguageManager,
) : ScreenModel {
    private val _greeting = MutableStateFlow<Greeting?>(null)
    val greeting: StateFlow<Greeting?> = _greeting.asStateFlow()

    private val _architectState = MutableStateFlow<ArchitectState>(ArchitectState.Idle)
    val architectState: StateFlow<ArchitectState> = _architectState.asStateFlow()

    val currentLanguage: StateFlow<String> = languageManager.currentLanguage

    fun changeLanguage(languageCode: String) {
        languageManager.changeLanguage(languageCode)
    }

    val journeys: StateFlow<List<Journey>> = getJourneysUseCase()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launch {
            _greeting.value = getGreetingUseCase()
            refreshJourneysUseCase()
        }
    }

    fun refineDream(seed: String) {
        if (seed.isBlank()) return
        screenModelScope.launch {
            _architectState.value = ArchitectState.Refining
            aiService.refineDream(seed)
                .onSuccess { proposal ->
                    _architectState.value = ArchitectState.Proposed(proposal)
                }
                .onFailure { error ->
                    _architectState.value = ArchitectState.Error(error.message ?: "Errore sconosciuto")
                }
        }
    }

    fun resetArchitect() {
        _architectState.value = ArchitectState.Idle
    }

    fun addJourney(journey: Journey) {
        screenModelScope.launch {
            upsertJourneyUseCase(journey)
        }
    }

    fun deleteJourney(id: String) {
        screenModelScope.launch {
            deleteJourneyUseCase(id)
        }
    }

    fun toggleTask(journeyId: String, taskId: String) {
        screenModelScope.launch {
            toggleTaskUseCase(journeyId, taskId)
        }
    }

    fun cheerTask(journeyId: String, taskId: String) {
        screenModelScope.launch {
            cheerTaskUseCase(journeyId, taskId)
        }
    }

    fun addTask(journeyId: String, task: JourneyTask) {
        screenModelScope.launch {
            addTaskUseCase(journeyId, task)
        }
    }

    fun updateTask(task: JourneyTask) {
        screenModelScope.launch {
            updateTaskUseCase(task)
        }
    }

    fun deleteTask(journeyId: String, taskId: String) {
        screenModelScope.launch {
            deleteTaskUseCase(journeyId, taskId)
        }
    }
}
