package app.mymultiverse.kmp.presentation.screens.detail

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
import app.mymultiverse.kmp.domain.model.FinanceBillEntry
import app.mymultiverse.kmp.domain.model.FinanceProfile
import app.mymultiverse.kmp.domain.model.HealthWellnessProfile
import app.mymultiverse.kmp.domain.model.Journey
import app.mymultiverse.kmp.domain.model.JourneyCategory
import app.mymultiverse.kmp.domain.model.JourneyPlanItem
import app.mymultiverse.kmp.domain.model.JourneyTask
import app.mymultiverse.kmp.presentation.components.FriendlyProgressRing
import app.mymultiverse.kmp.presentation.components.NapolitanBackground
import app.mymultiverse.kmp.presentation.components.TaskEditDialog
import app.mymultiverse.kmp.presentation.screens.calendar.CalendarScreen
import app.mymultiverse.kmp.presentation.screens.calendar.CalendarScope
import app.mymultiverse.kmp.presentation.screens.home.HomeScreenModel
import app.mymultiverse.kmp.presentation.screens.insights.InsightsScreen
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

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
                    onFinanceBillEntryAdd = { entry -> screenModel.addFinanceBillEntry(journeyId, entry) },
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
    onFinanceBillEntryAdd: (FinanceBillEntry) -> Unit,
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<JourneyTask?>(null) }
    var showFinanceBillDialog by remember { mutableStateOf(false) }

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

            if (journey.category == JourneyCategory.LongTermProjects && journey.longTermProjectProfile != null) {
                item {
                    LongTermActionBlueprintSection(journey)
                }
            }

            if (journey.category == JourneyCategory.HouseholdFinance && journey.financeProfile != null) {
                item {
                    MonthlySpendingSection(journey.financeProfile)
                }
                item {
                    FinanceBillLedgerSection(
                        journey = journey,
                        profile = journey.financeProfile,
                        onLogBillClick = { showFinanceBillDialog = true }
                    )
                }
            }

            if (journey.category == JourneyCategory.HealthWellness && journey.healthWellnessProfile != null) {
                item {
                    HealthWellnessInsightSection(
                        profile = journey.healthWellnessProfile,
                        planItems = journey.planItems
                    )
                }
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

    if (showFinanceBillDialog && journey.financeProfile != null) {
        FinanceBillEntryDialog(
            journeyId = journey.id,
            profile = journey.financeProfile,
            onDismiss = { showFinanceBillDialog = false },
            onConfirm = {
                onFinanceBillEntryAdd(it)
                showFinanceBillDialog = false
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
fun HealthWellnessInsightSection(
    profile: HealthWellnessProfile,
    planItems: List<JourneyPlanItem>,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = SharedJourneyColors.GlassWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Couples Wellness Radar",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "You + Them vs. The Problem. Use this card to translate tension into a shared next step.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                WellnessContextChip("Energy", profile.energyLevel, Modifier.weight(1f))
                WellnessContextChip("Stress", profile.stressLevel, Modifier.weight(1f))
                WellnessContextChip("Connection", profile.connectionLevel, Modifier.weight(1f))
            }
            if (planItems.isNotEmpty()) {
                planItems.forEach { item ->
                    WellnessPlanItemCard(item)
                }
            } else {
                if (profile.conflictTopic.isNotBlank()) {
                    WellnessLine("De-escalator", profile.conflictTopic)
                }
                if (profile.partnerLoveLanguage.isNotBlank()) {
                    WellnessLine("Appreciation", "${profile.partnerLoveLanguage} - ${profile.availableTime.ifBlank { "quick action" }} - ${profile.budget.ifBlank { "no budget" }}")
                }
                if (profile.weeklyDrain.isNotBlank()) {
                    WellnessLine("Weekly drain", profile.weeklyDrain)
                }
                if (profile.dateNightLikes.isNotBlank()) {
                    WellnessLine("Date night", "${profile.dateNightDuration.ifBlank { "Tonight" }} around ${profile.dateNightLikes}")
                }
            }
        }
    }
}

@Composable
private fun WellnessPlanItemCard(item: JourneyPlanItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SharedJourneyColors.SunDrenchedWhite,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                item.title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                item.content,
                style = MaterialTheme.typography.bodyMedium,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WellnessContextChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SharedJourneyColors.SunDrenchedWhite,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "${value.ifBlank { "?" }}/10",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun WellnessLine(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SharedJourneyColors.SunDrenchedWhite,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.MediterraneanTeal, fontWeight = FontWeight.Black)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = SharedJourneyColors.InkDeep, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LongTermActionBlueprintSection(journey: Journey) {
    val profile = journey.longTermProjectProfile ?: return
    val tasks = journey.tasks
    val nextTask = tasks.firstOrNull { !it.isCompleted }
    val groupedTasks = tasks
        .groupBy { it.label.ifBlank { "Plan" } }
        .toSortedMap(compareBy { label -> longTermSectionOrder(label) })

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = SharedJourneyColors.GlassWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Household Action Blueprint",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Black
            )
            Text(
                text = profile.successDefinition.ifBlank { journey.subtitle },
                style = MaterialTheme.typography.bodyMedium,
                color = SharedJourneyColors.InkMuted
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                LongTermContextChip("Milestone", profile.milestoneType, Modifier.weight(1f))
                LongTermContextChip("Timeline", profile.timeline, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                LongTermContextChip("Budget", profile.budgetStyle, Modifier.weight(1f))
                LongTermContextChip("Roadblock", profile.roadblock, Modifier.weight(1f))
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Next best action",
                        style = MaterialTheme.typography.labelSmall,
                        color = SharedJourneyColors.MediterraneanTeal,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = nextTask?.title ?: "All generated plan steps are complete. Add the next follow-up task when the milestone moves into a new phase.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SharedJourneyColors.InkDeep,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Progress by part",
                style = MaterialTheme.typography.titleSmall,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Black
            )

            if (groupedTasks.isEmpty()) {
                Text(
                    text = "No plan steps yet. Add tasks below to start tracking this milestone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SharedJourneyColors.InkMuted
                )
            } else {
                groupedTasks.forEach { (label, sectionTasks) ->
                    LongTermProgressPart(label, sectionTasks)
                }
            }
        }
    }
}

