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
import org.jetbrains.compose.resources.stringResource
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
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
        var selectedCategory by remember { mutableStateOf(existingJourney?.category ?: JourneyCategory.LongTermProjects) }
        var mealCookingFor by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.cookingFor ?: "3-4") }
        var mealDietaryRestrictions by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.dietaryRestrictions ?: emptyList()) }
        var mealDislikedIngredients by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.dislikedIngredients ?: "") }
        var mealBusyWeeknightCookTime by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.busyWeeknightCookTime ?: "15-30 mins") }
        var mealCookingSkillLevel by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.cookingSkillLevel ?: "Average home cook") }
        var mealLunchPreference by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.lunchPreference ?: "I love leftovers for lunch") }
        var mealRightNowGoal by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.rightNowGoal ?: "📋 Plan my entire week") }
        var mealLocationPreference by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.locationPreference ?: "Use GPS location") }
        var mealManualLocation by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.manualLocation ?: "") }

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
                                if (journeyId == null) stringResource(Res.string.edit_dream_architect_title) else stringResource(Res.string.edit_dream_edit_title),
                                fontWeight = FontWeight.Black
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                screenModel.resetArchitect()
                                navigator.pop() 
                            }) {
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
                                    category = selectedCategory,
                                    progress = 0f,
                                    participantInitials = listOf("S", "A")
                                )).copy(
                                    title = title,
                                    subtitle = subtitle,
                                    category = selectedCategory,
                                    specificGoal = specific,
                                    measurableOutcome = measurable,
                                    achievablePlan = achievable,
                                    relevanceToFamily = relevant,
                                    timeBoundDeadline = timeBound,
                                    colorHex = selectedCategory.defaultColorHex,
                                    mealPlanningProfile = if (selectedCategory == JourneyCategory.MealPlanning) {
                                        MealPlanningProfile(
                                            cookingFor = mealCookingFor,
                                            dietaryRestrictions = mealDietaryRestrictions,
                                            dislikedIngredients = mealDislikedIngredients,
                                            busyWeeknightCookTime = mealBusyWeeknightCookTime,
                                            cookingSkillLevel = mealCookingSkillLevel,
                                            lunchPreference = mealLunchPreference,
                                            rightNowGoal = mealRightNowGoal,
                                            locationPreference = mealLocationPreference,
                                            manualLocation = mealManualLocation
                                        )
                                    } else {
                                        null
                                    }
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
                            Icon(Icons.Default.Check, contentDescription = stringResource(Res.string.action_save))
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
                                    selectedCategory = selectedCategory,
                                    onCategorySelected = { selectedCategory = it },
                                    mealCookingFor = mealCookingFor,
                                    onMealCookingForChange = { mealCookingFor = it },
                                    mealDietaryRestrictions = mealDietaryRestrictions,
                                    onMealDietaryRestrictionsChange = { mealDietaryRestrictions = it },
                                    mealDislikedIngredients = mealDislikedIngredients,
                                    onMealDislikedIngredientsChange = { mealDislikedIngredients = it },
                                    mealBusyWeeknightCookTime = mealBusyWeeknightCookTime,
                                    onMealBusyWeeknightCookTimeChange = { mealBusyWeeknightCookTime = it },
                                    mealCookingSkillLevel = mealCookingSkillLevel,
                                    onMealCookingSkillLevelChange = { mealCookingSkillLevel = it },
                                    mealLunchPreference = mealLunchPreference,
                                    onMealLunchPreferenceChange = { mealLunchPreference = it },
                                    mealRightNowGoal = mealRightNowGoal,
                                    onMealRightNowGoalChange = { mealRightNowGoal = it },
                                    mealLocationPreference = mealLocationPreference,
                                    onMealLocationPreferenceChange = { mealLocationPreference = it },
                                    mealManualLocation = mealManualLocation,
                                    onMealManualLocationChange = { mealManualLocation = it },
                                    onGenerateWeeklyMealPlan = {
                                        screenModel.generateWeeklyMealPlan(
                                            MealPlanningProfile(
                                                cookingFor = mealCookingFor,
                                                dietaryRestrictions = mealDietaryRestrictions,
                                                dislikedIngredients = mealDislikedIngredients,
                                                busyWeeknightCookTime = mealBusyWeeknightCookTime,
                                                cookingSkillLevel = mealCookingSkillLevel,
                                                lunchPreference = mealLunchPreference,
                                                rightNowGoal = mealRightNowGoal,
                                                locationPreference = mealLocationPreference,
                                                manualLocation = mealManualLocation
                                            )
                                        )
                                    },
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
                stringResource(Res.string.edit_dream_seed_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = SharedJourneyColors.MediterraneanTeal,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(Res.string.edit_dream_seed_description),
                style = MaterialTheme.typography.bodyMedium,
                color = SharedJourneyColors.InkMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(stringResource(Res.string.edit_dream_seed_placeholder)) },
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
                Text(stringResource(Res.string.edit_dream_seed_button), fontWeight = FontWeight.Bold)
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
                stringResource(Res.string.edit_dream_loading_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SharedJourneyColors.MediterraneanTeal
            )
            Text(
                stringResource(Res.string.edit_dream_loading_description),
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )
        }
    }

    @Composable
    private fun ProposalReviewSection(
        title: String, onTitleChange: (String) -> Unit,
        subtitle: String, onSubtitleChange: (String) -> Unit,
        selectedCategory: JourneyCategory, onCategorySelected: (JourneyCategory) -> Unit,
        mealCookingFor: String, onMealCookingForChange: (String) -> Unit,
        mealDietaryRestrictions: List<String>, onMealDietaryRestrictionsChange: (List<String>) -> Unit,
        mealDislikedIngredients: String, onMealDislikedIngredientsChange: (String) -> Unit,
        mealBusyWeeknightCookTime: String, onMealBusyWeeknightCookTimeChange: (String) -> Unit,
        mealCookingSkillLevel: String, onMealCookingSkillLevelChange: (String) -> Unit,
        mealLunchPreference: String, onMealLunchPreferenceChange: (String) -> Unit,
        mealRightNowGoal: String, onMealRightNowGoalChange: (String) -> Unit,
        mealLocationPreference: String, onMealLocationPreferenceChange: (String) -> Unit,
        mealManualLocation: String, onMealManualLocationChange: (String) -> Unit,
        onGenerateWeeklyMealPlan: () -> Unit,
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
                        Text(stringResource(Res.string.edit_dream_proposal_badge), style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.MediterraneanTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { EditField(stringResource(Res.string.edit_dream_field_title), title, onTitleChange) }
            item { EditField(stringResource(Res.string.edit_dream_field_subtitle), subtitle, onSubtitleChange) }
            item {
                CategorySelector(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }
            if (selectedCategory == JourneyCategory.MealPlanning) {
                item {
                    MealPlanningQuestionnaire(
                        cookingFor = mealCookingFor,
                        onCookingForChange = onMealCookingForChange,
                        dietaryRestrictions = mealDietaryRestrictions,
                        onDietaryRestrictionsChange = onMealDietaryRestrictionsChange,
                        dislikedIngredients = mealDislikedIngredients,
                        onDislikedIngredientsChange = onMealDislikedIngredientsChange,
                        busyWeeknightCookTime = mealBusyWeeknightCookTime,
                        onBusyWeeknightCookTimeChange = onMealBusyWeeknightCookTimeChange,
                        cookingSkillLevel = mealCookingSkillLevel,
                        onCookingSkillLevelChange = onMealCookingSkillLevelChange,
                        lunchPreference = mealLunchPreference,
                        onLunchPreferenceChange = onMealLunchPreferenceChange,
                        rightNowGoal = mealRightNowGoal,
                        onRightNowGoalChange = onMealRightNowGoalChange,
                        locationPreference = mealLocationPreference,
                        onLocationPreferenceChange = onMealLocationPreferenceChange,
                        manualLocation = mealManualLocation,
                        onManualLocationChange = onMealManualLocationChange,
                        onGenerateWeeklyMealPlan = onGenerateWeeklyMealPlan
                    )
                }
            }

            item {
                Text(
                    stringResource(Res.string.edit_dream_smart_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SharedJourneyColors.MediterraneanTeal,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item { EditField(stringResource(Res.string.edit_dream_field_specific), specific, onSpecificChange) }
            item { EditField(stringResource(Res.string.edit_dream_field_measurable), measurable, onMeasurableChange) }
            item { EditField(stringResource(Res.string.edit_dream_field_achievable), achievable, onAchievableChange) }
            item { EditField(stringResource(Res.string.edit_dream_field_relevant), relevant, onRelevantChange) }
            item { EditField(stringResource(Res.string.edit_dream_field_timebound), timeBound, onTimeBoundChange) }

            if (suggestedTasks.isNotEmpty()) {
                item {
                    Text(
                        stringResource(Res.string.edit_dream_suggested_actions),
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
                    Text(stringResource(Res.string.edit_dream_regenerate))
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
            Text(stringResource(Res.string.edit_dream_error_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.Red.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
            Text(message, textAlign = TextAlign.Center, color = SharedJourneyColors.InkMuted)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)) {
                Text(stringResource(Res.string.edit_dream_error_back))
            }
        }
    }

    @Composable
    private fun CategorySelector(
        selectedCategory: JourneyCategory,
        onCategorySelected: (JourneyCategory) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Category",
                style = MaterialTheme.typography.labelSmall,
                color = SharedJourneyColors.InkMuted,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SharedJourneyColors.GlassWhite,
                        contentColor = SharedJourneyColors.InkDeep
                    )
                ) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text(selectedCategory.displayName, fontWeight = FontWeight.Bold)
                        Text(
                            selectedCategory.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = SharedJourneyColors.InkMuted
                        )
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SharedJourneyColors.SunDrenchedWhite)
                ) {
                    JourneyCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(category.displayName, fontWeight = FontWeight.Bold)
                                    Text(
                                        category.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SharedJourneyColors.InkMuted
                                    )
                                }
                            },
                            onClick = {
                                onCategorySelected(category)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun MealPlanningQuestionnaire(
        cookingFor: String,
        onCookingForChange: (String) -> Unit,
        dietaryRestrictions: List<String>,
        onDietaryRestrictionsChange: (List<String>) -> Unit,
        dislikedIngredients: String,
        onDislikedIngredientsChange: (String) -> Unit,
        busyWeeknightCookTime: String,
        onBusyWeeknightCookTimeChange: (String) -> Unit,
        cookingSkillLevel: String,
        onCookingSkillLevelChange: (String) -> Unit,
        lunchPreference: String,
        onLunchPreferenceChange: (String) -> Unit,
        rightNowGoal: String,
        onRightNowGoalChange: (String) -> Unit,
        locationPreference: String,
        onLocationPreferenceChange: (String) -> Unit,
        manualLocation: String,
        onManualLocationChange: (String) -> Unit,
        onGenerateWeeklyMealPlan: () -> Unit,
    ) {
        val partySizeOptions = listOf("1", "2", "3-4", "5+")
        val dietaryOptions = listOf("Vegetarian", "Vegan", "Gluten-Free", "Keto", "Nut Allergy", "None")
        val weeknightCookTimeOptions = listOf("Under 15 mins", "15-30 mins", "30-60 mins")
        val skillLevelOptions = listOf("Keep it simple", "Average home cook", "I like a culinary challenge")
        val lunchPreferenceOptions = listOf("I love leftovers for lunch", "I prefer to cook lunches fresh/separately")
        val rightNowGoalOptions = listOf(
            "📋 Plan my entire week",
            "🧊 Tell me what to make with what's in my fridge right now",
            "🛒 Just generate a quick grocery list"
        )
        val locationOptions = listOf("Use GPS location", "Provide location manually")
        val canGenerateWeeklyPlan = cookingFor.isNotBlank() &&
            dietaryRestrictions.isNotEmpty() &&
            dislikedIngredients.isNotBlank() &&
            busyWeeknightCookTime.isNotBlank() &&
            cookingSkillLevel.isNotBlank() &&
            lunchPreference.isNotBlank() &&
            rightNowGoal.isNotBlank() &&
            locationPreference.isNotBlank() &&
            (locationPreference != "Provide location manually" || manualLocation.isNotBlank())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Meal Planning Essentials",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                "These answers define what the AI can safely suggest for recipes, portions and weekly prep.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            QuestionBlock(
                title = "How many people are we cooking for?",
                description = "Scales ingredient quantities and portion sizes."
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    partySizeOptions.forEach { option ->
                        FilterChip(
                            selected = cookingFor == option,
                            onClick = { onCookingForChange(option) },
                            label = { Text(option) }
                        )
                    }
                }
            }

            QuestionBlock(
                title = "Are there any dietary restrictions or allergies?",
                description = "Acts as a hard filter for restricted foods and allergens."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    dietaryOptions.forEach { option ->
                        val isSelected = dietaryRestrictions.contains(option)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    val next = when {
                                        option == "None" && checked -> listOf("None")
                                        option == "None" -> dietaryRestrictions - option
                                        checked -> (dietaryRestrictions - "None") + option
                                        else -> dietaryRestrictions - option
                                    }
                                    onDietaryRestrictionsChange(next)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SharedJourneyColors.MediterraneanTeal)
                            )
                            Text(option, style = MaterialTheme.typography.bodyMedium, color = SharedJourneyColors.InkDeep)
                        }
                    }
                }
            }

            QuestionBlock(
                title = "Are there any ingredients you absolutely hate?",
                description = "Keeps meal plans away from foods the household dislikes."
            ) {
                TextField(
                    value = dislikedIngredients,
                    onValueChange = onDislikedIngredientsChange,
                    placeholder = { Text("Example: cilantro, mushrooms, olives") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                        unfocusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                        focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                "Lifestyle & Time Constraints",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                "These answers keep plans realistic for busy days and match the cooking effort you want.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            QuestionBlock(
                title = "On a busy weeknight, how much time do you realistically have to cook?",
                description = "Filters recipes by prep and cook time."
            ) {
                OptionChips(
                    options = weeknightCookTimeOptions,
                    selectedOption = busyWeeknightCookTime,
                    onOptionSelected = onBusyWeeknightCookTimeChange
                )
            }

            QuestionBlock(
                title = "What is your cooking style/skill level?",
                description = "Keeps meal suggestions aligned with your preferred complexity."
            ) {
                OptionChips(
                    options = skillLevelOptions,
                    selectedOption = cookingSkillLevel,
                    onOptionSelected = onCookingSkillLevelChange
                )
            }

            QuestionBlock(
                title = "How do you prefer to handle lunches?",
                description = "Lets the AI decide whether to scale dinners for leftovers."
            ) {
                OptionChips(
                    options = lunchPreferenceOptions,
                    selectedOption = lunchPreference,
                    onOptionSelected = onLunchPreferenceChange
                )
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                "Right Now Friction Solver",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                "Use this when you open the app in a panic and need the AI to pick the right behavior mode immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            QuestionBlock(
                title = "What's your goal today?",
                description = "Sets the AI mode: batch planning, inventory clearance, or grocery logistics."
            ) {
                OptionChips(
                    options = rightNowGoalOptions,
                    selectedOption = rightNowGoal,
                    onOptionSelected = onRightNowGoalChange
                )
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                "Local Shopping Context",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                "Allow GPS geolocation or provide your location manually so meal plans and grocery lists can be tailored with nearby shops, seasonal local products, and realistic availability.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            QuestionBlock(
                title = "How should we localize grocery suggestions?",
                description = "This helps tailor grocery lists to local shops and products."
            ) {
                OptionChips(
                    options = locationOptions,
                    selectedOption = locationPreference,
                    onOptionSelected = onLocationPreferenceChange
                )
            }

            if (locationPreference == "Provide location manually") {
                QuestionBlock(
                    title = "Where should we plan around?",
                    description = "Add a city, neighborhood, postal code, or market area."
                ) {
                    TextField(
                        value = manualLocation,
                        onValueChange = onManualLocationChange,
                        placeholder = { Text("Example: Portici, Naples or 80055") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                            unfocusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                            focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Button(
                onClick = onGenerateWeeklyMealPlan,
                enabled = canGenerateWeeklyPlan,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)
            ) {
                Icon(AppIcons.Sparkles, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Generate weekly meal plan with AI", fontWeight = FontWeight.Bold)
            }

            if (!canGenerateWeeklyPlan) {
                Text(
                    "Complete each Meal Planning answer first so the AI can build a realistic weekly plan.",
                    style = MaterialTheme.typography.labelSmall,
                    color = SharedJourneyColors.InkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    @Composable
    private fun OptionChips(
        options: List<String>,
        selectedOption: String,
        onOptionSelected: (String) -> Unit
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedOption == option,
                    onClick = { onOptionSelected(option) },
                    label = { Text(option) }
                )
            }
        }
    }

    @Composable
    private fun QuestionBlock(
        title: String,
        description: String,
        content: @Composable () -> Unit
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = SharedJourneyColors.InkDeep, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.InkMuted)
            content()
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
