package com.example.kmp.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmp.domain.model.Journey
import com.example.kmp.presentation.components.NapolitanBackground
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
        
        val existingJourney = journeys.find { it.id == journeyId }
        
        var title by remember { mutableStateOf(existingJourney?.title ?: "") }
        var subtitle by remember { mutableStateOf(existingJourney?.subtitle ?: "") }
        var specific by remember { mutableStateOf(existingJourney?.specificGoal ?: "") }
        var measurable by remember { mutableStateOf(existingJourney?.measurableOutcome ?: "") }
        var achievable by remember { mutableStateOf(existingJourney?.achievablePlan ?: "") }
        var relevant by remember { mutableStateOf(existingJourney?.relevanceToFamily ?: "") }
        var timeBound by remember { mutableStateOf(existingJourney?.timeBoundDeadline ?: "") }

        NapolitanBackground {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                if (journeyId == null) "Crea un Sogno" else "Modifica Sogno",
                                fontWeight = FontWeight.Black
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
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
                    FloatingActionButton(
                        onClick = {
                            val newJourney = (existingJourney ?: Journey(
                                id = journeyId ?: Clock.System.now().toEpochMilliseconds().toString(),
                                title = "",
                                subtitle = "",
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
                            navigator.pop()
                        },
                        containerColor = SharedJourneyColors.TerracottaOrange,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        EditField("Titolo del Sogno", title) { title = it }
                    }
                    item {
                        EditField("Sottotitolo", subtitle) { subtitle = it }
                    }
                    item {
                        Text(
                            "Parametri S.M.A.R.T.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SharedJourneyColors.MediterraneanTeal,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    item {
                        EditField("Specifico (Cosa vogliamo?)", specific) { specific = it }
                    }
                    item {
                        EditField("Misurabile (Come lo tracciamo?)", measurable) { measurable = it }
                    }
                    item {
                        EditField("Raggiungibile (È realistico?)", achievable) { achievable = it }
                    }
                    item {
                        EditField("Rilevante (Perché per noi?)", relevant) { relevant = it }
                    }
                    item {
                        EditField("Tempo (Entro quando?)", timeBound) { timeBound = it }
                    }
                }
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
