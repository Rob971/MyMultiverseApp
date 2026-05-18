package app.mymultiverse.kmp.presentation.screens.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import app.mymultiverse.kmp.domain.model.Journey
import app.mymultiverse.kmp.domain.model.JourneyTask
import app.mymultiverse.kmp.presentation.components.NapolitanBackground
import app.mymultiverse.kmp.presentation.components.TaskEditDialog
import app.mymultiverse.kmp.presentation.screens.home.HomeScreenModel
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.minus
import org.jetbrains.compose.ui.tooling.preview.Preview


enum class CalendarViewMode { DAY, WEEK, MONTH, QUARTER, YEAR }

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
        var currentDate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
        var selectedDate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
        val daysOfWeek = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")

        var showAddTaskDialog by remember { mutableStateOf(false) }
        var editingTask by remember { mutableStateOf<JourneyTask?>(null) }

        val visibleTasks = when (val s = scope) {
            is CalendarScope.Global -> journeys.flatMap { j -> j.tasks.map { t -> j to t } }
            is CalendarScope.Goal -> journeys.find { it.id == s.journeyId }?.let { j -> j.tasks.map { t -> j to t } } ?: emptyList()
            is CalendarScope.Task -> journeys.find { it.id == s.journeyId }?.tasks?.find { it.id == s.taskId }?.let { t -> 
                listOf(journeys.find { it.id == s.journeyId }!! to t)
            } ?: emptyList()
        }

        val filteredTasks = if (scope is CalendarScope.Task) {
            visibleTasks
        } else {
            visibleTasks.filter { it.second.scheduledDays.contains(selectedDate.dayOfWeek.ordinal + 1) }
        }

        NapolitanBackground {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                text = when (scope) {
                                    is CalendarScope.Global -> stringResource(Res.string.calendar_global_title)
                                    is CalendarScope.Goal -> stringResource(Res.string.calendar_goal_title)
                                    is CalendarScope.Task -> stringResource(Res.string.calendar_task_title)
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(AppIcons.ArrowBack, contentDescription = stringResource(Res.string.action_back))
                            }
                        },
                        actions = {
                            when (scope) {
                                is CalendarScope.Global, is CalendarScope.Goal -> {
                                    IconButton(onClick = { showAddTaskDialog = true }) {
                                        Icon(AppIcons.Add, contentDescription = stringResource(Res.string.action_add_event))
                                    }
                                }
                                is CalendarScope.Task -> {
                                    val currentPair = visibleTasks.firstOrNull()
                                    if (currentPair != null) {
                                        IconButton(onClick = { editingTask = currentPair.second }) {
                                            Icon(AppIcons.Edit, contentDescription = stringResource(Res.string.action_edit))
                                        }
                                        IconButton(onClick = { 
                                            screenModel.deleteTask(currentPair.first.id, currentPair.second.id)
                                            navigator.pop()
                                        }) {
                                            Icon(AppIcons.Delete, contentDescription = stringResource(Res.string.action_delete), tint = Color.Red.copy(alpha = 0.7f))
                                        }
                                    }
                                }
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
                            CalendarHeader(
                                mode = viewMode,
                                currentDate = currentDate,
                                onDatePrev = {
                                    val period = when(viewMode) {
                                        CalendarViewMode.DAY -> DatePeriod(days = 1)
                                        CalendarViewMode.WEEK -> DatePeriod(days = 7)
                                        CalendarViewMode.MONTH -> DatePeriod(months = 1)
                                        CalendarViewMode.QUARTER -> DatePeriod(months = 3)
                                        CalendarViewMode.YEAR -> DatePeriod(years = 1)
                                    }
                                    currentDate = currentDate.minus(period)
                                },
                                onDateNext = {
                                    val period = when(viewMode) {
                                        CalendarViewMode.DAY -> DatePeriod(days = 1)
                                        CalendarViewMode.WEEK -> DatePeriod(days = 7)
                                        CalendarViewMode.MONTH -> DatePeriod(months = 1)
                                        CalendarViewMode.QUARTER -> DatePeriod(months = 3)
                                        CalendarViewMode.YEAR -> DatePeriod(years = 1)
                                    }
                                    currentDate = currentDate.plus(period)
                                }
                            )
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(28.dp))
                                    .padding(20.dp)
                            ) {
                                when (viewMode) {
                                    CalendarViewMode.DAY -> DayView(
                                        filteredTasks,
                                        onTaskClick = { editingTask = it }
                                    )
                                    CalendarViewMode.WEEK -> WeekView(
                                        tasks = visibleTasks,
                                        days = daysOfWeek,
                                        currentDate = currentDate,
                                        selectedDate = selectedDate,
                                        onDateSelected = { selectedDate = it }
                                    )
                                    CalendarViewMode.MONTH -> MonthView(
                                        currentDate = currentDate,
                                        selectedDate = selectedDate,
                                        tasks = visibleTasks,
                                        onDateSelected = { selectedDate = it }
                                    )
                                    CalendarViewMode.QUARTER -> QuarterView(
                                        currentDate = currentDate,
                                        journeys = journeys,
                                        onMonthSelected = { monthDate ->
                                            currentDate = monthDate
                                            viewMode = CalendarViewMode.MONTH
                                        }
                                    )
                                    CalendarViewMode.YEAR -> YearView(
                                        journeys = journeys,
                                        onQuarterSelected = { quarterIndex ->
                                            val firstMonthOfQuarter = (quarterIndex * 3) + 1
                                            currentDate = LocalDate(currentDate.year, firstMonthOfQuarter, 1)
                                            viewMode = CalendarViewMode.QUARTER
                                        }
                                    )
                                }
                            }
                        }

                        if (scope is CalendarScope.Task) {
                            val currentPair = visibleTasks.firstOrNull()
                            if (currentPair != null) {
                                item {
                                    WeekdaySelector(
                                        task = currentPair.second,
                                        daysOfWeek = daysOfWeek,
                                        onToggleDay = { updatedTask ->
                                            screenModel.updateTask(updatedTask)
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = if (selectedDate == Clock.System.todayIn(TimeZone.currentSystemDefault())) stringResource(Res.string.calendar_today_agenda) 
                                       else stringResource(Res.string.calendar_date_agenda, "${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase()}"),
                                style = MaterialTheme.typography.titleMedium,
                                color = SharedJourneyColors.InkDeep,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (filteredTasks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        stringResource(Res.string.calendar_no_activities),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SharedJourneyColors.InkMuted
                                    )
                                }
                            }
                        }

                        items(filteredTasks) { (journey, task) ->
                            AgendaItem(
                                journey = journey,
                                task = task,
                                days = daysOfWeek,
                                onToggleComplete = { screenModel.updateTask(it) },
                                onClick = { editingTask = it }
                            )
                        }
                    }
                }
            }

            if (showAddTaskDialog) {
                val targetJourneyId = when (val s = scope) {
                    is CalendarScope.Goal -> s.journeyId
                    else -> journeys.firstOrNull()?.id ?: ""
                }
                if (targetJourneyId.isNotEmpty()) {
                    TaskEditDialog(
                        journeyId = targetJourneyId,
                        onDismiss = { showAddTaskDialog = false },
                        onConfirm = {
                            screenModel.addTask(targetJourneyId, it)
                            showAddTaskDialog = false
                        }
                    )
                }
            }

            editingTask?.let { task ->
                TaskEditDialog(
                    journeyId = task.journeyId,
                    task = task,
                    onDismiss = { editingTask = null },
                    onConfirm = {
                        screenModel.updateTask(it)
                        editingTask = null
                    }
                )
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
                                    CalendarViewMode.DAY -> stringResource(Res.string.calendar_view_day)
                                    CalendarViewMode.WEEK -> stringResource(Res.string.calendar_view_week)
                                    CalendarViewMode.MONTH -> stringResource(Res.string.calendar_view_month)
                                    CalendarViewMode.QUARTER -> stringResource(Res.string.calendar_view_quarter)
                                    CalendarViewMode.YEAR -> stringResource(Res.string.calendar_view_year)
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
    private fun CalendarHeader(
        mode: CalendarViewMode,
        currentDate: LocalDate,
        onDatePrev: () -> Unit,
        onDateNext: () -> Unit,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDatePrev) {
                Icon(AppIcons.ChevronLeft, contentDescription = "Previous")
            }
            Text(
                text = formatDate(mode, currentDate),
                style = MaterialTheme.typography.headlineSmall,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Black
            )
            IconButton(onClick = onDateNext) {
                Icon(AppIcons.ChevronRight, contentDescription = "Next")
            }
        }
    }

    @Composable
    private fun formatDate(mode: CalendarViewMode, date: LocalDate): String {
        val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return when (mode) {
            CalendarViewMode.DAY -> "Oggi, ${date.dayOfMonth} $month"
            CalendarViewMode.WEEK -> {
                val startOfWeek = date.minus(DatePeriod(days = date.dayOfWeek.ordinal))
                val endOfWeek = startOfWeek.plus(DatePeriod(days = 6))
                val startMonth = startOfWeek.month.name.lowercase().replaceFirstChar { it.uppercase() }
                val endMonth = endOfWeek.month.name.lowercase().replaceFirstChar { it.uppercase() }

                if (startMonth == endMonth) {
                    "${startOfWeek.dayOfMonth} - ${endOfWeek.dayOfMonth} $startMonth, ${date.year}"
                } else {
                    "${startOfWeek.dayOfMonth} $startMonth - ${endOfWeek.dayOfMonth} $endMonth, ${date.year}"
                }
            }
            CalendarViewMode.MONTH -> "$month ${date.year}"
            CalendarViewMode.QUARTER -> {
                val quarter = (date.monthNumber - 1) / 3 + 1
                stringResource(Res.string.calendar_quarter_q, "$quarter, ${date.year}")
            }
            CalendarViewMode.YEAR -> stringResource(Res.string.calendar_year_roadmap, "${date.year}")
        }
    }

    @Composable
    private fun DayView(
        tasks: List<Pair<Journey, JourneyTask>>,
        onTaskClick: (JourneyTask) -> Unit
    ) {
        val hours = (8..22).map { "${it.toString().padStart(2, '0')}:00" }
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            hours.forEach { time ->
                val tasksAtTime = tasks.filter { 
                    it.second.reminderTime?.startsWith(time.take(2)) == true 
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall,
                        color = SharedJourneyColors.InkMuted,
                        modifier = Modifier.width(48.dp).padding(top = 12.dp)
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(
                            color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(top = 20.dp)
                        )
                        
                        tasksAtTime.forEach { (journey, task) ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTaskClick(task) },
                                color = parseColor(journey.colorHex).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, parseColor(journey.colorHex).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp, 24.dp)
                                            .background(parseColor(journey.colorHex), RoundedCornerShape(2.dp))
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            task.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SharedJourneyColors.InkDeep
                                        )
                                        Text(
                                            journey.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SharedJourneyColors.InkMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WeekView(
        tasks: List<Pair<Journey, JourneyTask>>,
        days: List<String>,
        currentDate: LocalDate,
        selectedDate: LocalDate,
        onDateSelected: (LocalDate) -> Unit
    ) {
        val startOfWeek = currentDate.minus(DatePeriod(days = currentDate.dayOfWeek.ordinal))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { index, day ->
                val dayNum = index + 1
                val date = startOfWeek.plus(DatePeriod(days = index))
                val isSelected = date == selectedDate
                val isToday = date == Clock.System.todayIn(TimeZone.currentSystemDefault())
                val tasksOnDay = tasks.filter { it.second.scheduledDays.contains(dayNum) }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onDateSelected(date) }
                        .background(
                            if (isSelected) SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) SharedJourneyColors.MediterraneanTeal else SharedJourneyColors.InkMuted,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = when {
                            isSelected -> SharedJourneyColors.MediterraneanTeal
                            isToday -> SharedJourneyColors.TerracottaOrange.copy(alpha = 0.1f)
                            else -> Color.Transparent
                        },
                        border = if (isToday && !isSelected) BorderStroke(2.dp, SharedJourneyColors.TerracottaOrange) else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${date.dayOfMonth}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSelected -> Color.White
                                    isToday -> SharedJourneyColors.TerracottaOrange
                                    else -> SharedJourneyColors.InkDeep
                                },
                                fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Density indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.height(4.dp)
                    ) {
                        tasksOnDay.take(3).forEach { (journey, _) ->
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(parseColor(journey.colorHex), CircleShape)
                            )
                        }
                        if (tasksOnDay.size > 3) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(SharedJourneyColors.InkMuted, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MonthView(
        currentDate: LocalDate,
        selectedDate: LocalDate,
        tasks: List<Pair<Journey, JourneyTask>>,
        onDateSelected: (LocalDate) -> Unit
    ) {
        val firstDayOfMonth = LocalDate(currentDate.year, currentDate.month, 1)
        val lastDayOfMonth = firstDayOfMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
        val daysInMonth = lastDayOfMonth.dayOfMonth
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val startDayOfWeek = (firstDayOfMonth.dayOfWeek.ordinal) % 7 // 0 for Monday
        val emptyDays = startDayOfWeek

        val totalCells = if ((daysInMonth + emptyDays) % 7 == 0) {
            daysInMonth + emptyDays
        } else {
            ((daysInMonth + emptyDays) / 7 + 1) * 7
        }
        val days = (1..totalCells).map { i ->
            val day = i - emptyDays
            if (day > 0 && day <= daysInMonth) day else 0
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { dayNum ->
                        val date = if (dayNum > 0) LocalDate(currentDate.year, currentDate.month, dayNum) else null
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val tasksOnDay = date?.let { d -> 
                            tasks.filter { it.second.scheduledDays.contains(d.dayOfWeek.ordinal + 1) }
                        } ?: emptyList()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = date != null) { date?.let { onDateSelected(it) } }
                                .background(
                                    when {
                                        isSelected -> SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.15f)
                                        dayNum > 0 -> SharedJourneyColors.ParchmentWarm.copy(alpha = 0.5f)
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (isToday) Modifier.border(2.dp, SharedJourneyColors.TerracottaOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNum > 0) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 12.sp,
                                        color = when {
                                            isSelected -> SharedJourneyColors.MediterraneanTeal
                                            isToday -> SharedJourneyColors.TerracottaOrange
                                            else -> SharedJourneyColors.InkDeep
                                        },
                                        fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Medium
                                    )
                                    
                                    if (tasksOnDay.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            tasksOnDay.take(3).forEach { (journey, _) ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(parseColor(journey.colorHex), CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun QuarterView(
        currentDate: LocalDate,
        journeys: List<Journey>,
        onMonthSelected: (LocalDate) -> Unit
    ) {
        val quarterIndex = (currentDate.monthNumber - 1) / 3
        val quarterThemes = listOf(
            stringResource(Res.string.calendar_quarter_1_title) to stringResource(Res.string.calendar_quarter_1_subtitle),
            stringResource(Res.string.calendar_quarter_2_title) to stringResource(Res.string.calendar_quarter_2_subtitle),
            stringResource(Res.string.calendar_quarter_3_title) to stringResource(Res.string.calendar_quarter_3_subtitle),
            stringResource(Res.string.calendar_quarter_4_title) to stringResource(Res.string.calendar_quarter_4_subtitle)
        )
        val (title, subtitle) = quarterThemes[quarterIndex]

        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Quarter Header
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = SharedJourneyColors.MediterraneanTeal
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SharedJourneyColors.InkMuted
                )
            }

            // 3 Month Grids
            val firstMonth = (quarterIndex * 3) + 1
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                for (m in 0..2) {
                    val monthDate = LocalDate(currentDate.year, firstMonth + m, 1)
                    val monthName = monthDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
                    
                    Surface(
                        color = SharedJourneyColors.SunDrenchedWhite.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMonthSelected(monthDate) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = monthName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SharedJourneyColors.InkDeep
                            )
                            Spacer(Modifier.height(8.dp))
                            // Mini month view or summary of tasks
                            val journeysInMonth = journeys.filter { it.progress < 100 }
                            if (journeysInMonth.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    journeysInMonth.take(4).forEach { j ->
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(parseColor(j.colorHex), CircleShape)
                                        )
                                    }
                                }
                            } else {
                                Text(stringResource(Res.string.calendar_no_active_goals), style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.InkMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun YearView(
        journeys: List<Journey>,
        onQuarterSelected: (Int) -> Unit
    ) {
        val quarters = listOf(
            Triple(stringResource(Res.string.calendar_quarter_1_title), stringResource(Res.string.calendar_quarter_1_subtitle), SharedJourneyColors.MediterraneanTeal),
            Triple(stringResource(Res.string.calendar_quarter_2_title), stringResource(Res.string.calendar_quarter_2_subtitle), SharedJourneyColors.LemonZestYellow),
            Triple(stringResource(Res.string.calendar_quarter_3_title), stringResource(Res.string.calendar_quarter_3_subtitle), SharedJourneyColors.TerracottaOrange),
            Triple(stringResource(Res.string.calendar_quarter_4_title), stringResource(Res.string.calendar_quarter_4_subtitle), SharedJourneyColors.SageSoft)
        )

        // Mock progress for demo purposes, or calculate based on journeys
        val overallProgress = if (journeys.isEmpty()) 0f else journeys.map { it.progress }.average().toFloat() / 100f

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            quarters.forEachIndexed { i, (title, subtitle, color) ->
                // Simulated progress per quarter for visualization
                val progress = when(i) {
                    0 -> 1.0f
                    1 -> (overallProgress * 4).coerceIn(0f, 1f)
                    2 -> (overallProgress * 4 - 1).coerceIn(0f, 1f)
                    else -> (overallProgress * 4 - 2).coerceIn(0f, 1f)
                }

                Surface(
                    color = SharedJourneyColors.GlassWhite,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onQuarterSelected(i) },
                    shadowElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Q${i + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = color
                                )
                            }
                            
                            Spacer(Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = SharedJourneyColors.InkDeep
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SharedJourneyColors.InkMuted
                                )
                            }

                            Icon(
                                imageVector = AppIcons.ChevronRight,
                                contentDescription = null,
                                tint = SharedJourneyColors.InkMuted.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(Res.string.calendar_progress),
                                style = MaterialTheme.typography.labelSmall,
                                color = SharedJourneyColors.InkMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = color,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = color.copy(alpha = 0.1f)
                        )
                        
                        if (i == 1) { // Current quarter example
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = SharedJourneyColors.TerracottaOrange.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        stringResource(Res.string.calendar_in_progress),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SharedJourneyColors.TerracottaOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(Res.string.calendar_active_goals, "3"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SharedJourneyColors.InkMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WeekdaySelector(
        task: JourneyTask,
        daysOfWeek: List<String>,
        onToggleDay: (JourneyTask) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(Res.string.calendar_weekly_planning),
                style = MaterialTheme.typography.titleSmall,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    val dayNum = index + 1
                    val isSelected = task.scheduledDays.contains(dayNum)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                val newDays = if (isSelected) {
                                    task.scheduledDays.filter { it != dayNum }
                                } else {
                                    (task.scheduledDays + dayNum).sorted()
                                }
                                onToggleDay(task.copy(scheduledDays = newDays))
                            }
                            .padding(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = if (isSelected) SharedJourneyColors.MediterraneanTeal else SharedJourneyColors.ParchmentWarm,
                            border = if (isSelected) null else BorderStroke(1.dp, SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day.first().toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else SharedJourneyColors.InkMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AgendaItem(
        journey: Journey,
        task: JourneyTask,
        days: List<String>,
        onToggleComplete: (JourneyTask) -> Unit,
        onClick: (JourneyTask) -> Unit
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(task) },
            color = SharedJourneyColors.GlassWhite,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 0.dp
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onToggleComplete(task.copy(isCompleted = !task.isCompleted)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) AppIcons.CheckCircle else AppIcons.RadioButtonUnchecked,
                        contentDescription = "Toggle Complete",
                        tint = if (task.isCompleted) SharedJourneyColors.SageSoft else SharedJourneyColors.InkMuted
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                Box(modifier = Modifier.size(12.dp).background(parseColor(journey.colorHex), CircleShape))
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (task.isCompleted) SharedJourneyColors.InkMuted else SharedJourneyColors.InkDeep,
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                    Text(
                        text = "${journey.title} • ${task.scheduledDays.joinToString(", ") { days[it-1] }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SharedJourneyColors.InkMuted
                    )
                }
                if (task.reminderTime != null) {
                    Icon(AppIcons.Notifications, contentDescription = null, modifier = Modifier.size(20.dp), tint = SharedJourneyColors.TerracottaOrange)
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

@Preview
@Composable
fun CalendarScreenPreview() {
    MaterialTheme {
        CalendarScreen(scope = CalendarScope.Global).Content()
    }
}