@Composable
private fun LongTermContextChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SharedJourneyColors.SunDrenchedWhite,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                text = value.ifBlank { "Not set" },
                style = MaterialTheme.typography.labelMedium,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LongTermProgressPart(label: String, tasks: List<JourneyTask>) {
    val completed = tasks.count { it.isCompleted }
    val progress = if (tasks.isEmpty()) 0f else completed.toFloat() / tasks.size

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SharedJourneyColors.SunDrenchedWhite,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SharedJourneyColors.InkDeep,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$completed/${tasks.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SharedJourneyColors.InkMuted,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = SharedJourneyColors.MediterraneanTeal,
                trackColor = SharedJourneyColors.ParchmentWarm,
            )
            tasks.take(3).forEach { task ->
                Text(
                    text = if (task.isCompleted) "Done: ${task.title}" else "Open: ${task.title}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SharedJourneyColors.InkMuted
                )
            }
        }
    }
}

private fun longTermSectionOrder(label: String): Int {
    return when (label) {
        "Next Action" -> 0
        "Plan" -> 1
        "Budget" -> 2
        "Friction" -> 3
        "Roles" -> 4
        "Follow-up" -> 5
        else -> 6
    }
}

@Composable
fun FinanceBillLedgerSection(
    journey: Journey,
    profile: FinanceProfile,
    onLogBillClick: () -> Unit,
) {
    val entries = journey.financeBillEntries
    val partnerAOwes = entries.filter { it.owedBy == "Partner A" }.sumOf { it.owedAmount }
    val partnerBOwes = entries.filter { it.owedBy == "Partner B" }.sumOf { it.owedAmount }
    val netBalance = partnerAOwes - partnerBOwes
    val balanceText = when {
        netBalance > 0.0 -> "Partner A owes Partner B ${netBalance.toMoneyText()}"
        netBalance < 0.0 -> "Partner B owes Partner A ${(-netBalance).toMoneyText()}"
        else -> "All logged variable bills are balanced"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = SharedJourneyColors.GlassWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Variable Bill Tracker",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Receipt text-to-split MVP: paste receipt text or enter the bill manually, then the app applies ${profile.billSplitStrategy.ifBlank { "the saved split rule" }}.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = balanceText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SharedJourneyColors.MediterraneanTeal,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = onLogBillClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)
            ) {
                Text("Log variable bill", fontWeight = FontWeight.Bold)
            }
            entries.takeLast(4).reversed().forEach { entry ->
                FinanceBillEntryRow(entry)
            }
        }
    }
}

