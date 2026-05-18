package app.mymultiverse.kmp.presentation.screens.home

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
import app.mymultiverse.kmp.domain.model.*
import app.mymultiverse.kmp.presentation.components.NapolitanBackground
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
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
        var planItems by remember { mutableStateOf(existingJourney?.planItems ?: emptyList()) }
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
        var financeSplit by remember { mutableStateOf(existingJourney?.financeProfile?.financeSplit ?: "A mix (joint account for bills, separate for personal)") }
        var financeBillManager by remember { mutableStateOf(existingJourney?.financeProfile?.billManager ?: "We try to do it together") }
        var financeDailyAnnoyance by remember { mutableStateOf(existingJourney?.financeProfile?.dailyAnnoyance ?: "Forgetting when bills are due") }
        var financePartnerASpendingStyle by remember { mutableStateOf(existingJourney?.financeProfile?.partnerASpendingStyle ?: "3") }
        var financePartnerBSpendingStyle by remember { mutableStateOf(existingJourney?.financeProfile?.partnerBSpendingStyle ?: "3") }
        var financeMoneyTalkFrequency by remember { mutableStateOf(existingJourney?.financeProfile?.moneyTalkFrequency ?: "Regularly (monthly/weekly check-ins)") }
        var financePrimaryGoal by remember { mutableStateOf(existingJourney?.financeProfile?.primaryGoal ?: "Just gaining peace of mind") }
        var financeIrregularExpensePlan by remember { mutableStateOf(existingJourney?.financeProfile?.irregularExpensePlan ?: "No, we just handle them as they hit our credit cards") }
        var financeBillSplitStrategy by remember { mutableStateOf(existingJourney?.financeProfile?.billSplitStrategy ?: "50/50 (Right down the middle)") }
        var financeSettleWorkflow by remember { mutableStateOf(existingJourney?.financeProfile?.settleWorkflow ?: "We send Venmos/Zelles back and forth constantly") }
        var financeRecurringBills by remember { mutableStateOf(existingJourney?.financeProfile?.recurringBills ?: emptyList()) }
        var financeBillPainPoint by remember { mutableStateOf(existingJourney?.financeProfile?.billPainPoint ?: "Forgetting when a bill is due (Late fees)") }
        var financePartnerAIncome by remember { mutableStateOf(existingJourney?.financeProfile?.partnerAIncome ?: "") }
        var financePartnerBIncome by remember { mutableStateOf(existingJourney?.financeProfile?.partnerBIncome ?: "") }
        var financeCustomSplitPercentages by remember { mutableStateOf(existingJourney?.financeProfile?.customSplitPercentages ?: "") }
        var financeMonthlyHousingSpend by remember { mutableStateOf(existingJourney?.financeProfile?.monthlyHousingSpend ?: "") }
        var financeMonthlyUtilitiesSpend by remember { mutableStateOf(existingJourney?.financeProfile?.monthlyUtilitiesSpend ?: "") }
        var financeMonthlyConnectivitySpend by remember { mutableStateOf(existingJourney?.financeProfile?.monthlyConnectivitySpend ?: "") }
        var financeMonthlySubscriptionsSpend by remember { mutableStateOf(existingJourney?.financeProfile?.monthlySubscriptionsSpend ?: "") }
        var financeMonthlyInsuranceSpend by remember { mutableStateOf(existingJourney?.financeProfile?.monthlyInsuranceSpend ?: "") }
        var financeMonthlyKidsPetsSpend by remember { mutableStateOf(existingJourney?.financeProfile?.monthlyKidsPetsSpend ?: "") }
        var financeMonthlyOtherSpend by remember { mutableStateOf(existingJourney?.financeProfile?.monthlyOtherSpend ?: "") }
        var healthConflictTopic by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.conflictTopic ?: "") }
        var healthConflictDraft by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.conflictDraft ?: "") }
        var healthPartnerLoveLanguage by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.partnerLoveLanguage ?: "Acts of Service") }
        var healthAvailableTime by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.availableTime ?: "15 minutes") }
        var healthBudget by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.budget ?: "\$10") }
        var healthEnergyLevel by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.energyLevel ?: "5") }
        var healthStressLevel by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.stressLevel ?: "5") }
        var healthConnectionLevel by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.connectionLevel ?: "5") }
        var healthWeeklyDrain by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.weeklyDrain ?: "Work") }
        var healthDateNightDuration by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.dateNightDuration ?: "2 hours") }
        var healthDateNightLikes by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.dateNightLikes ?: "") }
        var healthDateNightAvoids by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.dateNightAvoids ?: "Watching a movie") }
        var longTermMilestoneType by remember { mutableStateOf(existingJourney?.longTermProjectProfile?.milestoneType ?: "Moving to a new home") }
        var longTermRoadblock by remember { mutableStateOf(existingJourney?.longTermProjectProfile?.roadblock ?: "Overwhelmed: We don't know the correct order of steps to take.") }
        var longTermTimeline by remember { mutableStateOf(existingJourney?.longTermProjectProfile?.timeline ?: "Medium-term (1-6 months)") }
        var longTermBudgetStyle by remember { mutableStateOf(existingJourney?.longTermProjectProfile?.budgetStyle ?: "Moderate (Some DIY, hiring pros for the big stuff)") }
        var longTermSuccessDefinition by remember { mutableStateOf(existingJourney?.longTermProjectProfile?.successDefinition ?: "") }
        var hasStartedDreamCard by remember { mutableStateOf(journeyId != null) }
        val categoryOutcomes = JourneyCategory.entries.associateWith { category ->
            stringResource(category.experienceResources.outcome)
        }
        val suggestedTaskCopy = SuggestedTaskCopy(
            grocery = stringResource(Res.string.task_label_grocery),
            prep = stringResource(Res.string.task_label_prep),
            lunch = stringResource(Res.string.task_label_lunch),
            menu = stringResource(Res.string.task_label_menu),
            splitRule = stringResource(Res.string.task_label_split_rule),
            settle = stringResource(Res.string.task_label_settle),
            goal = stringResource(Res.string.task_label_goal),
            ledger = stringResource(Res.string.task_label_ledger),
            deEscalator = stringResource(Res.string.task_label_de_escalator),
            dateNight = stringResource(Res.string.task_label_date_night),
            checkIn = stringResource(Res.string.task_label_check_in),
            care = stringResource(Res.string.task_label_care),
            plan = stringResource(Res.string.task_label_plan),
            budget = stringResource(Res.string.task_label_budget),
            friction = stringResource(Res.string.task_label_friction),
            roles = stringResource(Res.string.task_label_roles),
            followUp = stringResource(Res.string.task_label_follow_up),
            nextAction = stringResource(Res.string.task_label_next_action),
            groceryPlanning = stringResource(Res.string.task_planning_grocery),
            prepPlanning = stringResource(Res.string.task_planning_prep),
            menuPlanning = stringResource(Res.string.task_planning_menu),
            splitRulePlanning = stringResource(Res.string.task_planning_split_rule),
            settlePlanning = stringResource(Res.string.task_planning_settle),
            ledgerPlanning = stringResource(Res.string.task_planning_ledger),
            deEscalatorPlanning = stringResource(Res.string.task_planning_de_escalator),
            dateNightPlanning = stringResource(Res.string.task_planning_date_night),
            checkInPlanning = stringResource(Res.string.task_planning_check_in),
            categoryDefaultPlanning = stringResource(Res.string.task_planning_category_default),
            longTermNextActionPlanning = stringResource(Res.string.task_planning_long_term_next_action),
            longTermBudgetPlanning = stringResource(Res.string.task_planning_long_term_budget),
            longTermFrictionPlanning = stringResource(Res.string.task_planning_long_term_friction),
            longTermRolesPlanning = stringResource(Res.string.task_planning_long_term_roles),
            longTermFollowUpPlanning = stringResource(Res.string.task_planning_long_term_follow_up),
            longTermPlanPlanning = stringResource(Res.string.task_planning_long_term_plan),
        )

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
                planItems = proposal.planItems
            }
        }

        fun selectCategory(category: JourneyCategory) {
            selectedCategory = category
            subtitle = categoryOutcomes.getValue(category)
            selectedTasks = emptyList()
            planItems = emptyList()
        }

        val hasGeneratedAiPlan = selectedTasks.isNotEmpty() || planItems.isNotEmpty() || journeyId != null

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
                    if (hasGeneratedAiPlan) {
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
                                    planItems = planItems,
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
                                    },
                                    financeProfile = if (selectedCategory == JourneyCategory.HouseholdFinance) {
                                        FinanceProfile(
                                            financeSplit = financeSplit,
                                            billManager = financeBillManager,
                                            dailyAnnoyance = financeDailyAnnoyance,
                                            partnerASpendingStyle = financePartnerASpendingStyle,
                                            partnerBSpendingStyle = financePartnerBSpendingStyle,
                                            moneyTalkFrequency = financeMoneyTalkFrequency,
                                            primaryGoal = financePrimaryGoal,
                                            irregularExpensePlan = financeIrregularExpensePlan,
                                            billSplitStrategy = financeBillSplitStrategy,
                                            settleWorkflow = financeSettleWorkflow,
                                            recurringBills = financeRecurringBills,
                                            billPainPoint = financeBillPainPoint,
                                            partnerAIncome = financePartnerAIncome,
                                            partnerBIncome = financePartnerBIncome,
                                            customSplitPercentages = financeCustomSplitPercentages,
                                            monthlyHousingSpend = financeMonthlyHousingSpend,
                                            monthlyUtilitiesSpend = financeMonthlyUtilitiesSpend,
                                            monthlyConnectivitySpend = financeMonthlyConnectivitySpend,
                                            monthlySubscriptionsSpend = financeMonthlySubscriptionsSpend,
                                            monthlyInsuranceSpend = financeMonthlyInsuranceSpend,
                                            monthlyKidsPetsSpend = financeMonthlyKidsPetsSpend,
                                            monthlyOtherSpend = financeMonthlyOtherSpend
                                        )
                                    } else {
                                        null
                                    },
                                    healthWellnessProfile = if (selectedCategory == JourneyCategory.HealthWellness) {
                                        HealthWellnessProfile(
                                            conflictTopic = healthConflictTopic,
                                            conflictDraft = healthConflictDraft,
                                            partnerLoveLanguage = healthPartnerLoveLanguage,
                                            availableTime = healthAvailableTime,
                                            budget = healthBudget,
                                            energyLevel = healthEnergyLevel,
                                            stressLevel = healthStressLevel,
                                            connectionLevel = healthConnectionLevel,
                                            weeklyDrain = healthWeeklyDrain,
                                            dateNightDuration = healthDateNightDuration,
                                            dateNightLikes = healthDateNightLikes,
                                            dateNightAvoids = healthDateNightAvoids
                                        )
                                    } else {
                                        null
                                    },
                                    longTermProjectProfile = if (selectedCategory == JourneyCategory.LongTermProjects) {
                                        LongTermProjectProfile(
                                            milestoneType = longTermMilestoneType,
                                            roadblock = longTermRoadblock,
                                            timeline = longTermTimeline,
                                            budgetStyle = longTermBudgetStyle,
                                            successDefinition = longTermSuccessDefinition
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
                                        planning = suggestedTaskPlanning(selectedCategory, taskTitle, suggestedTaskCopy),
                                        isCompleted = false,
                                        label = suggestedTaskLabel(selectedCategory, taskTitle, suggestedTaskCopy),
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
                    val contentState = when {
                        journeyId != null -> ArchitectState.Proposed(
                            SmartGoalProposal(title, subtitle, specific, measurable, achievable, relevant, timeBound, emptyList(), planItems)
                        )
                        architectState is ArchitectState.Refining ||
                            architectState is ArchitectState.Error ||
                            architectState is ArchitectState.Proposed -> architectState
                        hasStartedDreamCard -> ArchitectState.Proposed(
                            SmartGoalProposal(title, subtitle, specific, measurable, achievable, relevant, timeBound, emptyList(), planItems)
                        )
                        else -> ArchitectState.Idle
                    }

                    AnimatedContent(
                        targetState = contentState,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }
                    ) { state ->
                        when (state) {
                            is ArchitectState.Idle -> {
                                DreamCardIntakeSection(
                                    value = seedText,
                                    onValueChange = { seedText = it },
                                    selectedCategory = selectedCategory,
                                    onCategorySelected = { selectCategory(it) },
                                    onContinue = {
                                        val dream = seedText.trim()
                                        title = dream
                                        subtitle = categoryOutcomes.getValue(selectedCategory)
                                        specific = dream
                                        measurable = ""
                                        achievable = ""
                                        relevant = ""
                                        timeBound = ""
                                        selectedTasks = emptyList()
                                        planItems = emptyList()
                                        hasStartedDreamCard = true
                                    }
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
                                    onCategorySelected = { selectCategory(it) },
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
                                    financeSplit = financeSplit,
                                    onFinanceSplitChange = { financeSplit = it },
                                    financeBillManager = financeBillManager,
                                    onFinanceBillManagerChange = { financeBillManager = it },
                                    financeDailyAnnoyance = financeDailyAnnoyance,
                                    onFinanceDailyAnnoyanceChange = { financeDailyAnnoyance = it },
                                    financePartnerASpendingStyle = financePartnerASpendingStyle,
                                    onFinancePartnerASpendingStyleChange = { financePartnerASpendingStyle = it },
                                    financePartnerBSpendingStyle = financePartnerBSpendingStyle,
                                    onFinancePartnerBSpendingStyleChange = { financePartnerBSpendingStyle = it },
                                    financeMoneyTalkFrequency = financeMoneyTalkFrequency,
                                    onFinanceMoneyTalkFrequencyChange = { financeMoneyTalkFrequency = it },
                                    financePrimaryGoal = financePrimaryGoal,
                                    onFinancePrimaryGoalChange = { financePrimaryGoal = it },
                                    financeIrregularExpensePlan = financeIrregularExpensePlan,
                                    onFinanceIrregularExpensePlanChange = { financeIrregularExpensePlan = it },
                                    financeBillSplitStrategy = financeBillSplitStrategy,
                                    onFinanceBillSplitStrategyChange = { financeBillSplitStrategy = it },
                                    financeSettleWorkflow = financeSettleWorkflow,
                                    onFinanceSettleWorkflowChange = { financeSettleWorkflow = it },
                                    financeRecurringBills = financeRecurringBills,
                                    onFinanceRecurringBillsChange = { financeRecurringBills = it },
                                    financeBillPainPoint = financeBillPainPoint,
                                    onFinanceBillPainPointChange = { financeBillPainPoint = it },
                                    financePartnerAIncome = financePartnerAIncome,
                                    onFinancePartnerAIncomeChange = { financePartnerAIncome = it },
                                    financePartnerBIncome = financePartnerBIncome,
                                    onFinancePartnerBIncomeChange = { financePartnerBIncome = it },
                                    financeCustomSplitPercentages = financeCustomSplitPercentages,
                                    onFinanceCustomSplitPercentagesChange = { financeCustomSplitPercentages = it },
                                    financeMonthlyHousingSpend = financeMonthlyHousingSpend,
                                    onFinanceMonthlyHousingSpendChange = { financeMonthlyHousingSpend = it },
                                    financeMonthlyUtilitiesSpend = financeMonthlyUtilitiesSpend,
                                    onFinanceMonthlyUtilitiesSpendChange = { financeMonthlyUtilitiesSpend = it },
                                    financeMonthlyConnectivitySpend = financeMonthlyConnectivitySpend,
                                    onFinanceMonthlyConnectivitySpendChange = { financeMonthlyConnectivitySpend = it },
                                    financeMonthlySubscriptionsSpend = financeMonthlySubscriptionsSpend,
                                    onFinanceMonthlySubscriptionsSpendChange = { financeMonthlySubscriptionsSpend = it },
                                    financeMonthlyInsuranceSpend = financeMonthlyInsuranceSpend,
                                    onFinanceMonthlyInsuranceSpendChange = { financeMonthlyInsuranceSpend = it },
                                    financeMonthlyKidsPetsSpend = financeMonthlyKidsPetsSpend,
                                    onFinanceMonthlyKidsPetsSpendChange = { financeMonthlyKidsPetsSpend = it },
                                    financeMonthlyOtherSpend = financeMonthlyOtherSpend,
                                    onFinanceMonthlyOtherSpendChange = { financeMonthlyOtherSpend = it },
                                    onGenerateFinancialBlueprint = {
                                        screenModel.generateFinancialBlueprint(
                                            FinanceProfile(
                                                financeSplit = financeSplit,
                                                billManager = financeBillManager,
                                                dailyAnnoyance = financeDailyAnnoyance,
                                                partnerASpendingStyle = financePartnerASpendingStyle,
                                                partnerBSpendingStyle = financePartnerBSpendingStyle,
                                                moneyTalkFrequency = financeMoneyTalkFrequency,
                                                primaryGoal = financePrimaryGoal,
                                                irregularExpensePlan = financeIrregularExpensePlan,
                                                billSplitStrategy = financeBillSplitStrategy,
                                                settleWorkflow = financeSettleWorkflow,
                                                recurringBills = financeRecurringBills,
                                                billPainPoint = financeBillPainPoint,
                                                partnerAIncome = financePartnerAIncome,
                                                partnerBIncome = financePartnerBIncome,
                                                customSplitPercentages = financeCustomSplitPercentages,
                                                monthlyHousingSpend = financeMonthlyHousingSpend,
                                                monthlyUtilitiesSpend = financeMonthlyUtilitiesSpend,
                                                monthlyConnectivitySpend = financeMonthlyConnectivitySpend,
                                                monthlySubscriptionsSpend = financeMonthlySubscriptionsSpend,
                                                monthlyInsuranceSpend = financeMonthlyInsuranceSpend,
                                                monthlyKidsPetsSpend = financeMonthlyKidsPetsSpend,
                                                monthlyOtherSpend = financeMonthlyOtherSpend
                                            )
                                        )
                                    },
                                    healthConflictTopic = healthConflictTopic,
                                    onHealthConflictTopicChange = { healthConflictTopic = it },
                                    healthConflictDraft = healthConflictDraft,
                                    onHealthConflictDraftChange = { healthConflictDraft = it },
                                    healthPartnerLoveLanguage = healthPartnerLoveLanguage,
                                    onHealthPartnerLoveLanguageChange = { healthPartnerLoveLanguage = it },
                                    healthAvailableTime = healthAvailableTime,
                                    onHealthAvailableTimeChange = { healthAvailableTime = it },
                                    healthBudget = healthBudget,
                                    onHealthBudgetChange = { healthBudget = it },
                                    healthEnergyLevel = healthEnergyLevel,
                                    onHealthEnergyLevelChange = { healthEnergyLevel = it },
                                    healthStressLevel = healthStressLevel,
                                    onHealthStressLevelChange = { healthStressLevel = it },
                                    healthConnectionLevel = healthConnectionLevel,
                                    onHealthConnectionLevelChange = { healthConnectionLevel = it },
                                    healthWeeklyDrain = healthWeeklyDrain,
                                    onHealthWeeklyDrainChange = { healthWeeklyDrain = it },
                                    healthDateNightDuration = healthDateNightDuration,
                                    onHealthDateNightDurationChange = { healthDateNightDuration = it },
                                    healthDateNightLikes = healthDateNightLikes,
                                    onHealthDateNightLikesChange = { healthDateNightLikes = it },
                                    healthDateNightAvoids = healthDateNightAvoids,
                                    onHealthDateNightAvoidsChange = { healthDateNightAvoids = it },
                                    onGenerateCouplesWellnessPlan = {
                                        screenModel.generateCouplesWellnessPlan(
                                            HealthWellnessProfile(
                                                conflictTopic = healthConflictTopic,
                                                conflictDraft = healthConflictDraft,
                                                partnerLoveLanguage = healthPartnerLoveLanguage,
                                                availableTime = healthAvailableTime,
                                                budget = healthBudget,
                                                energyLevel = healthEnergyLevel,
                                                stressLevel = healthStressLevel,
                                                connectionLevel = healthConnectionLevel,
                                                weeklyDrain = healthWeeklyDrain,
                                                dateNightDuration = healthDateNightDuration,
                                                dateNightLikes = healthDateNightLikes,
                                                dateNightAvoids = healthDateNightAvoids
                                            )
                                        )
                                    },
                                    longTermMilestoneType = longTermMilestoneType,
                                    onLongTermMilestoneTypeChange = { longTermMilestoneType = it },
                                    longTermRoadblock = longTermRoadblock,
                                    onLongTermRoadblockChange = { longTermRoadblock = it },
                                    longTermTimeline = longTermTimeline,
                                    onLongTermTimelineChange = { longTermTimeline = it },
                                    longTermBudgetStyle = longTermBudgetStyle,
                                    onLongTermBudgetStyleChange = { longTermBudgetStyle = it },
                                    longTermSuccessDefinition = longTermSuccessDefinition,
                                    onLongTermSuccessDefinitionChange = { longTermSuccessDefinition = it },
                                    onGenerateLongTermProjectBlueprint = {
                                        screenModel.generateLongTermProjectBlueprint(
                                            LongTermProjectProfile(
                                                milestoneType = longTermMilestoneType,
                                                roadblock = longTermRoadblock,
                                                timeline = longTermTimeline,
                                                budgetStyle = longTermBudgetStyle,
                                                successDefinition = longTermSuccessDefinition
                                            )
                                        )
                                    },
                                    specific = specific, onSpecificChange = { specific = it },
                                    measurable = measurable, onMeasurableChange = { measurable = it },
                                    achievable = achievable, onAchievableChange = { achievable = it },
                                    relevant = relevant, onRelevantChange = { relevant = it },
                                    timeBound = timeBound, onTimeBoundChange = { timeBound = it },
                                    planItems = planItems,
                                    showAiReview = hasGeneratedAiPlan,
                                    suggestedTasks = selectedTasks,
                                    onToggleTask = { task ->
                                        selectedTasks = if (selectedTasks.contains(task)) {
                                            selectedTasks - task
                                        } else {
                                            selectedTasks + task
                                        }
                                    },
                                    onRegenerate = {
                                        screenModel.resetArchitect()
                                        hasStartedDreamCard = false
                                    }
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
    private fun DreamCardIntakeSection(
        value: String,
        onValueChange: (String) -> Unit,
        selectedCategory: JourneyCategory,
        onCategorySelected: (JourneyCategory) -> Unit,
        onContinue: () -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val selectedExperience = selectedCategory.experienceResources
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            stringResource(Res.string.dream_card_step_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = SharedJourneyColors.MediterraneanTeal,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        stringResource(Res.string.dream_card_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = SharedJourneyColors.MediterraneanTeal,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(Res.string.dream_card_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SharedJourneyColors.InkMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(stringResource(Res.string.dream_card_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SharedJourneyColors.GlassWhite,
                        unfocusedContainerColor = SharedJourneyColors.GlassWhite,
                        focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            item {
                CategorySelector(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SharedJourneyColors.GlassWhite,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(Res.string.dream_card_category_output_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = SharedJourneyColors.MediterraneanTeal,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            stringResource(selectedExperience.outcome),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SharedJourneyColors.InkDeep,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(selectedExperience.dreamPrompt),
                            style = MaterialTheme.typography.bodySmall,
                            color = SharedJourneyColors.InkMuted,
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onContinue,
                    enabled = value.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)
                ) {
                    Icon(AppIcons.Sparkles, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(Res.string.dream_card_continue), fontWeight = FontWeight.Bold)
                }
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
        financeSplit: String, onFinanceSplitChange: (String) -> Unit,
        financeBillManager: String, onFinanceBillManagerChange: (String) -> Unit,
        financeDailyAnnoyance: String, onFinanceDailyAnnoyanceChange: (String) -> Unit,
        financePartnerASpendingStyle: String, onFinancePartnerASpendingStyleChange: (String) -> Unit,
        financePartnerBSpendingStyle: String, onFinancePartnerBSpendingStyleChange: (String) -> Unit,
        financeMoneyTalkFrequency: String, onFinanceMoneyTalkFrequencyChange: (String) -> Unit,
        financePrimaryGoal: String, onFinancePrimaryGoalChange: (String) -> Unit,
        financeIrregularExpensePlan: String, onFinanceIrregularExpensePlanChange: (String) -> Unit,
        financeBillSplitStrategy: String, onFinanceBillSplitStrategyChange: (String) -> Unit,
        financeSettleWorkflow: String, onFinanceSettleWorkflowChange: (String) -> Unit,
        financeRecurringBills: List<String>, onFinanceRecurringBillsChange: (List<String>) -> Unit,
        financeBillPainPoint: String, onFinanceBillPainPointChange: (String) -> Unit,
        financePartnerAIncome: String, onFinancePartnerAIncomeChange: (String) -> Unit,
        financePartnerBIncome: String, onFinancePartnerBIncomeChange: (String) -> Unit,
        financeCustomSplitPercentages: String, onFinanceCustomSplitPercentagesChange: (String) -> Unit,
        financeMonthlyHousingSpend: String, onFinanceMonthlyHousingSpendChange: (String) -> Unit,
        financeMonthlyUtilitiesSpend: String, onFinanceMonthlyUtilitiesSpendChange: (String) -> Unit,
        financeMonthlyConnectivitySpend: String, onFinanceMonthlyConnectivitySpendChange: (String) -> Unit,
        financeMonthlySubscriptionsSpend: String, onFinanceMonthlySubscriptionsSpendChange: (String) -> Unit,
        financeMonthlyInsuranceSpend: String, onFinanceMonthlyInsuranceSpendChange: (String) -> Unit,
        financeMonthlyKidsPetsSpend: String, onFinanceMonthlyKidsPetsSpendChange: (String) -> Unit,
        financeMonthlyOtherSpend: String, onFinanceMonthlyOtherSpendChange: (String) -> Unit,
        onGenerateFinancialBlueprint: () -> Unit,
        healthConflictTopic: String, onHealthConflictTopicChange: (String) -> Unit,
        healthConflictDraft: String, onHealthConflictDraftChange: (String) -> Unit,
        healthPartnerLoveLanguage: String, onHealthPartnerLoveLanguageChange: (String) -> Unit,
        healthAvailableTime: String, onHealthAvailableTimeChange: (String) -> Unit,
        healthBudget: String, onHealthBudgetChange: (String) -> Unit,
        healthEnergyLevel: String, onHealthEnergyLevelChange: (String) -> Unit,
        healthStressLevel: String, onHealthStressLevelChange: (String) -> Unit,
        healthConnectionLevel: String, onHealthConnectionLevelChange: (String) -> Unit,
        healthWeeklyDrain: String, onHealthWeeklyDrainChange: (String) -> Unit,
        healthDateNightDuration: String, onHealthDateNightDurationChange: (String) -> Unit,
        healthDateNightLikes: String, onHealthDateNightLikesChange: (String) -> Unit,
        healthDateNightAvoids: String, onHealthDateNightAvoidsChange: (String) -> Unit,
        onGenerateCouplesWellnessPlan: () -> Unit,
        longTermMilestoneType: String, onLongTermMilestoneTypeChange: (String) -> Unit,
        longTermRoadblock: String, onLongTermRoadblockChange: (String) -> Unit,
        longTermTimeline: String, onLongTermTimelineChange: (String) -> Unit,
        longTermBudgetStyle: String, onLongTermBudgetStyleChange: (String) -> Unit,
        longTermSuccessDefinition: String, onLongTermSuccessDefinitionChange: (String) -> Unit,
        onGenerateLongTermProjectBlueprint: () -> Unit,
        specific: String, onSpecificChange: (String) -> Unit,
        measurable: String, onMeasurableChange: (String) -> Unit,
        achievable: String, onAchievableChange: (String) -> Unit,
        relevant: String, onRelevantChange: (String) -> Unit,
        timeBound: String, onTimeBoundChange: (String) -> Unit,
        planItems: List<JourneyPlanItem>,
        showAiReview: Boolean,
        suggestedTasks: List<String>,
        onToggleTask: (String) -> Unit,
        onRegenerate: () -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val selectedExperience = selectedCategory.experienceResources
            item {
                Surface(
                    color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(AppIcons.Sparkles, contentDescription = null, tint = SharedJourneyColors.MediterraneanTeal, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (showAiReview) {
                                stringResource(
                                    Res.string.dream_card_review_step,
                                    stringResource(selectedExperience.planReviewTitle),
                                )
                            } else {
                                stringResource(
                                    Res.string.dream_card_questions_step,
                                    stringResource(selectedExperience.displayName),
                                )
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = SharedJourneyColors.MediterraneanTeal,
                            fontWeight = FontWeight.Bold
                        )
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
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SharedJourneyColors.GlassWhite,
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(selectedExperience.planReviewTitle),
                            style = MaterialTheme.typography.titleSmall,
                            color = SharedJourneyColors.InkDeep,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            if (showAiReview) {
                                stringResource(Res.string.dream_card_review_guidance)
                            } else {
                                stringResource(selectedExperience.questionsIntro)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SharedJourneyColors.InkMuted,
                        )
                    }
                }
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
            if (selectedCategory == JourneyCategory.HouseholdFinance) {
                item {
                    FinanceQuestionnaire(
                        financeSplit = financeSplit,
                        onFinanceSplitChange = onFinanceSplitChange,
                        billManager = financeBillManager,
                        onBillManagerChange = onFinanceBillManagerChange,
                        dailyAnnoyance = financeDailyAnnoyance,
                        onDailyAnnoyanceChange = onFinanceDailyAnnoyanceChange,
                        partnerASpendingStyle = financePartnerASpendingStyle,
                        onPartnerASpendingStyleChange = onFinancePartnerASpendingStyleChange,
                        partnerBSpendingStyle = financePartnerBSpendingStyle,
                        onPartnerBSpendingStyleChange = onFinancePartnerBSpendingStyleChange,
                        moneyTalkFrequency = financeMoneyTalkFrequency,
                        onMoneyTalkFrequencyChange = onFinanceMoneyTalkFrequencyChange,
                        primaryGoal = financePrimaryGoal,
                        onPrimaryGoalChange = onFinancePrimaryGoalChange,
                        irregularExpensePlan = financeIrregularExpensePlan,
                        onIrregularExpensePlanChange = onFinanceIrregularExpensePlanChange,
                        billSplitStrategy = financeBillSplitStrategy,
                        onBillSplitStrategyChange = onFinanceBillSplitStrategyChange,
                        settleWorkflow = financeSettleWorkflow,
                        onSettleWorkflowChange = onFinanceSettleWorkflowChange,
                        recurringBills = financeRecurringBills,
                        onRecurringBillsChange = onFinanceRecurringBillsChange,
                        billPainPoint = financeBillPainPoint,
                        onBillPainPointChange = onFinanceBillPainPointChange,
                        partnerAIncome = financePartnerAIncome,
                        onPartnerAIncomeChange = onFinancePartnerAIncomeChange,
                        partnerBIncome = financePartnerBIncome,
                        onPartnerBIncomeChange = onFinancePartnerBIncomeChange,
                        customSplitPercentages = financeCustomSplitPercentages,
                        onCustomSplitPercentagesChange = onFinanceCustomSplitPercentagesChange,
                        monthlyHousingSpend = financeMonthlyHousingSpend,
                        onMonthlyHousingSpendChange = onFinanceMonthlyHousingSpendChange,
                        monthlyUtilitiesSpend = financeMonthlyUtilitiesSpend,
                        onMonthlyUtilitiesSpendChange = onFinanceMonthlyUtilitiesSpendChange,
                        monthlyConnectivitySpend = financeMonthlyConnectivitySpend,
                        onMonthlyConnectivitySpendChange = onFinanceMonthlyConnectivitySpendChange,
                        monthlySubscriptionsSpend = financeMonthlySubscriptionsSpend,
                        onMonthlySubscriptionsSpendChange = onFinanceMonthlySubscriptionsSpendChange,
                        monthlyInsuranceSpend = financeMonthlyInsuranceSpend,
                        onMonthlyInsuranceSpendChange = onFinanceMonthlyInsuranceSpendChange,
                        monthlyKidsPetsSpend = financeMonthlyKidsPetsSpend,
                        onMonthlyKidsPetsSpendChange = onFinanceMonthlyKidsPetsSpendChange,
                        monthlyOtherSpend = financeMonthlyOtherSpend,
                        onMonthlyOtherSpendChange = onFinanceMonthlyOtherSpendChange,
                        onGenerateFinancialBlueprint = onGenerateFinancialBlueprint
                    )
                }
            }
            if (selectedCategory == JourneyCategory.HealthWellness) {
                item {
                    HealthWellnessQuestionnaire(
                        conflictTopic = healthConflictTopic,
                        onConflictTopicChange = onHealthConflictTopicChange,
                        conflictDraft = healthConflictDraft,
                        onConflictDraftChange = onHealthConflictDraftChange,
                        partnerLoveLanguage = healthPartnerLoveLanguage,
                        onPartnerLoveLanguageChange = onHealthPartnerLoveLanguageChange,
                        availableTime = healthAvailableTime,
                        onAvailableTimeChange = onHealthAvailableTimeChange,
                        budget = healthBudget,
                        onBudgetChange = onHealthBudgetChange,
                        energyLevel = healthEnergyLevel,
                        onEnergyLevelChange = onHealthEnergyLevelChange,
                        stressLevel = healthStressLevel,
                        onStressLevelChange = onHealthStressLevelChange,
                        connectionLevel = healthConnectionLevel,
                        onConnectionLevelChange = onHealthConnectionLevelChange,
                        weeklyDrain = healthWeeklyDrain,
                        onWeeklyDrainChange = onHealthWeeklyDrainChange,
                        dateNightDuration = healthDateNightDuration,
                        onDateNightDurationChange = onHealthDateNightDurationChange,
                        dateNightLikes = healthDateNightLikes,
                        onDateNightLikesChange = onHealthDateNightLikesChange,
                        dateNightAvoids = healthDateNightAvoids,
                        onDateNightAvoidsChange = onHealthDateNightAvoidsChange,
                        onGenerateCouplesWellnessPlan = onGenerateCouplesWellnessPlan
                    )
                }
            }
            if (selectedCategory == JourneyCategory.LongTermProjects) {
                item {
                    LongTermProjectQuestionnaire(
                        milestoneType = longTermMilestoneType,
                        onMilestoneTypeChange = onLongTermMilestoneTypeChange,
                        roadblock = longTermRoadblock,
                        onRoadblockChange = onLongTermRoadblockChange,
                        timeline = longTermTimeline,
                        onTimelineChange = onLongTermTimelineChange,
                        budgetStyle = longTermBudgetStyle,
                        onBudgetStyleChange = onLongTermBudgetStyleChange,
                        successDefinition = longTermSuccessDefinition,
                        onSuccessDefinitionChange = onLongTermSuccessDefinitionChange,
                        onGenerateBlueprint = onGenerateLongTermProjectBlueprint
                    )
                }
            }

            if (showAiReview) {
                item {
                    Text(
                        stringResource(Res.string.dream_card_ai_understanding),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SharedJourneyColors.MediterraneanTeal,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                item { EditField(stringResource(Res.string.dream_card_ai_understood_field), specific, onSpecificChange) }
                item { EditField(stringResource(Res.string.dream_card_success_field), measurable, onMeasurableChange) }
                item { EditField(stringResource(Res.string.dream_card_realistic_field), achievable, onAchievableChange) }
                item { EditField(stringResource(Res.string.dream_card_household_relevance_field), relevant, onRelevantChange) }
                item { EditField(stringResource(Res.string.dream_card_time_horizon_field), timeBound, onTimeBoundChange) }
            }

            if (planItems.isNotEmpty()) {
                item {
                    Text(
                        stringResource(selectedExperience.planReviewTitle),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SharedJourneyColors.MediterraneanTeal,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                items(planItems) { item ->
                    PlanItemPreview(item)
                }
            }

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
    private fun PlanItemPreview(item: JourneyPlanItem) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SharedJourneyColors.GlassWhite,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    item.type.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SharedJourneyColors.MediterraneanTeal,
                    fontWeight = FontWeight.Black
                )
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SharedJourneyColors.InkDeep,
                    fontWeight = FontWeight.Black
                )
                Text(
                    item.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = SharedJourneyColors.InkMuted
                )
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
        val selectedExperience = selectedCategory.experienceResources

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(Res.string.journey_category_label),
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
                        Text(stringResource(selectedExperience.displayName), fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(selectedExperience.description),
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
                    JourneyCategory.entries.filter { it.isCreationReady }.forEach { category ->
                        val experience = category.experienceResources
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(stringResource(experience.displayName), fontWeight = FontWeight.Bold)
                                    Text(
                                        stringResource(experience.description),
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
    private fun HealthWellnessQuestionnaire(
        conflictTopic: String,
        onConflictTopicChange: (String) -> Unit,
        conflictDraft: String,
        onConflictDraftChange: (String) -> Unit,
        partnerLoveLanguage: String,
        onPartnerLoveLanguageChange: (String) -> Unit,
        availableTime: String,
        onAvailableTimeChange: (String) -> Unit,
        budget: String,
        onBudgetChange: (String) -> Unit,
        energyLevel: String,
        onEnergyLevelChange: (String) -> Unit,
        stressLevel: String,
        onStressLevelChange: (String) -> Unit,
        connectionLevel: String,
        onConnectionLevelChange: (String) -> Unit,
        weeklyDrain: String,
        onWeeklyDrainChange: (String) -> Unit,
        dateNightDuration: String,
        onDateNightDurationChange: (String) -> Unit,
        dateNightLikes: String,
        onDateNightLikesChange: (String) -> Unit,
        dateNightAvoids: String,
        onDateNightAvoidsChange: (String) -> Unit,
        onGenerateCouplesWellnessPlan: () -> Unit,
    ) {
        val loveLanguageOptions = listOf(
            "Acts of Service",
            "Words of Affirmation",
            "Quality Time",
            "Physical Touch",
            "Receiving Gifts"
        )
        val timeOptions = listOf("5 minutes", "15 minutes", "30 minutes", "1 hour")
        val budgetOptions = listOf("\$0", "\$10", "\$25", "\$50")
        val drainOptions = listOf("Work", "Kids", "Chores", "Health", "Money", "Family")
        val durationOptions = listOf("30 minutes", "1 hour", "2 hours", "All evening")
        val canGeneratePlan = listOf(
            conflictTopic,
            conflictDraft,
            partnerLoveLanguage,
            availableTime,
            budget,
            energyLevel,
            stressLevel,
            connectionLevel,
            weeklyDrain,
            dateNightDuration,
            dateNightLikes,
            dateNightAvoids
        ).any { it.isNotBlank() }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Couples Wellness Check-In",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                "Capture tiny answers that help the AI mediate, translate and plan for the team without taking sides.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            QuestionBlock(
                title = "De-escalator: what are you fighting about?",
                description = "The AI rewrites blame into a calmer 'I feel / I need / can we' message."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = conflictTopic,
                        onValueChange = onConflictTopicChange,
                        placeholder = { Text("Example: dishes, money, bedtime, chores") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                            unfocusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                            focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    TextField(
                        value = conflictDraft,
                        onValueChange = onConflictDraftChange,
                        placeholder = { Text("Paste what you want to say before sending it") },
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
                }
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            QuestionBlock(
                title = "Love language translator",
                description = "Pick quick constraints so the AI can suggest a tiny appreciation action that actually lands."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OptionChips(loveLanguageOptions, partnerLoveLanguage, onPartnerLoveLanguageChange)
                    OptionChips(timeOptions, availableTime, onAvailableTimeChange)
                    OptionChips(budgetOptions, budget, onBudgetChange)
                }
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                "Weekly Temperature Check",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            WellnessSlider("Energy Level", energyLevel, onEnergyLevelChange)
            WellnessSlider("Stress Level", stressLevel, onStressLevelChange)
            WellnessSlider("Connection Level", connectionLevel, onConnectionLevelChange)
            QuestionBlock(
                title = "What is draining you the most this week?",
                description = "This gives the AI a prevention signal before the couple runs out of fuel."
            ) {
                OptionChips(drainOptions, weeklyDrain, onWeeklyDrainChange)
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            QuestionBlock(
                title = "Date night decision maker",
                description = "Capture constraints so the AI can remove the 'I don't know, what do you want to do?' loop."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionChips(durationOptions, dateNightDuration, onDateNightDurationChange)
                    TextField(
                        value = dateNightLikes,
                        onValueChange = onDateNightLikesChange,
                        placeholder = { Text("We like sushi, cards, cooking, music...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                            unfocusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                            focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    TextField(
                        value = dateNightAvoids,
                        onValueChange = onDateNightAvoidsChange,
                        placeholder = { Text("We do not want to watch a movie") },
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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Golden rule: You + Them vs. The Problem. The AI should never judge who is right.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SharedJourneyColors.MediterraneanTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onGenerateCouplesWellnessPlan,
                enabled = canGeneratePlan,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)
            ) {
                Icon(AppIcons.Sparkles, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Generate couples wellness plan", fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun WellnessSlider(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
    ) {
        val sliderValue = value.toFloatOrNull()?.coerceIn(1f, 10f) ?: 5f
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "$label: ${sliderValue.toInt()}/10",
                style = MaterialTheme.typography.bodyMedium,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = sliderValue,
                onValueChange = { onValueChange(it.toInt().toString()) },
                valueRange = 1f..10f,
                steps = 8
            )
        }
    }

    @Composable
    private fun LongTermProjectQuestionnaire(
        milestoneType: String,
        onMilestoneTypeChange: (String) -> Unit,
        roadblock: String,
        onRoadblockChange: (String) -> Unit,
        timeline: String,
        onTimelineChange: (String) -> Unit,
        budgetStyle: String,
        onBudgetStyleChange: (String) -> Unit,
        successDefinition: String,
        onSuccessDefinitionChange: (String) -> Unit,
        onGenerateBlueprint: () -> Unit,
    ) {
        val milestoneOptions = listOf(
            "Moving to a new home",
            "Renovation / Home Improvement",
            "Big Event (Wedding, milestone party, baby shower)",
            "Major life transition (New baby, career change, kids starting school)",
            "Decluttering / Deep Organization (Garage, attic, etc.)"
        )
        val roadblockOptions = listOf(
            "Time: We are too busy with daily life to focus on it.",
            "Alignment: We aren't on the same page about the budget, style, or plan.",
            "Overwhelmed: We don't know the correct order of steps to take.",
            "Accountability: We start, but things keep getting pushed to the back burner."
        )
        val timelineOptions = listOf(
            "ASAP (Less than a month)",
            "Medium-term (1-6 months)",
            "Long-term (6+ months / No rush)"
        )
        val budgetOptions = listOf(
            "Tight / Bootstrapping (DIY all the way)",
            "Moderate (Some DIY, hiring pros for the big stuff)",
            "Full Service (We want to hire vendors/professionals)"
        )
        val canGenerateBlueprint = milestoneType.isNotBlank() &&
            roadblock.isNotBlank() &&
            timeline.isNotBlank() &&
            budgetStyle.isNotBlank() &&
            successDefinition.isNotBlank()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Long-Term Project Blueprint",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                "Answer four quick prompts and one sentence so the AI can build the project plan for you.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            QuestionBlock(
                title = "What big milestone are you tackling next?",
                description = "This tells the AI what kind of project mountain it is planning around."
            ) {
                OptionChips(milestoneOptions, milestoneType, onMilestoneTypeChange)
            }

            QuestionBlock(
                title = "What's the biggest roadblock keeping you from finishing or starting this?",
                description = "This changes whether the AI emphasizes steps, alignment, ownership or momentum."
            ) {
                OptionChips(roadblockOptions, roadblock, onRoadblockChange)
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                "Boundaries",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            QuestionBlock(
                title = "What is your realistic timeline for this?",
                description = "Sets the size and cadence of the micro-step checklist."
            ) {
                OptionChips(timelineOptions, timeline, onTimelineChange)
            }
            QuestionBlock(
                title = "How would you describe your budget for this?",
                description = "Helps the AI choose between DIY steps, vendor work and quote tracking."
            ) {
                OptionChips(budgetOptions, budgetStyle, onBudgetStyleChange)
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            QuestionBlock(
                title = "In one sentence, what does a successful finish look like to you?",
                description = "Example: A completely organized garage where we can actually park both cars by summer."
            ) {
                TextField(
                    value = successDefinition,
                    onValueChange = onSuccessDefinitionChange,
                    placeholder = { Text("Example: Moving into our new house without losing our minds or overspending on movers.") },
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

            Button(
                onClick = onGenerateBlueprint,
                enabled = canGenerateBlueprint,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)
            ) {
                Icon(AppIcons.Sparkles, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Generate household action blueprint", fontWeight = FontWeight.Bold)
            }

            if (!canGenerateBlueprint) {
                Text(
                    "Complete the milestone, roadblock, timeline, budget and success sentence so the AI can build the roadmap.",
                    style = MaterialTheme.typography.labelSmall,
                    color = SharedJourneyColors.InkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    @Composable
    private fun FinanceQuestionnaire(
        financeSplit: String,
        onFinanceSplitChange: (String) -> Unit,
        billManager: String,
        onBillManagerChange: (String) -> Unit,
        dailyAnnoyance: String,
        onDailyAnnoyanceChange: (String) -> Unit,
        partnerASpendingStyle: String,
        onPartnerASpendingStyleChange: (String) -> Unit,
        partnerBSpendingStyle: String,
        onPartnerBSpendingStyleChange: (String) -> Unit,
        moneyTalkFrequency: String,
        onMoneyTalkFrequencyChange: (String) -> Unit,
        primaryGoal: String,
        onPrimaryGoalChange: (String) -> Unit,
        irregularExpensePlan: String,
        onIrregularExpensePlanChange: (String) -> Unit,
        billSplitStrategy: String,
        onBillSplitStrategyChange: (String) -> Unit,
        settleWorkflow: String,
        onSettleWorkflowChange: (String) -> Unit,
        recurringBills: List<String>,
        onRecurringBillsChange: (List<String>) -> Unit,
        billPainPoint: String,
        onBillPainPointChange: (String) -> Unit,
        partnerAIncome: String,
        onPartnerAIncomeChange: (String) -> Unit,
        partnerBIncome: String,
        onPartnerBIncomeChange: (String) -> Unit,
        customSplitPercentages: String,
        onCustomSplitPercentagesChange: (String) -> Unit,
        monthlyHousingSpend: String,
        onMonthlyHousingSpendChange: (String) -> Unit,
        monthlyUtilitiesSpend: String,
        onMonthlyUtilitiesSpendChange: (String) -> Unit,
        monthlyConnectivitySpend: String,
        onMonthlyConnectivitySpendChange: (String) -> Unit,
        monthlySubscriptionsSpend: String,
        onMonthlySubscriptionsSpendChange: (String) -> Unit,
        monthlyInsuranceSpend: String,
        onMonthlyInsuranceSpendChange: (String) -> Unit,
        monthlyKidsPetsSpend: String,
        onMonthlyKidsPetsSpendChange: (String) -> Unit,
        monthlyOtherSpend: String,
        onMonthlyOtherSpendChange: (String) -> Unit,
        onGenerateFinancialBlueprint: () -> Unit,
    ) {
        val billSplitStrategyOptions = listOf(
            "50/50 (Right down the middle)",
            "Proportional to income",
            "Custom fixed percentages",
            "Assign specific bills to specific people"
        )
        val settleWorkflowOptions = listOf(
            "We send Venmos/Zelles back and forth constantly",
            "One person pays everything, and we calculate a lump sum at the end of the month",
            "We have a shared joint account that we both fund, and bills auto-pay from there"
        )
        val recurringBillOptions = listOf(
            "Housing (Rent / Mortgage)",
            "Utilities (Electric, Water, Gas, Trash)",
            "Connectivity (Internet, Mobile/Cell plans)",
            "Subscriptions (Netflix, Spotify, Gym, Costco, etc.)",
            "Insurance (Car, Health, Home/Renter's)",
            "Kids & Pets (Daycare, Tuition, Pet Insurance)"
        )
        val billPainPointOptions = listOf(
            "Forgetting when a bill is due (Late fees)",
            "The awkwardness of nagging my partner to send their half",
            "Calculating who owes what when bills fluctuate (like electric/gas)",
            "Feeling like the Project Manager who has to log into 10 different sites"
        )
        val financeSplitOptions = listOf(
            "Completely combined",
            "Completely separate",
            "A mix (joint account for bills, separate for personal)"
        )
        val billManagerOptions = listOf(
            "Partner A does it all",
            "Partner B does it all",
            "We try to do it together",
            "Honestly, it's a bit chaotic"
        )
        val annoyanceOptions = listOf(
            "Forgetting when bills are due",
            "Tracking who owes what",
            "Overspending on groceries/dining out",
            "Arguments over personal spending"
        )
        val spendingScaleOptions = listOf("1", "2", "3", "4", "5")
        val moneyTalkOptions = listOf(
            "Only when something goes wrong",
            "Regularly (monthly/weekly check-ins)",
            "Rarely/We avoid it"
        )
        val goalOptions = listOf(
            "Building an emergency fund",
            "Paying off high-interest debt",
            "Saving for a big milestone (wedding, baby, house, vacation)",
            "Just gaining peace of mind"
        )
        val irregularExpenseOptions = listOf(
            "Yes, we save in advance",
            "No, we just handle them as they hit our credit cards"
        )
        val monthlySpendTotal = listOf(
            monthlyHousingSpend,
            monthlyUtilitiesSpend,
            monthlyConnectivitySpend,
            monthlySubscriptionsSpend,
            monthlyInsuranceSpend,
            monthlyKidsPetsSpend,
            monthlyOtherSpend
        ).sumOf { it.toAmountValue() }
        val canGenerateBlueprint = billSplitStrategy.isNotBlank() &&
            settleWorkflow.isNotBlank() &&
            recurringBills.isNotEmpty() &&
            billPainPoint.isNotBlank() &&
            (!billSplitStrategy.contains("Proportional", ignoreCase = true) || (partnerAIncome.isNotBlank() && partnerBIncome.isNotBlank())) &&
            (!billSplitStrategy.contains("Custom", ignoreCase = true) || customSplitPercentages.isNotBlank()) &&
            financeSplit.isNotBlank() &&
            billManager.isNotBlank() &&
            dailyAnnoyance.isNotBlank() &&
            partnerASpendingStyle.isNotBlank() &&
            partnerBSpendingStyle.isNotBlank() &&
            moneyTalkFrequency.isNotBlank() &&
            primaryGoal.isNotBlank() &&
            irregularExpensePlan.isNotBlank()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Household Financial Assessment",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                "These answers let the AI create a custom household ledger for bill tracking, smart splitting and one clean settle-up rhythm.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            Text(
                "Bill Tracking & Splitting",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            QuestionBlock(
                title = "How do you prefer to split your shared household expenses?",
                description = "This becomes the default math rule for each bill in the ledger."
            ) {
                OptionChips(billSplitStrategyOptions, billSplitStrategy, onBillSplitStrategyChange)
            }
            if (billSplitStrategy.contains("Proportional", ignoreCase = true)) {
                QuestionBlock(
                    title = "What are the approximate partner incomes?",
                    description = "Optional in a real app, but useful here so the AI can calculate a fair-share split."
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = partnerAIncome,
                            onValueChange = onPartnerAIncomeChange,
                            placeholder = { Text("Partner A income, e.g. 60000") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                                unfocusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                                focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        TextField(
                            value = partnerBIncome,
                            onValueChange = onPartnerBIncomeChange,
                            placeholder = { Text("Partner B income, e.g. 40000") },
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
            }
            if (billSplitStrategy.contains("Custom", ignoreCase = true)) {
                QuestionBlock(
                    title = "What fixed split should we use?",
                    description = "Example: 60/40, 70/30, or Partner A 60% / Partner B 40%."
                ) {
                    TextField(
                        value = customSplitPercentages,
                        onValueChange = onCustomSplitPercentagesChange,
                        placeholder = { Text("Example: 60/40") },
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
            QuestionBlock(
                title = "How do you settle up right now?",
                description = "Lets the AI decide whether to create smart settle, lump-sum previews or joint-account reminders."
            ) {
                OptionChips(settleWorkflowOptions, settleWorkflow, onSettleWorkflowChange)
            }
            QuestionBlock(
                title = "Which recurring shared bills do you manage?",
                description = "Select all that apply. These become starter ledger categories and calendar placeholders."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    recurringBillOptions.forEach { option ->
                        val isSelected = recurringBills.contains(option)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    val next = if (checked) {
                                        recurringBills + option
                                    } else {
                                        recurringBills - option
                                    }
                                    onRecurringBillsChange(next)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SharedJourneyColors.MediterraneanTeal)
                            )
                            Text(option, style = MaterialTheme.typography.bodyMedium, color = SharedJourneyColors.InkDeep)
                        }
                    }
                }
            }
            QuestionBlock(
                title = "What is the biggest headache with bills right now?",
                description = "This chooses the first automation the AI should emphasize."
            ) {
                OptionChips(billPainPointOptions, billPainPoint, onBillPainPointChange)
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                "Monthly Spending Snapshot",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                "Add the monthly amounts you already know. The ledger will use this to show a clear per-month spending breakdown while receipts and variable bills improve it over time.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )
            MonthlySpendField("Housing", monthlyHousingSpend, onMonthlyHousingSpendChange)
            MonthlySpendField("Utilities", monthlyUtilitiesSpend, onMonthlyUtilitiesSpendChange)
            MonthlySpendField("Connectivity", monthlyConnectivitySpend, onMonthlyConnectivitySpendChange)
            MonthlySpendField("Subscriptions", monthlySubscriptionsSpend, onMonthlySubscriptionsSpendChange)
            MonthlySpendField("Insurance", monthlyInsuranceSpend, onMonthlyInsuranceSpendChange)
            MonthlySpendField("Kids & Pets", monthlyKidsPetsSpend, onMonthlyKidsPetsSpendChange)
            MonthlySpendField("Other shared spending", monthlyOtherSpend, onMonthlyOtherSpendChange)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Reported monthly spend: ${monthlySpendTotal.toCurrencyText()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SharedJourneyColors.MediterraneanTeal,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center
                )
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                "Relationship Context",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            QuestionBlock(
                title = "How do you currently split your finances?",
                description = "Determines whether the app should emphasize joint pools, separate tracking, or expense splitting."
            ) {
                OptionChips(financeSplitOptions, financeSplit, onFinanceSplitChange)
            }
            QuestionBlock(
                title = "Who currently manages the day-to-day household bills?",
                description = "Identifies mental load and bill ownership."
            ) {
                OptionChips(billManagerOptions, billManager, onBillManagerChange)
            }
            QuestionBlock(
                title = "What is your biggest daily financial annoyance right now?",
                description = "Sets the first feature focus for the AI."
            ) {
                OptionChips(annoyanceOptions, dailyAnnoyance, onDailyAnnoyanceChange)
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                "Phase 2: Money Personalities",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                "Scale: 1 = Strict Saver, 5 = Spontaneous Spender. Each partner should answer individually.",
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )
            QuestionBlock(
                title = "Partner A: relationship with spending",
                description = "Helps detect saver-spender dynamics without blaming either partner."
            ) {
                OptionChips(spendingScaleOptions, partnerASpendingStyle, onPartnerASpendingStyleChange)
            }
            QuestionBlock(
                title = "Partner B: relationship with spending",
                description = "Pairs with Partner A's answer so the AI can act as a neutral mediator."
            ) {
                OptionChips(spendingScaleOptions, partnerBSpendingStyle, onPartnerBSpendingStyleChange)
            }
            QuestionBlock(
                title = "How often do you talk about money together?",
                description = "Shapes the tone and cadence of AI insights."
            ) {
                OptionChips(moneyTalkOptions, moneyTalkFrequency, onMoneyTalkFrequencyChange)
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                "Phase 3: Future Goals",
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            QuestionBlock(
                title = "What is your primary financial goal for the next 12 months?",
                description = "Gives the AI a North Star for budget and automation suggestions."
            ) {
                OptionChips(goalOptions, primaryGoal, onPrimaryGoalChange)
            }
            QuestionBlock(
                title = "Do you have a plan for irregular expenses?",
                description = "Shows whether the AI should recommend a sinking fund."
            ) {
                OptionChips(irregularExpenseOptions, irregularExpensePlan, onIrregularExpensePlanChange)
            }

            Button(
                onClick = onGenerateFinancialBlueprint,
                enabled = canGenerateBlueprint,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)
            ) {
                Icon(AppIcons.Sparkles, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Generate household financial blueprint", fontWeight = FontWeight.Bold)
            }

            if (!canGenerateBlueprint) {
                Text(
                    "Complete the bill split, settle-up flow, recurring bill list and finance context first so the AI can build the household ledger.",
                    style = MaterialTheme.typography.labelSmall,
                    color = SharedJourneyColors.InkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    @Composable
    private fun MonthlySpendField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("$label per month") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("\$") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                unfocusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
                focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        )
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

private val JourneyCategory.isCreationReady: Boolean
    get() = when (this) {
        JourneyCategory.MealPlanning,
        JourneyCategory.HouseholdFinance,
        JourneyCategory.HealthWellness,
        JourneyCategory.LongTermProjects -> true
        JourneyCategory.CalendarLogistics,
        JourneyCategory.HouseholdManagement -> false
    }

private fun String.toAmountValue(): Double {
    return filter { it.isDigit() || it == '.' }
        .takeIf { it.isNotBlank() }
        ?.toDoubleOrNull()
        ?: 0.0
}

private fun Double.toCurrencyText(): String {
    return "\$${toInt()}/mo"
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
fun JourneyEditScreenPreview() {
    MaterialTheme {
        JourneyEditScreen().Content()
    }
}
