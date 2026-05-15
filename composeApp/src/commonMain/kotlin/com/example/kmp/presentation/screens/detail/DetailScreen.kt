package com.example.kmp.presentation.screens.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmp.domain.model.Journey
import com.example.kmp.domain.model.JourneyTask
import com.example.kmp.presentation.components.FriendlyProgressRing
import com.example.kmp.presentation.screens.calendar.CalendarScreen
import com.example.kmp.presentation.screens.calendar.CalendarScope
import com.example.kmp.presentation.screens.home.HomeScreenModel
import com.example.kmp.presentation.theme.SharedJourneyColors
import kotlinx.coroutines.delay

data class DetailScreen(
    private val journeyId: String,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<HomeScreenModel>()
        
        val journeys by screenModel.journeys.collectAsState()
        val journey = journeys.find { it.id == journeyId } ?: return

        var showCelebration by remember { mutableStateOf(false) }
        
        LaunchedEffect(showCelebration) {
            if (showCelebration) {
                delay(2000)
                showCelebration = false
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            DetailContent(
                journey = journey,
                onBackClick = { navigator.pop() },
                onCalendarClick = { 
                    navigator.push(CalendarScreen(CalendarScope.Goal(journeyId))) 
                },
                onTaskScheduleClick = { taskId -> 
                    navigator.push(CalendarScreen(CalendarScope.Task(journeyId, taskId))) 
                },
                onCheerClick = { taskId -> screenModel.cheerTask(journeyId, taskId) },
                onToggleTask = { taskId, isNowCompleted ->
                    screenModel.toggleTask(journeyId, taskId)
                    if (isNowCompleted) {
                        showCelebration = true
                    }
                }
            )
            
            if (showCelebration) {
                CelebrationOverlay()
            }
        }
    }
}

@Composable
fun CelebrationOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎉 BRAVO! 🎉",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Family Goal Accomplished!",
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    journey: Journey,
    onBackClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onTaskScheduleClick: (String) -> Unit,
    onCheerClick: (String) -> Unit,
    onToggleTask: (String, Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = journey.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SharedJourneyColors.Parchment,
                    titleContentColor = SharedJourneyColors.InkBrown,
                    navigationIconContentColor = SharedJourneyColors.InkBrown
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = parseColor(journey.colorHex).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = parseColor(journey.colorHex)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${journey.familyStreak} Day Streak!",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = parseColor(journey.colorHex)
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = journey.subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                SmartPrincipleSection(journey)
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    FriendlyProgressRing(
                        progress = journey.progress,
                        ringSize = 160.dp,
                        strokeWidth = 14.dp,
                        trackColor = SharedJourneyColors.WarmBeige,
                        progressColor = parseColor(journey.colorHex),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(journey.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineLarge,
                            color = SharedJourneyColors.InkBrown,
                        )
                        Text(
                            text = "Family Progress",
                            style = MaterialTheme.typography.labelMedium,
                            color = SharedJourneyColors.InkMuted,
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "The Roadmap",
                            style = MaterialTheme.typography.titleLarge,
                            color = SharedJourneyColors.InkBrown,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "🚀",
                            fontSize = 20.sp
                        )
                    }
                    
                    Button(
                        onClick = onCalendarClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = parseColor(journey.colorHex).copy(alpha = 0.1f),
                            contentColor = parseColor(journey.colorHex)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        elevation = null
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Roadmap View", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            items(journey.tasks) { task ->
                TaskBlockItem(
                    journey = journey,
                    task = task, 
                    onScheduleClick = { onTaskScheduleClick(task.id) },
                    onCheerClick = { onCheerClick(task.id) },
                    onToggle = { isChecked -> onToggleTask(task.id, isChecked) }
                )
            }

            item {
                Spacer(Modifier.height(48.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = onBackClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = SharedJourneyColors.InkMuted
                        )
                    ) {
                        Text("Back to Dashboard")
                    }
                }
            }
        }
    }
}

@Composable
fun SmartPrincipleSection(journey: Journey) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = SharedJourneyColors.WarmBeige.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = SharedJourneyColors.InkBrown,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "S.M.A.R.T. Framework",
                    style = MaterialTheme.typography.titleMedium,
                    color = SharedJourneyColors.InkBrown,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (expanded) "Collapse" else "View Strategy",
                    style = MaterialTheme.typography.labelSmall,
                    color = parseColor(journey.colorHex)
                )
            }
            
            if (expanded) {
                Spacer(Modifier.height(16.dp))
                SmartItem("Specific", journey.specificGoal, journey)
                SmartItem("Measurable", journey.measurableOutcome, journey)
                SmartItem("Achievable", journey.achievablePlan, journey)
                SmartItem("Relevant", journey.relevanceToFamily, journey)
                SmartItem("Time-bound", journey.timeBoundDeadline, journey)
            }
        }
    }
}

@Composable
fun SmartItem(label: String, value: String?, journey: Journey) {
    if (value == null) return
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = parseColor(journey.colorHex),
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = SharedJourneyColors.InkBrown
        )
    }
}

@Composable
private fun TaskBlockItem(
    journey: Journey,
    task: JourneyTask, 
    onScheduleClick: () -> Unit,
    onCheerClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (task.isCompleted) SharedJourneyColors.ParchmentSurface else Color.White,
        shadowElevation = if (task.isCompleted) 0.dp else 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onToggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = SharedJourneyColors.Sage,
                        uncheckedColor = SharedJourneyColors.OutlineWarm,
                        checkmarkColor = Color.White
                    )
                )

                Spacer(Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (task.isCompleted) SharedJourneyColors.InkMuted else SharedJourneyColors.InkBrown,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = task.planning,
                        style = MaterialTheme.typography.bodySmall,
                        color = SharedJourneyColors.InkMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                IconButton(
                    onClick = onScheduleClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Edit Schedule",
                        tint = parseColor(journey.colorHex).copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.claimedByInitials != null) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = SharedJourneyColors.Sage.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    task.claimedByInitials,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SharedJourneyColors.Sage
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "is on it!",
                            style = MaterialTheme.typography.labelSmall,
                            color = SharedJourneyColors.InkMuted
                        )
                    } else {
                        TextButton(
                            onClick = { /* Claim logic */ },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "✋ Claim for today",
                                style = MaterialTheme.typography.labelSmall,
                                color = parseColor(journey.colorHex)
                            )
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.clickable { onCheerClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = SharedJourneyColors.WarmBeige.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Cheer",
                            modifier = Modifier.size(14.dp),
                            tint = SharedJourneyColors.Terracotta
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${task.cheersCount} Cheers",
                            style = MaterialTheme.typography.labelSmall,
                            color = SharedJourneyColors.InkBrown,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun parseColor(hex: String?): Color {
    return try {
        if (hex == null) SharedJourneyColors.Terracotta
        else Color(("FF" + hex).toLong(16))
    } catch (_: Exception) {
        SharedJourneyColors.Terracotta
    }
}