@Composable
private fun FinanceBillEntryRow(entry: FinanceBillEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SharedJourneyColors.SunDrenchedWhite,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.merchant, style = MaterialTheme.typography.bodyMedium, color = SharedJourneyColors.InkDeep, fontWeight = FontWeight.Bold)
                Text(entry.amount.toMoneyText(), style = MaterialTheme.typography.bodyMedium, color = SharedJourneyColors.InkDeep, fontWeight = FontWeight.Bold)
            }
            Text("${entry.category} • ${entry.billDate} • paid by ${entry.paidBy}", style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.InkMuted)
            Text("${entry.owedBy} owes ${entry.owedTo} ${entry.owedAmount.toMoneyText()}", style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.MediterraneanTeal, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FinanceBillEntryDialog(
    journeyId: String,
    profile: FinanceProfile,
    onDismiss: () -> Unit,
    onConfirm: (FinanceBillEntry) -> Unit,
) {
    var receiptText by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var billDate by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(profile.recurringBills.firstOrNull()?.substringBefore(" (") ?: "Utilities") }
    var paidBy by remember { mutableStateOf("Partner A") }

    val amount = amountText.toAmountValue()
    val shares = calculateFinanceShares(amount, paidBy, profile)
    val canSave = merchant.isNotBlank() && billDate.isNotBlank() && amount > 0.0 && category.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log variable bill", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Paste receipt text or enter the fields manually. Camera OCR and email forwarding can feed this same parser later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SharedJourneyColors.InkMuted
                )
                TextField(
                    value = receiptText,
                    onValueChange = { receiptText = it },
                    placeholder = { Text("Paste receipt text, e.g. ENEL Energia 2026-05-16 Total \$143.20") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                        unfocusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                        focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedButton(
                    onClick = {
                        merchant = receiptText.extractMerchant().ifBlank { merchant }
                        billDate = receiptText.extractDate().ifBlank { billDate }
                        amountText = receiptText.extractAmount().ifBlank { amountText }
                    },
                    enabled = receiptText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Extract from receipt text")
                }
                TextField(value = merchant, onValueChange = { merchant = it }, placeholder = { Text("Merchant") }, modifier = Modifier.fillMaxWidth())
                TextField(value = billDate, onValueChange = { billDate = it }, placeholder = { Text("Date, e.g. 2026-05-16") }, modifier = Modifier.fillMaxWidth())
                TextField(value = amountText, onValueChange = { amountText = it }, placeholder = { Text("Amount, e.g. 143.20") }, modifier = Modifier.fillMaxWidth())
                TextField(value = category, onValueChange = { category = it }, placeholder = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Partner A", "Partner B").forEach { partner ->
                        FilterChip(
                            selected = paidBy == partner,
                            onClick = { paidBy = partner },
                            label = { Text("Paid by $partner") }
                        )
                    }
                }
                Text(
                    "${shares.owedBy} owes ${shares.owedTo} ${shares.owedAmount.toMoneyText()} (${shares.partnerAShare.toMoneyText()} A / ${shares.partnerBShare.toMoneyText()} B)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SharedJourneyColors.MediterraneanTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        FinanceBillEntry(
                            id = Clock.System.now().toEpochMilliseconds().toString(),
                            journeyId = journeyId,
                            merchant = merchant,
                            billDate = billDate,
                            amount = amount,
                            category = category,
                            paidBy = paidBy,
                            partnerAShare = shares.partnerAShare,
                            partnerBShare = shares.partnerBShare,
                            owedBy = shares.owedBy,
                            owedTo = shares.owedTo,
                            owedAmount = shares.owedAmount,
                            sourceText = receiptText,
                        )
                    )
                },
                enabled = canSave
            ) {
                Text("Save bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MonthlySpendingSection(profile: FinanceProfile) {
    var selectedPeriod by remember { mutableStateOf(SpendingPeriod.Month) }
    val monthlyTotal = profile.monthlyReportedSpendTotal
    val breakdown = profile.monthlySpendingBreakdown
    if (monthlyTotal <= 0.0 || breakdown.isEmpty()) return

    val periodTotal = monthlyTotal * selectedPeriod.monthMultiplier
    val scaledBreakdown = breakdown.map { (category, amount) ->
        category to amount * selectedPeriod.monthMultiplier
    }
    val topCategory = scaledBreakdown.maxByOrNull { it.second }
    val topShare = topCategory?.let { (_, amount) ->
        ((amount / periodTotal) * 100).toInt()
    } ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = SharedJourneyColors.GlassWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Spending Explorer",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Black
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SpendingPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period.label) }
                    )
                }
            }
            Text(
                text = "${selectedPeriod.label} reported spend: ${periodTotal.toCurrencyText(selectedPeriod)}",
                style = MaterialTheme.typography.titleLarge,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Top category: ${topCategory?.first ?: "Not enough data"}" +
                    if (topCategory != null) " (${topShare}% of reported ${selectedPeriod.label.lowercase()} spend)." else ".",
                style = MaterialTheme.typography.bodyMedium,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "These values are derived from the monthly amounts reported during setup. Receipt import or manual bill entries can refine the history over time.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )
            scaledBreakdown.forEach { (category, amount) ->
                MonthlySpendRow(
                    category = category,
                    amount = amount,
                    period = selectedPeriod,
                    share = (amount / periodTotal).toFloat()
                )
            }
        }
    }
}

