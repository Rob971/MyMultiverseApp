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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmp.domain.model.Journey
import com.example.kmp.domain.model.JourneyTask
import com.example.kmp.presentation.components.FriendlyProgressRing
import com.example.kmp.presentation.components.NapolitanBackground
import com.example.kmp.presentation.components.TaskEditDialog
import com.example.kmp.presentation.screens.calendar.CalendarScreen
import com.example.kmp.presentation.screens.calendar.CalendarScope
import com.example.kmp.presentation.screens.home.HomeScreenModel
import com.example.kmp.presentation.screens.insights.InsightsScreen
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

        NapolitanBackground {
            Box(modifier = Modifier.fillMaxSize()) {
                DetailContent(
                    journey = journey,
                    onBackClick = { navigator.pop() },
                    onCalendarClick = { 
                        navigator.push(CalendarScreen(CalendarScope.Goal(journeyId))) 
                    },
                    onInsightsClick = {
                        navigator.push(InsightsScreen(journey))
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
                    },
                    onTaskAdd = { jId, task -> screenModel.addTask(jId, task) },
                    onTaskUpdate = { task -> screenModel.updateTask(task) },
                    onTaskDelete = { jId, tId -> screenModel.deleteTask(jId, tId) }
                )
                
                if (showCelebration) {
                    CelebrationOverlay()
                }
            }
        }
    }
}

@Composable
fun CelebrationOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "✨ BRAVISSIMO! ✨",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = SharedJourneyColors.LemonZestYellow
            )
            Text(
                text = "Un cuore solo, una sola famiglia.",
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
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
    onInsightsClick: () -> Unit,
    onTaskScheduleClick: (String) -> Unit,
    onCheerClick: (String) -> Unit,
    onToggleTask: (String, Boolean) -> Unit,
    onTaskAdd: (String, JourneyTask) -> Unit,
    onTaskUpdate: (JourneyTask) -> Unit,
    onTaskDelete: (String, String) -> Unit,
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<JourneyTask?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = journey.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.action_back))
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SharedJourneyColors.TerracottaOrange.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = SharedJourneyColors.TerracottaOrange
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${journey.familyStreak} Day Streak",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SharedJourneyColors.TerracottaOrange
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = journey.subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SharedJourneyColors.InkMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                GoalStatsRow(journey, onInsightsClick)
            }

            item {
                SmartPrincipleSection(journey)
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    FriendlyProgressRing(
                        progress = journey.progress,
                        ringSize = 180.dp,
                        strokeWidth = 14.dp,
                        trackColor = SharedJourneyColors.SunDrenchedWhite.copy(alpha = 0.5f),
                        progressColor = parseColor(journey.colorHex),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(journey.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                            color = SharedJourneyColors.InkDeep,
                        )
                        Text(
                            text = stringResource(Res.string.detail_progress_label),
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
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(Res.string.detail_timeline),
                            style = MaterialTheme.typography.titleLarge,
                            color = SharedJourneyColors.InkDeep,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "🌋",
                            fontSize = 22.sp
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showAddTaskDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = SharedJourneyColors.MediterraneanTeal
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Event")
                        }

                        TextButton(
                            onClick = onCalendarClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = SharedJourneyColors.MediterraneanTeal
                            )
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Schedule", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(journey.tasks) { task ->
                TaskBlockItem(
                    journey = journey,
                    task = task, 
                    onScheduleClick = { onTaskScheduleClick(task.id) },
                    onCheerClick = { onCheerClick(task.id) },
                    onToggle = { isChecked -> onToggleTask(task.id, isChecked) },
                    onEditClick = { editingTask = task },
                    onDeleteClick = { onTaskDelete(journey.id, task.id) },
                    onClick = { onTaskScheduleClick(task.id) }
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
                        Text(stringResource(Res.string.action_back), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        TaskEditDialog(
            journeyId = journey.id,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = {
                onTaskAdd(journey.id, it)
                showAddTaskDialog = false
            }
        )
    }

    editingTask?.let { task ->
        TaskEditDialog(
            journeyId = journey.id,
            task = task,
            onDismiss = { editingTask = null },
            onConfirm = {
                onTaskUpdate(it)
                editingTask = null
            }
        )
    }
}

@Composable
fun GoalStatsRow(journey: Journey, onInsightsClick: () -> Unit) {
    val totalCheers = journey.tasks.sumOf { it.cheersCount }
    val completedTasks = journey.tasks.count { it.isCompleted }
    val totalTasks = journey.tasks.size
    val topPartner = journey.tasks
        .filter { it.claimedByInitials != null }
        .groupBy { it.claimedByInitials }
        .maxByOrNull { it.value.size }?.key ?: "---"

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Cheers",
                value = totalCheers.toString(),
                icon = Icons.Default.Favorite,
                color = SharedJourneyColors.TerracottaOrange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Progress",
                value = "$completedTasks/$totalTasks",
                icon = Icons.Default.CheckCircle,
                color = SharedJourneyColors.MediterraneanTeal,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Lead",
                value = topPartner,
                icon = Icons.Default.Person,
                color = SharedJourneyColors.LemonZestYellow,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onInsightsClick() },
            colors = CardDefaults.cardColors(containerColor = SharedJourneyColors.GlassWhite),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("📊", fontSize = 18.sp)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(Res.string.insights_title), fontWeight = FontWeight.ExtraBold, color = SharedJourneyColors.MediterraneanTeal)
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SharedJourneyColors.GlassWhite,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = SharedJourneyColors.InkDeep
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SharedJourneyColors.InkMuted
            )
        }
    }
}

