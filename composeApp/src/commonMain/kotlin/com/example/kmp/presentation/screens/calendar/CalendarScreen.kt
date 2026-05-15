package com.example.kmp.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmp.domain.model.Journey
import com.example.kmp.domain.model.JourneyTask
import com.example.kmp.presentation.components.NapolitanBackground
import com.example.kmp.presentation.screens.home.HomeScreenModel
import com.example.kmp.presentation.theme.SharedJourneyColors

enum class CalendarViewMode { DAY, WEEK, MONTH, YEAR }

sealed class CalendarScope {
    object Global : CalendarScope()
    data class Goal(val journeyId: String) : CalendarScope()
    data class Task(val journeyId: String, val taskId: String) : CalendarScope()
}

data class CalendarScreen(
    val scope: CalendarScope = CalendarScope.Global
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<HomeScreenModel>()
        val journeys by screenModel.journeys.collectAsState()
        
        var viewMode by remember { mutableStateOf(CalendarViewMode.WEEK) }
        val daysOfWeek = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")

        val visibleTasks = when (val s = scope) {
            is CalendarScope.Global -> journeys.flatMap { j -> j.tasks.map { t -> j to t } }
            is CalendarScope.Goal -> journeys.find { it.id == s.journeyId }?.let { j -> j.tasks.map { t -> j to t } } ?: emptyList()
            is CalendarScope.Task -> journeys.find { it.id == s.journeyId }?.tasks?.find { it.id == s.taskId }?.let { t -> 
                listOf(journeys.find { it.id == s.journeyId }!! to t)
            } ?: emptyList()
        }

        NapolitanBackground {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                text = when (scope) {
                                    is CalendarScope.Global -> "Calendario Famiglia"
                                    is CalendarScope.Goal -> "Piano d'Azione"
                                    is CalendarScope.Task -> "Dettaglio Attività"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = SharedJourneyColors.MediterraneanTeal,
                            navigationIconContentColor = SharedJourneyColors.MediterraneanTeal
                        )
                    )
                },
                containerColor = Color.Transparent
            ) { padding ->
                Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                    
                    ViewModeSelector(
                        selectedMode = viewMode,
                        onModeSelected = { viewMode = it }
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            CalendarHeader(viewMode)
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(28.dp))
                                    .padding(20.dp)
                            ) {
                                when (viewMode) {
                                    CalendarViewMode.DAY -> DayView(visibleTasks)
                                    CalendarViewMode.WEEK -> WeekView(visibleTasks, daysOfWeek)
                                    CalendarViewMode.MONTH -> MonthView()
                                    CalendarViewMode.YEAR -> YearView()
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Prossimi Appuntamenti",
                                style = MaterialTheme.typography.titleMedium,
                                color = SharedJourneyColors.InkDeep,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(visibleTasks) { (journey, task) ->
                            AgendaItem(journey, task, daysOfWeek)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ViewModeSelector(
        selectedMode: CalendarViewMode,
        onModeSelected: (CalendarViewMode) -> Unit
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxWidth(),
            color = SharedJourneyColors.GlassWhite,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalendarViewMode.entries.forEach { mode ->
                    val isSelected = mode == selectedMode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onModeSelected(mode) },
                        color = if (isSelected) SharedJourneyColors.MediterraneanTeal else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when(mode) {
                                    CalendarViewMode.DAY -> "Giorno"
                                    CalendarViewMode.WEEK -> "Sett."
                                    CalendarViewMode.MONTH -> "Mese"
                                    CalendarViewMode.YEAR -> "Anno"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else SharedJourneyColors.InkMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CalendarHeader(mode: CalendarViewMode) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (mode) {
                    CalendarViewMode.DAY -> "Oggi, 15 Maggio"
                    CalendarViewMode.WEEK -> "12 - 18 Maggio, 2025"
                    CalendarViewMode.MONTH -> "Maggio 2025"
                    CalendarViewMode.YEAR -> "Roadmap 2025"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Black
            )
            Icon(Icons.Default.DateRange, contentDescription = null, tint = SharedJourneyColors.MediterraneanTeal)
        }
    }

    @Composable
    private fun DayView(tasks: List<Pair<Journey, JourneyTask>>) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("08:00", "12:00", "18:00", "21:00").forEach { time ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(time, style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.InkMuted, modifier = Modifier.width(48.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f))
                }
                val tasksAtTime = tasks.filter { it.second.reminderTime?.startsWith(time.take(2)) == true }
                tasksAtTime.forEach { (j, t) ->
                    TaskSmallCard(j, t)
                }
            }
        }
    }

    @Composable
    private fun WeekView(tasks: List<Pair<Journey, JourneyTask>>, days: List<String>) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEachIndexed { index, day ->
                val dayNum = index + 1
                val tasksOnDay = tasks.filter { it.second.scheduledDays.contains(dayNum) }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(day, style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.InkMuted, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (tasksOnDay.isNotEmpty()) parseColor(tasksOnDay.first().first.colorHex).copy(alpha = 0.15f) 
                                else SharedJourneyColors.ParchmentWarm, 
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (tasksOnDay.isNotEmpty()) {
                            Box(modifier = Modifier.size(10.dp).background(parseColor(tasksOnDay.first().first.colorHex), CircleShape))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MonthView() {
        Column {
            for (row in 0..4) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 1..7) {
                        val dayNum = row * 7 + col
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(SharedJourneyColors.ParchmentWarm.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(if (dayNum <= 31) "$dayNum" else "", fontSize = 10.sp, color = SharedJourneyColors.InkMuted)
                            if (dayNum % 4 == 0 && dayNum <= 31) {
                                Box(modifier = Modifier.align(Alignment.Center).size(6.dp).background(SharedJourneyColors.TerracottaOrange, CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun YearView() {
        val quarters = listOf("Q1: Fondamenta", "Q2: Crescita", "Q3: Vitalità", "Q4: Raccolto")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            quarters.forEachIndexed { i, q ->
                Surface(
                    color = when(i) {
                        0 -> SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f)
                        1 -> SharedJourneyColors.LemonZestYellow.copy(alpha = 0.1f)
                        2 -> SharedJourneyColors.TerracottaOrange.copy(alpha = 0.1f)
                        else -> SharedJourneyColors.SageSoft.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(q, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = SharedJourneyColors.InkDeep)
                        Spacer(Modifier.weight(1f))
                        Text("Vedi Piano", style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.InkMuted)
                    }
                }
            }
        }
    }

    @Composable
    private fun TaskSmallCard(journey: Journey, task: JourneyTask) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            color = SharedJourneyColors.SunDrenchedWhite,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp, 24.dp).background(parseColor(journey.colorHex), RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(12.dp))
                Text(task.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SharedJourneyColors.InkDeep)
            }
        }
    }

    @Composable
    private fun AgendaItem(journey: Journey, task: JourneyTask, days: List<String>) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SharedJourneyColors.GlassWhite,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 0.dp
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(parseColor(journey.colorHex), CircleShape))
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = SharedJourneyColors.InkDeep)
                    Text(
                        text = "${journey.title} • ${task.scheduledDays.joinToString(", ") { days[it-1] }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SharedJourneyColors.InkMuted
                    )
                }
                if (task.reminderTime != null) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(20.dp), tint = SharedJourneyColors.TerracottaOrange)
                }
            }
        }
    }

    private fun parseColor(hex: String?): Color {
        return try {
            if (hex == null) SharedJourneyColors.TerracottaOrange
            else Color(("FF" + hex).toLong(16))
        } catch (_: Exception) {
            SharedJourneyColors.TerracottaOrange
        }
    }
}