@Composable
private fun MonthlySpendRow(
    category: String,
    amount: Double,
    period: SpendingPeriod,
    share: Float,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category, style = MaterialTheme.typography.bodyMedium, color = SharedJourneyColors.InkDeep, fontWeight = FontWeight.Bold)
            Text(amount.toCurrencyText(period), style = MaterialTheme.typography.bodyMedium, color = SharedJourneyColors.InkMuted, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { share.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = SharedJourneyColors.MediterraneanTeal,
            trackColor = SharedJourneyColors.ParchmentWarm,
        )
    }
}

private enum class SpendingPeriod(
    val label: String,
    val suffix: String,
    val monthMultiplier: Double,
) {
    Day("Day", "day", 1.0 / 30.0),
    Week("Week", "wk", 1.0 / 4.33),
    Month("Month", "mo", 1.0),
    Quarter("Quarter", "qtr", 3.0),
}

private data class FinanceSplitShares(
    val partnerAShare: Double,
    val partnerBShare: Double,
    val owedBy: String,
    val owedTo: String,
    val owedAmount: Double,
)

private fun calculateFinanceShares(
    amount: Double,
    paidBy: String,
    profile: FinanceProfile,
): FinanceSplitShares {
    val (partnerAPercent, partnerBPercent) = when {
        profile.billSplitStrategy.contains("Proportional", ignoreCase = true) -> {
            val partnerAIncome = profile.partnerAIncome.toAmountValue()
            val partnerBIncome = profile.partnerBIncome.toAmountValue()
            val totalIncome = partnerAIncome + partnerBIncome
            if (totalIncome > 0.0) {
                partnerAIncome / totalIncome to partnerBIncome / totalIncome
            } else {
                0.5 to 0.5
            }
        }
        profile.billSplitStrategy.contains("Custom", ignoreCase = true) -> {
            parseCustomSplit(profile.customSplitPercentages) ?: (0.5 to 0.5)
        }
        profile.billSplitStrategy.contains("assign", ignoreCase = true) -> {
            if (paidBy == "Partner A") 1.0 to 0.0 else 0.0 to 1.0
        }
        else -> 0.5 to 0.5
    }
    val partnerAShare = amount * partnerAPercent
    val partnerBShare = amount * partnerBPercent
    val owedAmount = if (paidBy == "Partner A") partnerBShare else partnerAShare
    val owedBy = when {
        owedAmount <= 0.0 -> "No one"
        paidBy == "Partner A" -> "Partner B"
        else -> "Partner A"
    }
    val owedTo = when {
        owedAmount <= 0.0 -> "No one"
        paidBy == "Partner A" -> "Partner A"
        else -> "Partner B"
    }
    return FinanceSplitShares(
        partnerAShare = partnerAShare,
        partnerBShare = partnerBShare,
        owedBy = owedBy,
        owedTo = owedTo,
        owedAmount = owedAmount,
    )
}

private fun parseCustomSplit(value: String): Pair<Double, Double>? {
    val numbers = Regex("""\d+(\.\d+)?""")
        .findAll(value)
        .mapNotNull { it.value.toDoubleOrNull() }
        .toList()
    if (numbers.size < 2) return null
    val total = numbers[0] + numbers[1]
    if (total <= 0.0) return null
    return numbers[0] / total to numbers[1] / total
}

private fun String.extractAmount(): String {
    return Regex("""(?:total|amount|due)?\s*\$?\s*(\d+[.,]\d{2})""", RegexOption.IGNORE_CASE)
        .findAll(this)
        .lastOrNull()
        ?.groupValues
        ?.getOrNull(1)
        ?.replace(",", ".")
        .orEmpty()
}

private fun String.extractDate(): String {
    return Regex("""\d{4}-\d{2}-\d{2}|\d{1,2}/\d{1,2}/\d{2,4}""")
        .find(this)
        ?.value
        .orEmpty()
}

private fun String.extractMerchant(): String {
    return lines()
        .map { it.trim() }
        .firstOrNull { line ->
            line.isNotBlank() &&
                !line.contains(Regex("""\d+[.,]\d{2}""")) &&
                !line.contains(Regex("""\d{4}-\d{2}-\d{2}|\d{1,2}/\d{1,2}/\d{2,4}"""))
        }
        .orEmpty()
}

private fun String.toAmountValue(): Double {
    return filter { it.isDigit() || it == '.' || it == ',' }
        .replace(",", ".")
        .takeIf { it.isNotBlank() }
        ?.toDoubleOrNull()
        ?: 0.0
}

private fun Double.toMoneyText(): String {
    return "\$${toInt()}"
}

private fun Double.toCurrencyText(period: SpendingPeriod): String {
    return "\$${toInt()}/${period.suffix}"
}

@Composable
private fun TaskBlockItem(
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
