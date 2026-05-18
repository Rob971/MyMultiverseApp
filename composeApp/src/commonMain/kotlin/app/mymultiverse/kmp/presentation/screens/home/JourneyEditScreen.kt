package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.mymultiverse.kmp.presentation.components.LanguagePicker
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
        var mealBusyWeeknightCookTime by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.busyWeeknightCookTime ?: MealQuestionnaireValues.TIME_15_30) }
        var mealCookingSkillLevel by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.cookingSkillLevel ?: MealQuestionnaireValues.SKILL_AVERAGE) }
        var mealLunchPreference by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.lunchPreference ?: MealQuestionnaireValues.LUNCH_LEFTOVERS) }
        var mealRightNowGoal by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.rightNowGoal ?: MealQuestionnaireValues.GOAL_WEEK) }
        var mealLocationPreference by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.locationPreference ?: MealQuestionnaireValues.LOCATION_GPS) }
        var mealManualLocation by remember { mutableStateOf(existingJourney?.mealPlanningProfile?.manualLocation ?: "") }
        var financeSplit by remember { mutableStateOf(existingJourney?.financeProfile?.financeSplit ?: FinanceQuestionnaireValues.OPT_POOL_MIX) }
        var financeBillManager by remember { mutableStateOf(existingJourney?.financeProfile?.billManager ?: FinanceQuestionnaireValues.OPT_MANAGER_TOGETHER) }
        var financeDailyAnnoyance by remember { mutableStateOf(existingJourney?.financeProfile?.dailyAnnoyance ?: FinanceQuestionnaireValues.OPT_ANNOY_BILLS_DUE) }
        var financePartnerASpendingStyle by remember { mutableStateOf(existingJourney?.financeProfile?.partnerASpendingStyle ?: "3") }
        var financePartnerBSpendingStyle by remember { mutableStateOf(existingJourney?.financeProfile?.partnerBSpendingStyle ?: "3") }
        var financeMoneyTalkFrequency by remember { mutableStateOf(existingJourney?.financeProfile?.moneyTalkFrequency ?: FinanceQuestionnaireValues.OPT_TALK_REGULAR) }
        var financePrimaryGoal by remember { mutableStateOf(existingJourney?.financeProfile?.primaryGoal ?: FinanceQuestionnaireValues.OPT_GOAL_PEACE) }
        var financeIrregularExpensePlan by remember { mutableStateOf(existingJourney?.financeProfile?.irregularExpensePlan ?: FinanceQuestionnaireValues.OPT_IRREGULAR_NO) }
        var financeBillSplitStrategy by remember { mutableStateOf(existingJourney?.financeProfile?.billSplitStrategy ?: FinanceQuestionnaireValues.OPT_SPLIT_50_50) }
        var financeSettleWorkflow by remember { mutableStateOf(existingJourney?.financeProfile?.settleWorkflow ?: FinanceQuestionnaireValues.OPT_SETTLE_VENMO) }
        var financeRecurringBills by remember { mutableStateOf(existingJourney?.financeProfile?.recurringBills ?: emptyList()) }
        var financeBillPainPoint by remember { mutableStateOf(existingJourney?.financeProfile?.billPainPoint ?: FinanceQuestionnaireValues.OPT_PAIN_LATE) }
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
        var healthPartnerLoveLanguage by remember {
            mutableStateOf(
                existingJourney?.healthWellnessProfile?.partnerLoveLanguage
                    ?: WellnessQuestionnaireValues.OPT_LOVE_ACTS,
            )
        }
        var healthAvailableTime by remember {
            mutableStateOf(
                existingJourney?.healthWellnessProfile?.availableTime
                    ?: WellnessQuestionnaireValues.OPT_TIME_15,
            )
        }
        var healthBudget by remember {
            mutableStateOf(
                existingJourney?.healthWellnessProfile?.budget
                    ?: WellnessQuestionnaireValues.OPT_BUDGET_10,
            )
        }
        var healthEnergyLevel by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.energyLevel ?: "5") }
        var healthStressLevel by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.stressLevel ?: "5") }
        var healthConnectionLevel by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.connectionLevel ?: "5") }
        var healthWeeklyDrain by remember {
            mutableStateOf(
                existingJourney?.healthWellnessProfile?.weeklyDrain
                    ?: WellnessQuestionnaireValues.OPT_DRAIN_WORK,
            )
        }
        var healthDateNightDuration by remember {
            mutableStateOf(
                existingJourney?.healthWellnessProfile?.dateNightDuration
                    ?: WellnessQuestionnaireValues.OPT_DURATION_120,
            )
        }
        var healthDateNightLikes by remember { mutableStateOf(existingJourney?.healthWellnessProfile?.dateNightLikes ?: "") }
        var healthDateNightAvoids by remember {
            mutableStateOf(
                existingJourney?.healthWellnessProfile?.dateNightAvoids
                    ?: WellnessQuestionnaireValues.DEFAULT_DATE_AVOID,
            )
        }
        var longTermMilestoneType by remember {
            mutableStateOf(
                existingJourney?.longTermProjectProfile?.milestoneType
                    ?: LongTermQuestionnaireValues.OPT_MILESTONE_MOVE,
            )
        }
        var longTermRoadblock by remember {
            mutableStateOf(
                existingJourney?.longTermProjectProfile?.roadblock
                    ?: LongTermQuestionnaireValues.OPT_ROADBLOCK_OVERWHELMED,
            )
        }
        var longTermTimeline by remember {
            mutableStateOf(
                existingJourney?.longTermProjectProfile?.timeline
                    ?: LongTermQuestionnaireValues.OPT_TIMELINE_MEDIUM,
            )
        }
        var longTermBudgetStyle by remember {
            mutableStateOf(
                existingJourney?.longTermProjectProfile?.budgetStyle
                    ?: LongTermQuestionnaireValues.OPT_BUDGET_MODERATE,
            )
        }
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
                                Icon(AppIcons.ArrowBack, contentDescription = stringResource(Res.string.action_back))
                            }
                        },
                        actions = {
                            LanguagePicker()
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
                            Icon(AppIcons.Check, contentDescription = stringResource(Res.string.action_save))
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
                    FinancePlanningQuestionnaire(
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
                    WellnessPlanningQuestionnaire(
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
                    LongTermPlanningQuestionnaire(
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
                    Icon(AppIcons.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
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
        val partySizeOptions = mealPartySizeOptions()
        val dietaryOptions = mealDietaryOptions()
        val weeknightCookTimeOptions = mealWeeknightTimeOptions()
        val skillLevelOptions = mealSkillOptions()
        val lunchPreferenceOptions = mealLunchOptions()
        val rightNowGoalOptions = mealRightNowGoalOptions()
        val locationOptions = mealLocationOptions()
        val canGenerateWeeklyPlan = cookingFor.isNotBlank() &&
            dietaryRestrictions.isNotEmpty() &&
            dislikedIngredients.isNotBlank() &&
            busyWeeknightCookTime.isNotBlank() &&
            cookingSkillLevel.isNotBlank() &&
            lunchPreference.isNotBlank() &&
            rightNowGoal.isNotBlank() &&
            locationPreference.isNotBlank() &&
            (locationPreference != MealQuestionnaireValues.LOCATION_MANUAL || manualLocation.isNotBlank())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(Res.string.meal_q_essentials_title),
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                stringResource(Res.string.meal_q_essentials_desc),
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            LocalizedQuestionBlock(
                title = Res.string.meal_q_cooking_for_title,
                description = Res.string.meal_q_cooking_for_desc,
            ) {
                LocalizedPartySizeChips(
                    options = partySizeOptions,
                    selectedValue = cookingFor,
                    onSelected = onCookingForChange,
                )
            }

            LocalizedQuestionBlock(
                title = Res.string.meal_q_dietary_title,
                description = Res.string.meal_q_dietary_desc,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    dietaryOptions.forEach { option ->
                        val isSelected = dietaryRestrictions.contains(option.storedValue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    val next = when {
                                        option.storedValue == MealQuestionnaireValues.DIETARY_NONE && checked -> listOf(MealQuestionnaireValues.DIETARY_NONE)
                                        option.storedValue == MealQuestionnaireValues.DIETARY_NONE -> dietaryRestrictions - option.storedValue
                                        checked -> (dietaryRestrictions - MealQuestionnaireValues.DIETARY_NONE) + option.storedValue
                                        else -> dietaryRestrictions - option.storedValue
                                    }
                                    onDietaryRestrictionsChange(next)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SharedJourneyColors.MediterraneanTeal)
                            )
                            Text(
                                stringResource(option.label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SharedJourneyColors.InkDeep,
                            )
                        }
                    }
                }
            }

            LocalizedQuestionBlock(
                title = Res.string.meal_q_disliked_title,
                description = Res.string.meal_q_disliked_desc,
            ) {
                TextField(
                    value = dislikedIngredients,
                    onValueChange = onDislikedIngredientsChange,
                    placeholder = { Text(stringResource(Res.string.meal_q_disliked_placeholder)) },
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
                stringResource(Res.string.meal_q_lifestyle_title),
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                stringResource(Res.string.meal_q_lifestyle_desc),
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            LocalizedQuestionBlock(
                title = Res.string.meal_q_weeknight_time_title,
                description = Res.string.meal_q_weeknight_time_desc,
            ) {
                LocalizedOptionChips(
                    options = weeknightCookTimeOptions,
                    selectedValue = busyWeeknightCookTime,
                    onSelected = onBusyWeeknightCookTimeChange,
                )
            }

            LocalizedQuestionBlock(
                title = Res.string.meal_q_skill_title,
                description = Res.string.meal_q_skill_desc,
            ) {
                LocalizedOptionChips(
                    options = skillLevelOptions,
                    selectedValue = cookingSkillLevel,
                    onSelected = onCookingSkillLevelChange,
                )
            }

            LocalizedQuestionBlock(
                title = Res.string.meal_q_lunch_title,
                description = Res.string.meal_q_lunch_desc,
            ) {
                LocalizedOptionChips(
                    options = lunchPreferenceOptions,
                    selectedValue = lunchPreference,
                    onSelected = onLunchPreferenceChange,
                )
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                stringResource(Res.string.meal_q_friction_title),
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                stringResource(Res.string.meal_q_friction_desc),
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            LocalizedQuestionBlock(
                title = Res.string.meal_q_goal_today_title,
                description = Res.string.meal_q_goal_today_desc,
            ) {
                LocalizedOptionChips(
                    options = rightNowGoalOptions,
                    selectedValue = rightNowGoal,
                    onSelected = onRightNowGoalChange,
                )
            }

            HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

            Text(
                stringResource(Res.string.meal_q_local_context_title),
                style = MaterialTheme.typography.titleMedium,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Black
            )
            Text(
                stringResource(Res.string.meal_q_local_context_desc),
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted
            )

            LocalizedQuestionBlock(
                title = Res.string.meal_q_localize_grocery_title,
                description = Res.string.meal_q_localize_grocery_desc,
            ) {
                LocalizedOptionChips(
                    options = locationOptions,
                    selectedValue = locationPreference,
                    onSelected = onLocationPreferenceChange,
                )
            }

            if (locationPreference == MealQuestionnaireValues.LOCATION_MANUAL) {
                LocalizedQuestionBlock(
                    title = Res.string.meal_q_manual_location_title,
                    description = Res.string.meal_q_manual_location_desc,
                ) {
                    TextField(
                        value = manualLocation,
                        onValueChange = onManualLocationChange,
                        placeholder = { Text(stringResource(Res.string.meal_q_manual_location_placeholder)) },
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
                Text(stringResource(Res.string.meal_q_generate_button), fontWeight = FontWeight.Bold)
            }

            if (!canGenerateWeeklyPlan) {
                Text(
                    stringResource(Res.string.meal_q_complete_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = SharedJourneyColors.InkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
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

private val JourneyCategory.isCreationReady: Boolean
    get() = when (this) {
        JourneyCategory.MealPlanning,
        JourneyCategory.HouseholdFinance,
        JourneyCategory.HealthWellness,
        JourneyCategory.LongTermProjects -> true
        JourneyCategory.CalendarLogistics,
        JourneyCategory.HouseholdManagement -> false
    }

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
fun JourneyEditScreenPreview() {
    MaterialTheme {
        JourneyEditScreen().Content()
    }
}
