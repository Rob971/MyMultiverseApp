package com.example.kmp.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
import androidx.compose.ui.unit.sp
import com.example.kmp.domain.model.FinanceProfile
import com.example.kmp.domain.model.Journey
import com.example.kmp.domain.model.JourneyCategory
import com.example.kmp.domain.model.JourneyTask
import com.example.kmp.domain.model.MealPlanningProfile
import com.example.kmp.presentation.theme.SharedJourneyColors

@Composable
private fun ParticipantAvatar(initial: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SharedJourneyColors.MediterraneanTeal),
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun JourneyDreamCard(
    dream: Journey,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onTaskToggle: (String, String) -> Unit,
    onTaskAdd: (String, JourneyTask) -> Unit,
    onTaskUpdate: (JourneyTask) -> Unit,
    onTaskDelete: (String, String) -> Unit,
    onTaskClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    progressColor: Color = SharedJourneyColors.TerracottaOrange,
) {
    val totalCheers = dream.tasks.sumOf { it.cheersCount }
    val openTasks = dream.tasks.count { !it.isCompleted }
    var showMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<JourneyTask?>(null) }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        color = SharedJourneyColors.GlassWhite,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            progressColor.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column {
                // Top/Middle Part: Main Information (Clickable to open detail)
                Row(
                    modifier = Modifier
                        .clickable(onClick = onClick)
                        .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        FriendlyProgressRing(
                            progress = dream.progress,
                            trackColor = SharedJourneyColors.ParchmentWarm,
                            progressColor = progressColor,
                            ringSize = 80.dp,
                            strokeWidth = 10.dp
                        )
                        Text(
                            text = "${(dream.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = SharedJourneyColors.InkDeep
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dream.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SharedJourneyColors.InkDeep
                                )
                                Text(
                                    text = dream.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SharedJourneyColors.InkMuted,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(8.dp))
                                CategoryBadge(
                                    category = dream.category,
                                    color = progressColor,
                                )
                            }
                            
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = SharedJourneyColors.InkMuted)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(SharedJourneyColors.SunDrenchedWhite)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Apri Dettaglio") },
                                        onClick = { 
                                            showMenu = false
                                            onClick() 
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.edit_dream_edit_title)) },
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

                        Spacer(Modifier.height(12.dp))
                        CategorySummary(
                            category = dream.category,
                            tasks = dream.tasks,
                            openTasks = openTasks,
                            totalCheers = totalCheers,
                            familyStreak = dream.familyStreak,
                            timeBoundDeadline = dream.timeBoundDeadline,
                            mealPlanningProfile = dream.mealPlanningProfile,
                            financeProfile = dream.financeProfile,
                        )

                        Spacer(Modifier.height(16.dp))

                        // Bottom Part: Avatars and Expand Toggle (Reserve for Expansion)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                                .clickable { isExpanded = !isExpanded }
                                .padding(vertical = 12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy((-10).dp)
                            ) {
                                dream.participantInitials.forEach { initial ->
                                    ParticipantAvatar(initial)
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (dream.familyStreak > 0) {
                                    Text(
                                        text = "🔥",
                                        fontSize = 14.sp
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${dream.familyStreak} days",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SharedJourneyColors.TerracottaOrange,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(Modifier.width(12.dp))
                                }
                                Icon(
                                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collassa attività" else "Espandi attività",
                                    tint = SharedJourneyColors.InkMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                            .fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 16.dp),
                            color = SharedJourneyColors.ParchmentWarm
                        )
                        
                        dream.tasks.forEach { task ->
                            TaskRow(
                                task = task,
                                onToggle = { onTaskToggle(dream.id, task.id) },
                                onEdit = { editingTask = task },
                                onDelete = { onTaskDelete(dream.id, task.id) },
                                onClick = { onTaskClick(dream.id, task.id) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        TextButton(
                            onClick = { showAddTaskDialog = true },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.textButtonColors(contentColor = SharedJourneyColors.MediterraneanTeal)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(Res.string.task_edit_title_add), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        TaskEditDialog(
            journeyId = dream.id,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = {
                onTaskAdd(dream.id, it)
                showAddTaskDialog = false
            }
        )
    }

    editingTask?.let { task ->
        TaskEditDialog(
            journeyId = dream.id,
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
private fun CategoryBadge(
    category: JourneyCategory,
    color: Color,
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = CircleShape,
    ) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = SharedJourneyColors.InkDeep,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun CategorySummary(
    category: JourneyCategory,
    tasks: List<JourneyTask>,
    openTasks: Int,
    totalCheers: Int,
    familyStreak: Int,
    timeBoundDeadline: String?,
    mealPlanningProfile: MealPlanningProfile?,
    financeProfile: FinanceProfile?,
) {
    val reminderCount = tasks.count { it.reminderTime != null }
    val scheduledCount = tasks.count { it.scheduledDays.isNotEmpty() }
    val completedCount = tasks.count { it.isCompleted }
    val text = when (category) {
        JourneyCategory.CalendarLogistics -> "$scheduledCount scheduled items - $reminderCount reminders"
        JourneyCategory.HouseholdManagement -> "$openTasks open upkeep tasks - ${tasks.size} total routines"
        JourneyCategory.MealPlanning -> {
            val people = mealPlanningProfile?.cookingFor?.takeIf { it.isNotBlank() } ?: "No party size"
            val cookTime = mealPlanningProfile?.busyWeeknightCookTime?.takeIf { it.isNotBlank() } ?: "No cook time"
            val restrictions = mealPlanningProfile?.dietaryRestrictions
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(", ")
                ?: "No restrictions"
            "Cooking for $people - $cookTime - $restrictions - $openTasks prep actions"
        }
        JourneyCategory.HouseholdFinance -> {
            val split = financeProfile?.billSplitStrategy?.takeIf { it.isNotBlank() }
                ?: financeProfile?.financeSplit?.takeIf { it.isNotBlank() }
                ?: "No split rule"
            val billCount = financeProfile?.recurringBills?.size ?: 0
            "$split - $billCount bill groups - $openTasks ledger actions"
        }
        JourneyCategory.HealthWellness -> "$completedCount habits completed - $familyStreak day streak - $totalCheers cheers"
        JourneyCategory.LongTermProjects -> "${tasks.size} milestones - ${timeBoundDeadline ?: "No deadline set"}"
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = SharedJourneyColors.InkMuted,
        fontWeight = FontWeight.SemiBold,
    )

    if (category == JourneyCategory.MealPlanning && !mealPlanningProfile?.dislikedIngredients.isNullOrBlank()) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Avoid: ${mealPlanningProfile?.dislikedIngredients}",
            style = MaterialTheme.typography.labelSmall,
            color = SharedJourneyColors.InkMuted,
        )
    }
    if (category == JourneyCategory.MealPlanning && !mealPlanningProfile?.lunchPreference.isNullOrBlank()) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Lunch: ${mealPlanningProfile?.lunchPreference}",
            style = MaterialTheme.typography.labelSmall,
            color = SharedJourneyColors.InkMuted,
        )
    }
    if (category == JourneyCategory.MealPlanning && !mealPlanningProfile?.rightNowGoal.isNullOrBlank()) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Today: ${mealPlanningProfile?.rightNowGoal}",
            style = MaterialTheme.typography.labelSmall,
            color = SharedJourneyColors.InkMuted,
        )
    }
    val mealProfile = mealPlanningProfile
    if (category == JourneyCategory.MealPlanning && mealProfile != null && mealProfile.locationPreference.isNotBlank()) {
        Spacer(Modifier.height(4.dp))
        val locationText = if (mealProfile.locationPreference == "Provide location manually" && mealProfile.manualLocation.isNotBlank()) {
            mealProfile.manualLocation
        } else {
            mealProfile.locationPreference
        }
        Text(
            text = "Local shops: $locationText",
            style = MaterialTheme.typography.labelSmall,
            color = SharedJourneyColors.InkMuted,
        )
    }
    val finance = financeProfile
    if (category == JourneyCategory.HouseholdFinance && finance != null && (finance.billPainPoint.isNotBlank() || finance.dailyAnnoyance.isNotBlank())) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Friction: ${finance.billPainPoint.ifBlank { finance.dailyAnnoyance }}",
            style = MaterialTheme.typography.labelSmall,
            color = SharedJourneyColors.InkMuted,
        )
    }
    if (category == JourneyCategory.HouseholdFinance && finance != null && finance.settleWorkflow.isNotBlank()) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Settle-up: ${finance.settleWorkflow}",
            style = MaterialTheme.typography.labelSmall,
            color = SharedJourneyColors.InkMuted,
        )
    }
}

@Composable
private fun TaskRow(
    task: JourneyTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showTaskMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SharedJourneyColors.SunDrenchedWhite)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = SharedJourneyColors.MediterraneanTeal)
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (task.isCompleted) SharedJourneyColors.InkMuted else SharedJourneyColors.InkDeep
            )
            if (task.planning.isNotEmpty()) {
                Text(
                    text = task.planning,
                    style = MaterialTheme.typography.labelSmall,
                    color = SharedJourneyColors.InkMuted
                )
            }
        }
        Box {
            IconButton(onClick = { showTaskMenu = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(16.dp), tint = SharedJourneyColors.InkMuted)
            }
            DropdownMenu(
                expanded = showTaskMenu,
                onDismissRequest = { showTaskMenu = false },
                modifier = Modifier.background(SharedJourneyColors.SunDrenchedWhite)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.action_edit)) },
                    onClick = { 
                        showTaskMenu = false
                        onEdit() 
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.action_delete), color = Color.Red) },
                    onClick = { 
                        showTaskMenu = false
                        onDelete() 
                    }
                )
            }
        }
    }
}