@Composable
fun SmartPrincipleSection(journey: Journey) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = SharedJourneyColors.GlassWhite.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = SharedJourneyColors.MediterraneanTeal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.detail_smart_goals),
                    style = MaterialTheme.typography.titleMedium,
                    color = SharedJourneyColors.InkDeep,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (expanded) "Nascondi" else "Vedi Strategia",
                    style = MaterialTheme.typography.labelSmall,
                    color = SharedJourneyColors.MediterraneanTeal,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (expanded) {
                Spacer(Modifier.height(20.dp))
                SmartItem(stringResource(Res.string.detail_specific), journey.specificGoal)
                SmartItem(stringResource(Res.string.detail_measurable), journey.measurableOutcome)
                SmartItem(stringResource(Res.string.detail_achievable), journey.achievablePlan)
                SmartItem(stringResource(Res.string.detail_relevant), journey.relevanceToFamily)
                SmartItem(stringResource(Res.string.detail_timebound), journey.timeBoundDeadline)
            }
        }
    }
}

@Composable
fun SmartItem(label: String, value: String?) {
    if (value == null) return
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = SharedJourneyColors.InkDeep
        )
    }
}

@Composable
private fun TaskBlockItem(
    journey: Journey,
    task: JourneyTask, 
    onScheduleClick: () -> Unit,
    onCheerClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = if (task.isCompleted) SharedJourneyColors.GlassWhite.copy(alpha = 0.5f) else SharedJourneyColors.GlassWhite,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onToggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = SharedJourneyColors.MediterraneanTeal,
                        uncheckedColor = SharedJourneyColors.SageSoft,
                        checkmarkColor = Color.White
                    )
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (task.isCompleted) SharedJourneyColors.InkMuted else SharedJourneyColors.InkDeep,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    Text(
                        text = task.planning,
                        style = MaterialTheme.typography.bodySmall,
                        color = SharedJourneyColors.InkMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onScheduleClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = stringResource(Res.string.action_edit),
                            tint = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = SharedJourneyColors.InkMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SharedJourneyColors.SunDrenchedWhite)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.action_edit)) },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.action_delete), color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.claimedByInitials != null) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    task.claimedByInitials,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SharedJourneyColors.MediterraneanTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "è impegnato/a",
                            style = MaterialTheme.typography.labelSmall,
                            color = SharedJourneyColors.InkMuted
                        )
                    } else {
                        TextButton(
                            onClick = { /* Claim logic */ },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "🤚 Prendi in carico",
                                style = MaterialTheme.typography.labelSmall,
                                color = SharedJourneyColors.MediterraneanTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.clickable { onCheerClick() },
                    shape = RoundedCornerShape(14.dp),
                    color = SharedJourneyColors.TerracottaOrange.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Cheer",
                            modifier = Modifier.size(16.dp),
                            tint = SharedJourneyColors.TerracottaOrange
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${task.cheersCount} Cuori",
                            style = MaterialTheme.typography.labelSmall,
                            color = SharedJourneyColors.InkDeep,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
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
