package com.example.kmp.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmp.domain.model.*
import com.example.kmp.presentation.components.NapolitanBackground
import com.example.kmp.presentation.theme.AppIcons
import com.example.kmp.presentation.theme.SharedJourneyColors
import kotlinx.datetime.Clock

data class JourneyEditScreen(
    val journeyId: String? = null
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<HomeScreenModel>()
        val journeys by screenModel.journeys.collectAsState()
        val architectState by screenModel.architectState.collectAsState()
        
        val existingJourney = journeys.find { it.id == journeyId }
        
        var title by remember { mutableStateOf(existingJourney?.title ?: "") }
        var subtitle by remember { mutableStateOf(existingJourney?.subtitle ?: "") }
        var specific by remember { mutableStateOf(existingJourney?.specificGoal ?: "") }
        var measurable by remember { mutableStateOf(existingJourney?.measurableOutcome ?: "") }
        var achievable by remember { mutableStateOf(existingJourney?.achievablePlan ?: "") }
        var relevant by remember { mutableStateOf(existingJourney?.relevanceToFamily ?: "") }
        var timeBound by remember { mutableStateOf(existingJourney?.timeBoundDeadline ?: "") }
        var seedText by remember { mutableStateOf("") }
        var selectedTasks by remember { mutableStateOf<List<String>>(emptyList()) }

        // Sync local state with AI proposal
        LaunchedEffect(architectState) {
            if (architectState is ArchitectState.Proposed) {
                val proposal = (architectState as ArchitectState.Proposed).proposal
                title = proposal.title
                subtitle = proposal.subtitle
                specific = proposal.specific
                measurable = proposal.measurable
                achievable = proposal.achievable
                relevant = proposal.relevant
                timeBound = proposal.timeBound
                selectedTasks = proposal.suggestedTasks
            }
        }

        NapolitanBackground {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                if (journeyId == null) "Architetto dei Sogni" else "Modifica Sogno",
                                fontWeight = FontWeight.Black
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                screenModel.resetArchitect()
                                navigator.pop() 
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = SharedJourneyColors.MediterraneanTeal,
                            navigationIconContentColor = SharedJourneyColors.MediterraneanTeal
                        )
                    )
                },
                containerColor = Color.Transparent,
                floatingActionButton = {
                    if (architectState is ArchitectState.Proposed || journeyId != null) {
                        FloatingActionButton(
                            onClick = {
                                val finalJourneyId = journeyId ?: Clock.System.now().toEpochMilliseconds().toString()
                                val newJourney = (existingJourney ?: Journey(
                                    id = finalJourneyId,
                                    title = title,
                                    subtitle = subtitle,
                                    progress = 0f,
                                    participantInitials = listOf("S", "A")
                                )).copy(
                                    title = title,
                                    subtitle = subtitle,
                                    specificGoal = specific,
                                    measurableOutcome = measurable,
                                    achievablePlan = achievable,
                                    relevanceToFamily = relevant,
                                    timeBoundDeadline = timeBound,
                                    colorHex = existingJourney?.colorHex ?: "E2725B"
                                )
                                
                                screenModel.addJourney(newJourney)
                                
                                // Add tasks
                                selectedTasks.forEach { taskTitle ->
                                    screenModel.addTask(finalJourneyId, JourneyTask(
                                        id = Clock.System.now().toEpochMilliseconds().toString() + taskTitle.hashCode(),
                                        journeyId = finalJourneyId,
                                        title = taskTitle,
                                        planning = "Suggested by AI Architect",
                                        isCompleted = false,
                                        label = "AI",
                                        scheduledDays = listOf(1, 2, 3, 4, 5, 6, 7)
                                    ))
                                }
                                
                                screenModel.resetArchitect()
                                navigator.pop()
                            },
                            containerColor = SharedJourneyColors.TerracottaOrange,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    AnimatedContent(
                        targetState = if (journeyId != null) ArchitectState.Proposed(
                            SmartGoalProposal(title, subtitle, specific, measurable, achievable, relevant, timeBound, emptyList())
                        ) else architectState,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }
                    ) { state ->
                        when (state) {
                            is ArchitectState.Idle -> {
                                SeedInputSection(
                                    value = seedText,
                                    onValueChange = { seedText = it },
                                    onRefine = { screenModel.refineDream(seedText) }
                                )
                            }
                            is ArchitectState.Refining -> {
                                LoadingSection()
                            }
                            is ArchitectState.Proposed -> {
                                ProposalReviewSection(
                                    title = title, onTitleChange = { title = it },
                                    subtitle = subtitle, onSubtitleChange = { subtitle = it },
                                    specific = specific, onSpecificChange = { specific = it },
                                    measurable = measurable, onMeasurableChange = { measurable = it },
                                    achievable = achievable, onAchievableChange = { achievable = it },
                                    relevant = relevant, onRelevantChange = { relevant = it },
                                    timeBound = timeBound, onTimeBoundChange = { timeBound = it },
                                    suggestedTasks = selectedTasks,
                                    onToggleTask = { task ->
                                        selectedTasks = if (selectedTasks.contains(task)) {
                                            selectedTasks - task
                                        } else {
                                            selectedTasks + task
                                        }
                                    },
                                    onRegenerate = { screenModel.resetArchitect() }
                                )
                            }
                            is ArchitectState.Error -> {
                                ErrorSection(state.message) { screenModel.resetArchitect() }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SeedInputSection(
        value: String,
        onValueChange: (String) -> Unit,
        onRefine: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Qual è il prossimo sogno della tua famiglia?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = SharedJourneyColors.MediterraneanTeal,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Scrivi una frase o una parola chiave e l'Architetto AI ti aiuterà a pianificarlo.",
                style = MaterialTheme.typography.bodyMedium,
                color = SharedJourneyColors.InkMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Esempio: Comprare una moto, Viaggio in Spagna...") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SharedJourneyColors.GlassWhite,
                    unfocusedContainerColor = SharedJourneyColors.GlassWhite,
                    focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRefine,
                enabled = value.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)
            ) {
                Icon(AppIcons.Sparkles, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Progetta con l'Architetto", fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun LoadingSection() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = SharedJourneyColors.MediterraneanTeal)
            Spacer(Modifier.height(24.dp))
            Text(
                "L'Architetto sta progettando...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SharedJourneyColors.MediterraneanTeal
            )
            Text(
                "Definendo i parametri S.M.A.R.T. per il tuo successo.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )
        }
    }

    @Composable
    private fun ProposalReviewSection(
        title: String, onTitleChange: (String) -> Unit,
        subtitle: String, onSubtitleChange: (String) -> Unit,
        specific: String, onSpecificChange: (String) -> Unit,
        measurable: String, onMeasurableChange: (String) -> Unit,
        achievable: String, onAchievableChange: (String) -> Unit,
        relevant: String, onRelevantChange: (String) -> Unit,
        timeBound: String, onTimeBoundChange: (String) -> Unit,
        suggestedTasks: List<String>,
        onToggleTask: (String) -> Unit,
        onRegenerate: () -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(AppIcons.Sparkles, contentDescription = null, tint = SharedJourneyColors.MediterraneanTeal, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Proposta dell'Architetto", style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.MediterraneanTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { EditField("Titolo del Sogno", title, onTitleChange) }
            item { EditField("Sottotitolo", subtitle, onSubtitleChange) }

            item {
                Text(
                    "Parametri S.M.A.R.T.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SharedJourneyColors.MediterraneanTeal,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item { EditField("Specifico (Cosa vogliamo?)", specific, onSpecificChange) }
            item { EditField("Misurabile (Come lo tracciamo?)", measurable, onMeasurableChange) }
            item { EditField("Raggiungibile (È realistico?)", achievable, onAchievableChange) }
            item { EditField("Rilevante (Perché per noi?)", relevant, onRelevantChange) }
            item { EditField("Tempo (Entro quando?)", timeBound, onTimeBoundChange) }

            if (suggestedTasks.isNotEmpty()) {
                item {
                    Text(
                        "Azioni Suggerite",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SharedJourneyColors.MediterraneanTeal,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(suggestedTasks) { task ->
                    val isSelected = true // For now they are all selected by default
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isSelected) SharedJourneyColors.GlassWhite else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SharedJourneyColors.InkMuted.copy(alpha = 0.2f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isSelected, onCheckedChange = { onToggleTask(task) })
                            Text(task, style = MaterialTheme.typography.bodyMedium, color = SharedJourneyColors.InkDeep)
                        }
                    }
                }
            }

            item {
                TextButton(
                    onClick = onRegenerate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Riprova con un'altra idea")
                }
            }
        }
    }

    @Composable
    private fun ErrorSection(message: String, onRetry: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Oops!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.Red.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
            Text(message, textAlign = TextAlign.Center, color = SharedJourneyColors.InkMuted)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)) {
                Text("Torna indietro")
            }
        }
    }

    @Composable
    private fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = SharedJourneyColors.InkMuted,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SharedJourneyColors.GlassWhite,
                    unfocusedContainerColor = SharedJourneyColors.GlassWhite,
                    focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
fun JourneyEditScreenPreview() {
    MaterialTheme {
        JourneyEditScreen().Content()
    }
}
