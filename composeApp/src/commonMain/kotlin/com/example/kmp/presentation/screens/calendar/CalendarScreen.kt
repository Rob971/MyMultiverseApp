package com.example.kmp.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.kmp.presentation.screens.home.HomeScreenModel
import com.example.kmp.presentation.screens.home.JourneyDreamUi
import com.example.kmp.presentation.screens.home.JourneyTaskUi
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
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        // Filter data based on scope
        val visibleTasks = when (val s = scope) {
            is CalendarScope.Global -> journeys.flatMap { j -> j.tasks.map { t -> j to t } }
            is CalendarScope.Goal -> journeys.find { it.id == s.journeyId }?.let { j -> j.tasks.map { t -> j to t } } ?: emptyList()
            is CalendarScope.Task -> journeys.find { it.id == s.journeyId }?.tasks?.find { it.id == s.taskId }?.let { t -> 
                listOf(journeys.find { it.id == s.journeyId }!! to t)
            } ?: emptyList()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                text = when (scope) {
                                    is CalendarScope.Global -> "Family Schedule"
                                    is CalendarScope.Goal -> "Goal Roadmap"
                                    is CalendarScope.Task -> "Task Schedule"
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SharedJourneyColors.Parchment,
                        titleContentColor = SharedJourneyColors.InkBrown
                    )
                )
            },
            containerColor = SharedJourneyColors.Parchment
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                
                ViewModeSelector(
                    selectedMode = viewMode,
                    onModeSelected = { viewMode = it }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CalendarHeader(viewMode)
                    }

                    item {
                        when (viewMode) {
                            CalendarViewMode.DAY -> DayView(visibleTasks)
                            CalendarViewMode.WEEK -> WeekView(visibleTasks, daysOfWeek)
                            CalendarViewMode.MONTH -> MonthView()
                            CalendarViewMode.YEAR -> YearView()
                        }
                    }

                    item {
                        Text(
                            text = "Upcoming Agenda",
                            style = MaterialTheme.typography.titleMedium,
                            color = SharedJourneyColors.InkBrown,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    items(visibleTasks) { (journey, task) ->
                        AgendaItem(journey, task, daysOfWeek)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(SharedJourneyColors.WarmBeige.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CalendarViewMode.entries.forEach { mode ->
                val isSelected = mode == selectedMode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onModeSelected(mode) },
                    color = if (isSelected) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) SharedJourneyColors.Terracotta else SharedJourneyColors.InkMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
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
                    CalendarViewMode.DAY -> "Today, May 15"
                    CalendarViewMode.WEEK -> "May 12 - 18, 2025"
                    CalendarViewMode.MONTH -> "May 2025"
                    CalendarViewMode.YEAR -> "2025 Roadmap"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = SharedJourneyColors.InkBrown,
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.DateRange, contentDescription = null, tint = SharedJourneyColors.InkMuted)
        }
    }

    @Composable
    private fun DayView(tasks: List<Pair<JourneyDreamUi, JourneyTaskUi>>) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("08:00", "12:00", "18:00", "21:00").forEach { time ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(time, style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.InkMuted, modifier = Modifier.width(48.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = SharedJourneyColors.OutlineWarm.copy(alpha = 0.5f))
                }
                val tasksAtTime = tasks.filter { it.second.reminderTime?.startsWith(time.take(2)) == true }
                tasksAtTime.forEach { (j, t) ->
                    TaskSmallCard(j, t)
                }
            }
        }
    }

    @Composable
    private fun WeekView(tasks: List<Pair<JourneyDreamUi, JourneyTaskUi>>, days: List<String>) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEachIndexed { index, day ->
                val dayNum = index + 1
                val tasksOnDay = tasks.filter { it.second.scheduledDays.contains(dayNum) }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(day, style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.InkMuted)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (tasksOnDay.isNotEmpty()) parseColor(tasksOnDay.first().first.colorHex).copy(alpha = 0.2f) 
                                else SharedJourneyColors.WarmBeige.copy(alpha = 0.1f), 
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (tasksOnDay.isNotEmpty()) {
                            Box(modifier = Modifier.size(8.dp).background(parseColor(tasksOnDay.first().first.colorHex), CircleShape))
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
                                .background(SharedJourneyColors.ParchmentSurface, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(if (dayNum <= 31) "$dayNum" else "", fontSize = 10.sp, color = SharedJourneyColors.InkMuted)
                            if (dayNum % 3 == 0 && dayNum <= 31) {
                                Box(modifier = Modifier.align(Alignment.Center).size(4.dp).background(SharedJourneyColors.Terracotta, CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun YearView() {
        val quarters = listOf("Q1: Start", "Q2: Growth", "Q3: Peak", "Q4: Review")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            quarters.forEach { q ->
                Surface(
                    color = SharedJourneyColors.WarmBeige.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(q, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    private fun TaskSmallCard(journey: JourneyDreamUi, task: JourneyTaskUi) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            color = parseColor(journey.colorHex).copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp, 24.dp).background(parseColor(journey.colorHex), RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(8.dp))
                Text(task.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun AgendaItem(journey: JourneyDreamUi, task: JourneyTaskUi, days: List<String>) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 1.dp
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(parseColor(journey.colorHex), CircleShape))
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SharedJourneyColors.InkBrown)
                    Text(
                        text = "${journey.title} • ${task.scheduledDays.joinToString(", ") { days[it-1] }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SharedJourneyColors.InkMuted
                    )
                }
                if (task.reminderTime != null) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp), tint = parseColor(journey.colorHex))
                }
            }
        }
    }

    private fun parseColor(hex: String?): Color {
        return try {
            if (hex == null) SharedJourneyColors.Terracotta
            else Color(longArrayOf(0xFFL shl 24 or hex.toLong(16)).first())
        } catch (_: Exception) {
            SharedJourneyColors.Terracotta
        }
    }
}
