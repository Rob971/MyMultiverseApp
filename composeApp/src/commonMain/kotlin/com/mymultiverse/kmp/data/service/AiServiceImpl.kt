package com.mymultiverse.kmp.data.service

import com.mymultiverse.kmp.domain.model.FinanceProfile
import com.mymultiverse.kmp.domain.model.HealthWellnessProfile
import com.mymultiverse.kmp.domain.model.JourneyPlanItem
import com.mymultiverse.kmp.domain.model.LongTermProjectProfile
import com.mymultiverse.kmp.domain.model.MealPlanningProfile
import com.mymultiverse.kmp.domain.model.SmartGoalProposal
import com.mymultiverse.kmp.domain.service.AiService
import kotlinx.coroutines.delay

class AiServiceImpl : AiService {
    override suspend fun refineDream(seed: String): Result<SmartGoalProposal> {
        return try {
            // Simulate AI processing delay
            delay(2000)
            
            // In a real implementation, this would call an LLM API (OpenAI/Gemini/etc.)
            // using Ktor and parse the JSON response.
            
            val proposal = when {
                seed.contains("bike", ignoreCase = true) || seed.contains("moto", ignoreCase = true) -> {
                    SmartGoalProposal(
                        title = "Avventura su Due Ruote",
                        subtitle = "Esplorando Portici e Sagunto con libertà",
                        specific = "Acquistare una MITT 555 TT ADVENTURE per gite in famiglia.",
                        measurable = "Risparmiare €150 al mese per raggiungere il budget.",
                        achievable = "Possibile riducendo le spese non necessarie e usando il bonus mobilità.",
                        relevant = "Migliora la vitalità familiare e le attività all'aperto.",
                        timeBound = "Entro 12 mesi da oggi.",
                        suggestedTasks = listOf(
                            "Ricerca concessionari a Napoli e Valencia",
                            "Aprire un fondo 'Sogno Moto'",
                            "Confrontare preventivi assicurativi",
                            "Pianificare il primo tour costiero"
                        )
                    )
                }
                seed.contains("spagnolo", ignoreCase = true) || seed.contains("spanish", ignoreCase = true) -> {
                    SmartGoalProposal(
                        title = "Espanol en Familia",
                        subtitle = "Connettersi con le nostre radici a Sagunto",
                        specific = "Raggiungere un livello di conversazione base in spagnolo.",
                        measurable = "Completare 50 unità su Duolingo e 10 lezioni con tutor.",
                        achievable = "15 minuti di pratica giornaliera dopo cena.",
                        relevant = "Essenziale per la nostra integrazione e unità familiare in Spagna.",
                        timeBound = "Prima della prossima estate a Sagunto.",
                        suggestedTasks = listOf(
                            "Scaricare Duolingo e creare un gruppo famiglia",
                            "Prenotare un tutor su iTalki",
                            "Guardare un film in spagnolo a settimana",
                            "Cucinare una Paella seguendo una ricetta in lingua"
                        )
                    )
                }
                else -> {
                    SmartGoalProposal(
                        title = "Sogno: $seed",
                        subtitle = "Un nuovo capitolo per la nostra famiglia",
                        specific = "Definire chiaramente l'obiettivo $seed.",
                        measurable = "Identificare 3 indicatori chiave di successo.",
                        achievable = "Suddividere il progetto in piccoli passi sostenibili.",
                        relevant = "Allineare questo sogno con i valori di Vitalità e Unità.",
                        timeBound = "Fissare una scadenza entro i prossimi 6-12 mesi.",
                        suggestedTasks = listOf(
                            "Dettagliare i costi previsti",
                            "Assegnare responsabilità ai membri della famiglia",
                            "Creare una bacheca della visione",
                            "Fissare il primo check-in tra un mese"
                        )
                    )
                }
            }
            Result.success(proposal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateWeeklyMealPlan(profile: MealPlanningProfile): Result<SmartGoalProposal> {
        return try {
            delay(1500)

            val restrictions = profile.dietaryRestrictions
                .filterNot { it == "None" }
                .takeIf { it.isNotEmpty() }
                ?.joinToString(", ")
                ?: "no dietary restrictions"
            val location = when {
                profile.locationPreference == "Provide location manually" && profile.manualLocation.isNotBlank() -> profile.manualLocation
                profile.locationPreference.isNotBlank() -> "your local area"
                else -> "nearby shops"
            }
            val lunchStrategy = if (profile.lunchPreference.contains("leftovers", ignoreCase = true)) {
                "double dinner portions so tomorrow's lunch is covered"
            } else {
                "keep lunches separate and quick to prepare fresh"
            }
            val mode = when {
                profile.rightNowGoal.contains("fridge", ignoreCase = true) -> "Inventory clearance"
                profile.rightNowGoal.contains("grocery", ignoreCase = true) -> "Grocery logistics"
                else -> "Batch planning"
            }
            val planItems = listOf(
                JourneyPlanItem(
                    type = "weekly_menu",
                    title = "5-Night Dinner Rhythm",
                    content = "Monday pantry pasta, Tuesday one-pan protein and vegetables, Wednesday fridge-clearance bowl, Thursday simple soup or stew, Friday flexible family favorite.",
                ),
                JourneyPlanItem(
                    type = "grocery_list",
                    title = "Focused Grocery List",
                    content = "Buy local produce, two proteins, pantry staples, lunch extras, and one backup meal from $location while respecting $restrictions.",
                ),
                JourneyPlanItem(
                    type = "prep_block",
                    title = "Weekend Prep Block",
                    content = "Wash and chop produce, batch-cook two base ingredients, and double dinner portions when lunch leftovers are useful.",
                ),
                JourneyPlanItem(
                    type = "constraints",
                    title = "Cooking Guardrails",
                    content = "Keep weeknight cooking within ${profile.busyWeeknightCookTime.ifBlank { "the available time" }}, avoid ${profile.dislikedIngredients.ifBlank { "listed dislikes" }}, and match ${profile.cookingSkillLevel.ifBlank { "the household skill level" }}.",
                ),
            )

            Result.success(
                SmartGoalProposal(
                    title = "Weekly Meal Plan",
                    subtitle = "$mode for ${profile.cookingFor.ifBlank { "the household" }} people",
                    specific = "Create a realistic weekly meal plan using $restrictions, avoiding ${profile.dislikedIngredients.ifBlank { "no disliked ingredients listed" }}.",
                    measurable = "Cover 5 weeknight dinners, $lunchStrategy, and one focused grocery list tailored to $location.",
                    achievable = "Keep weeknight cooking within ${profile.busyWeeknightCookTime.ifBlank { "the available time" }} and match the style: ${profile.cookingSkillLevel.ifBlank { "average home cook" }}.",
                    relevant = "Use local shops and available products so the plan is easier to buy, cook, and repeat.",
                    timeBound = "Plan the next 7 days, with the first grocery run prepared today.",
                    suggestedTasks = listOf(
                        "Monday: quick pantry pasta with seasonal vegetables",
                        "Tuesday: one-pan protein and vegetables within ${profile.busyWeeknightCookTime.ifBlank { "30 minutes" }}",
                        "Wednesday: fridge-clearance bowl using open ingredients first",
                        "Thursday: simple soup or stew with enough portions to support lunch",
                        "Friday: flexible family favorite that respects $restrictions",
                        "Weekend prep: wash, chop, and batch-cook two base ingredients",
                        "Grocery list: buy local produce, proteins, pantry staples, and lunch extras from $location"
                    ),
                    planItems = planItems,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateFinancialBlueprint(profile: FinanceProfile): Result<SmartGoalProposal> {
        return try {
            delay(1500)

            val saverSpenderDynamic = profile.partnerASpendingStyle != profile.partnerBSpendingStyle
            val recurringBillSummary = profile.recurringBills
                .takeIf { it.isNotEmpty() }
                ?.joinToString(", ")
                ?: "shared recurring bills"
            val monthlyTotal = profile.monthlyReportedSpendTotal
            val monthlyBreakdown = profile.monthlySpendingBreakdown
                .takeIf { it.isNotEmpty() }
                ?.joinToString(", ") { (category, amount) -> "$category ${amount.toCurrencyText()}" }
                ?: "no reported monthly spend yet"
            val largestCategory = profile.monthlySpendingBreakdown.maxByOrNull { it.second }?.first
            val partnerAIncome = profile.partnerAIncome.toAmountOrNull()
            val partnerBIncome = profile.partnerBIncome.toAmountOrNull()
            val totalIncome = (partnerAIncome ?: 0.0) + (partnerBIncome ?: 0.0)
            val fairSplit = if (
                profile.billSplitStrategy.contains("Proportional", ignoreCase = true) &&
                partnerAIncome != null &&
                partnerBIncome != null &&
                totalIncome > 0.0
            ) {
                val partnerAPercent = (partnerAIncome / totalIncome * 100).toInt()
                val partnerBPercent = 100 - partnerAPercent
                "$partnerAPercent% Partner A / $partnerBPercent% Partner B"
            } else if (
                profile.billSplitStrategy.contains("Custom", ignoreCase = true) &&
                profile.customSplitPercentages.isNotBlank()
            ) {
                profile.customSplitPercentages
            } else if (profile.billSplitStrategy.contains("50/50", ignoreCase = true)) {
                "50% Partner A / 50% Partner B"
            } else if (profile.billSplitStrategy.contains("assign", ignoreCase = true)) {
                "assign each bill to a specific owner"
            } else {
                profile.billSplitStrategy.ifBlank { "confirm the split rule" }
            }
            val mentalLoad = when {
                profile.billManager.contains("Partner A", ignoreCase = true) -> "Partner A is carrying most of the bill mental load"
                profile.billManager.contains("Partner B", ignoreCase = true) -> "Partner B is carrying most of the bill mental load"
                profile.billManager.contains("chaotic", ignoreCase = true) -> "bill ownership is unclear and creating avoidable stress"
                else -> "you are already trying to manage bills together"
            }
            val dynamic = if (saverSpenderDynamic) {
                "a saver-spender dynamic"
            } else {
                "a similar money personality pattern"
            }
            val featureFocus = when {
                profile.billPainPoint.contains("due", ignoreCase = true) -> "proactive bill calendar reminders"
                profile.billPainPoint.contains("nagging", ignoreCase = true) -> "one monthly smart-settle notification"
                profile.billPainPoint.contains("fluctuate", ignoreCase = true) -> "variable bill math"
                profile.billPainPoint.contains("Project Manager", ignoreCase = true) -> "automatic bill inventory tracking"
                profile.dailyAnnoyance.contains("bills", ignoreCase = true) -> "automated bill tracking"
                profile.dailyAnnoyance.contains("owes", ignoreCase = true) -> "expense splitting"
                profile.dailyAnnoyance.contains("groceries", ignoreCase = true) ||
                    profile.dailyAnnoyance.contains("dining", ignoreCase = true) -> "spending guardrails"
                else -> "neutral spending insights"
            }
            val settlePlan = when {
                profile.settleWorkflow.contains("Venmos", ignoreCase = true) ||
                    profile.settleWorkflow.contains("Zelles", ignoreCase = true) -> {
                    "replace back-and-forth payments with one net balance notification on the 28th"
                }
                profile.settleWorkflow.contains("lump sum", ignoreCase = true) -> {
                    "keep a running ledger and preview the end-of-month lump sum before it surprises anyone"
                }
                profile.settleWorkflow.contains("joint account", ignoreCase = true) -> {
                    "turn the joint account into a bill runway with funding reminders before autopay"
                }
                else -> "use one shared ledger so both partners see the same numbers"
            }
            val goalPlan = when {
                profile.primaryGoal.contains("emergency", ignoreCase = true) -> "automate a weekly emergency-fund transfer before discretionary spending"
                profile.primaryGoal.contains("debt", ignoreCase = true) -> "prioritize one high-interest balance and route extra cash there first"
                profile.primaryGoal.contains("milestone", ignoreCase = true) -> "create a sinking fund with a weekly target for the milestone"
                else -> "build a calm monthly rhythm with clear fun-money limits"
            }
            val irregularPlan = if (profile.irregularExpensePlan.contains("No", ignoreCase = true)) {
                "Add a sinking fund for irregular expenses so surprises do not land on credit cards."
            } else {
                "Keep the irregular-expense fund visible and review it during check-ins."
            }
            val planItems = listOf(
                JourneyPlanItem(
                    type = "split_rule",
                    title = "Shared Split Rule",
                    content = "Use $fairSplit for recurring and variable household bills unless a bill is explicitly assigned to one partner.",
                ),
                JourneyPlanItem(
                    type = "bill_rhythm",
                    title = "Bill Operating Rhythm",
                    content = "Track $recurringBillSummary in one shared ledger, confirm due dates once, and preview the net balance before the monthly settle-up.",
                ),
                JourneyPlanItem(
                    type = "spending_snapshot",
                    title = "Monthly Spending Snapshot",
                    content = "$monthlyBreakdown. Reported total: ${monthlyTotal.toCurrencyText()}. Largest category: ${largestCategory ?: "not enough data yet"}.",
                ),
                JourneyPlanItem(
                    type = "settle_workflow",
                    title = "Smart Settle Workflow",
                    content = "$settlePlan $irregularPlan",
                ),
                JourneyPlanItem(
                    type = "north_star",
                    title = "Financial North Star",
                    content = "${profile.primaryGoal.ifBlank { "Gain peace of mind" }}: $goalPlan.",
                ),
            )

            Result.success(
                SmartGoalProposal(
                    title = "Custom Household Ledger",
                    subtitle = "Bill tracking and smart splitting for $recurringBillSummary",
                    specific = "Diagnosis: You have $dynamic, $mentalLoad, and the ledger should use $fairSplit.",
                    measurable = "Quick win: Start with $featureFocus and track ${profile.recurringBills.size.coerceAtLeast(1)} recurring bill groups in one shared ledger. Reported monthly spend: ${monthlyTotal.toCurrencyText()}.",
                    achievable = "Smart Settle: $settlePlan. Receipt text-to-split can log variable bills, apply $fairSplit, and update the balance automatically.",
                    relevant = "Monthly Spending Snapshot: $monthlyBreakdown. Largest category: ${largestCategory ?: "not enough reported data"}. North Star: ${profile.primaryGoal.ifBlank { "Gain peace of mind" }}. $goalPlan.",
                    timeBound = "Build the ledger this week, confirm due dates once, then review the net balance monthly.",
                    suggestedTasks = listOf(
                        "Create ledger categories for: $recurringBillSummary",
                        "Apply split rule: $fairSplit",
                        "Review monthly reported spending: ${monthlyTotal.toCurrencyText()}",
                        "Add placeholder due dates for selected recurring bills",
                        "Send one smart-settle reminder on the 28th of each month",
                        "Enable receipt text-to-split for variable bills like utilities",
                        irregularPlan,
                        "Track the primary goal alongside bills: ${profile.primaryGoal.ifBlank { "peace of mind" }}"
                    ),
                    planItems = planItems,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateCouplesWellnessPlan(profile: HealthWellnessProfile): Result<SmartGoalProposal> {
        return try {
            delay(1500)

            val conflictTopic = profile.conflictTopic.ifBlank { "the current friction point" }
            val conflictDraft = profile.conflictDraft.ifBlank { "what you are tempted to say" }
            val loveLanguage = profile.partnerLoveLanguage.ifBlank { "their preferred way of feeling appreciated" }
            val availableTime = profile.availableTime.ifBlank { "15 minutes" }
            val budget = profile.budget.ifBlank { "\$10" }
            val weeklyDrain = profile.weeklyDrain.ifBlank { "the biggest drain this week" }
            val dateDuration = profile.dateNightDuration.ifBlank { "2 hours" }
            val dateLikes = profile.dateNightLikes.ifBlank { "something easy and cozy" }
            val dateAvoids = profile.dateNightAvoids.ifBlank { "anything that feels like another chore" }
            val energy = profile.energyLevel.ifBlank { "5" }
            val stress = profile.stressLevel.ifBlank { "5" }
            val connection = profile.connectionLevel.ifBlank { "5" }

            val translation = buildString {
                append("Instead of leading with '$conflictDraft', try: ")
                append("'I am feeling stretched around $conflictTopic, and I want us to solve it as a team. ")
                append("Could we take 10 minutes to decide the next fair step together?'")
            }
            val appreciationPlan = when {
                loveLanguage.contains("Acts", ignoreCase = true) -> "Use $availableTime to remove one small task from their plate, then spend $budget on a practical comfort like coffee, tea, or a favorite snack."
                loveLanguage.contains("Words", ignoreCase = true) -> "Write a specific three-line note naming what you noticed, why it mattered, and one thing you admire about them."
                loveLanguage.contains("Time", ignoreCase = true) -> "Protect $availableTime of undistracted time, put phones away, and ask one real question without trying to fix anything."
                loveLanguage.contains("Touch", ignoreCase = true) -> "Offer a long hug, shoulder rub, or hand-hold first, then ask what would feel good tonight."
                loveLanguage.contains("Gifts", ignoreCase = true) -> "Spend $budget on a tiny, personal cue that says 'I know you', not a generic gift."
                else -> "Use $availableTime and $budget for one tiny signal that matches what usually makes them feel seen."
            }
            val temperaturePlan = when {
                stress.toIntOrNull()?.let { it >= 8 } == true || energy.toIntOrNull()?.let { it <= 3 } == true -> {
                    "You are running low on fuel. Lower the bar this week: reduce one optional task, order or simplify one meal, and schedule one solo decompression block for each partner."
                }
                connection.toIntOrNull()?.let { it <= 4 } == true -> {
                    "Connection needs a small deposit. Pick one 20-minute ritual with no screens and no logistics talk."
                }
                else -> {
                    "The dashboard looks workable. Keep prevention simple with one weekly check-in and one visible appreciation gesture."
                }
            }
            val datePlan = "Create a $dateDuration at-home date around $dateLikes, while avoiding $dateAvoids. Make it frictionless: one setup task, one shared activity, and one conversation card."
            val planItems = listOf(
                JourneyPlanItem(
                    type = "de_escalator",
                    title = "The Translation",
                    content = translation,
                ),
                JourneyPlanItem(
                    type = "hidden_insight",
                    title = "Why This Works",
                    content = "This phrasing lowers defensiveness because it names your feeling, the shared problem, and a small next step without making your partner the villain.",
                ),
                JourneyPlanItem(
                    type = "appreciation_action",
                    title = "Make Them Feel Seen",
                    content = appreciationPlan,
                ),
                JourneyPlanItem(
                    type = "weekly_radar",
                    title = "The Radar",
                    content = "Energy $energy/10, Stress $stress/10, Connection $connection/10. Main drain: $weeklyDrain.",
                ),
                JourneyPlanItem(
                    type = "game_plan",
                    title = "The Gameplan",
                    content = temperaturePlan,
                ),
                JourneyPlanItem(
                    type = "date_night",
                    title = "Date Night Plan",
                    content = datePlan,
                ),
            )

            Result.success(
                SmartGoalProposal(
                    title = "Couples Wellness Check-In",
                    subtitle = "Low-friction support for conflict, care, prevention and connection",
                    specific = "Use the app as a neutral mediator: it translates blame into team language around $conflictTopic and never decides who is right.",
                    measurable = "Track Energy $energy/10, Stress $stress/10, Connection $connection/10, plus the main weekly drain: $weeklyDrain.",
                    achievable = "De-escalator: $translation Appreciation plan: $appreciationPlan",
                    relevant = "Golden rule: it is not you vs. them; it is you + them vs. the problem. $temperaturePlan The AI should coach tone, timing and next actions without taking sides.",
                    timeBound = "Run this check-in once this week, then review whether stress moved down or connection moved up within 7 days.",
                    suggestedTasks = listOf(
                        "Send the de-escalated message about $conflictTopic",
                        "Do one $loveLanguage appreciation action within $availableTime",
                        "Reduce one drain linked to $weeklyDrain this week",
                        "Schedule one solo decompression block for each partner",
                        "Plan the at-home date: $datePlan",
                        "Run the three-slider temperature check again next week"
                    ),
                    planItems = planItems,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateLongTermProjectBlueprint(profile: LongTermProjectProfile): Result<SmartGoalProposal> {
        return try {
            delay(1500)

            val milestone = profile.milestoneType.ifBlank { "long-term milestone" }
            val finishLine = profile.successDefinition.ifBlank { "a finished project that feels calm, clear and shared" }
            val cadence = when {
                profile.timeline.contains("ASAP", ignoreCase = true) -> "daily 20-minute sprints for the next two weeks"
                profile.timeline.contains("1-6", ignoreCase = true) ||
                    profile.timeline.contains("Medium", ignoreCase = true) -> "two micro-tasks per week"
                else -> "one focused planning block per week"
            }
            val budgetGuardrail = when {
                profile.budgetStyle.contains("Tight", ignoreCase = true) ||
                    profile.budgetStyle.contains("Bootstrapping", ignoreCase = true) -> "DIY-first decisions and a simple must-have versus nice-to-have list"
                profile.budgetStyle.contains("Full", ignoreCase = true) ||
                    profile.budgetStyle.contains("Service", ignoreCase = true) -> "vendor shortlists, quote comparisons and clear handoff checklists"
                else -> "DIY for low-risk work and professional help for expensive or specialized tasks"
            }
            val frictionPlan = when {
                profile.roadblock.contains("Time", ignoreCase = true) -> "protect tiny calendar blocks so daily life does not swallow the project"
                profile.roadblock.contains("Alignment", ignoreCase = true) -> "run a short decision meeting with budget, style and priority votes before any spending"
                profile.roadblock.contains("Overwhelmed", ignoreCase = true) -> "follow the steps in strict order and hide later decisions until the current one is done"
                profile.roadblock.contains("Accountability", ignoreCase = true) -> "assign one owner per task and review progress every Sunday"
                else -> "choose the next visible action and keep the plan small enough to maintain"
            }
            val roleDelegation = when {
                profile.roadblock.contains("Alignment", ignoreCase = true) -> "Partner A gathers options, Partner B scores trade-offs, then both choose from the same shortlist."
                profile.roadblock.contains("Time", ignoreCase = true) -> "One partner owns scheduling, the other owns prep work, with tasks capped at 20 minutes."
                profile.roadblock.contains("Accountability", ignoreCase = true) -> "One partner owns the checklist, the other owns reminders and follow-through."
                else -> "Partner A owns logistics, Partner B owns research, and both approve cost or style decisions."
            }
            val planItems = listOf(
                JourneyPlanItem(
                    type = "finish_line",
                    title = "Finish Line",
                    content = finishLine,
                ),
                JourneyPlanItem(
                    type = "cadence",
                    title = "Progress Cadence",
                    content = "Use $cadence and keep the visible plan to the next 3 decisions plus the next 2 actions.",
                ),
                JourneyPlanItem(
                    type = "budget_guardrail",
                    title = "Budget Guardrail",
                    content = budgetGuardrail,
                ),
                JourneyPlanItem(
                    type = "friction_strategy",
                    title = "Friction Strategy",
                    content = frictionPlan,
                ),
                JourneyPlanItem(
                    type = "role_split",
                    title = "Role Split",
                    content = roleDelegation,
                ),
            )

            Result.success(
                SmartGoalProposal(
                    title = "$milestone Blueprint",
                    subtitle = "A lightweight roadmap for: $finishLine",
                    specific = "Define the finish line as: $finishLine.",
                    measurable = "Track progress with $cadence and keep the visible plan to the next 3 decisions plus the next 2 actions.",
                    achievable = "Use $budgetGuardrail. Roadblock strategy: $frictionPlan.",
                    relevant = "Role split: $roleDelegation This keeps the milestone moving without asking the household to build a giant spreadsheet first.",
                    timeBound = profile.timeline.ifBlank { "Set a realistic target date during the first planning check-in." },
                    suggestedTasks = listOf(
                        "Today: write the one-sentence finish line somewhere shared",
                        "Create a three-column board: Decide, Do, Waiting",
                        "Pick the next 2 micro-tasks for this week",
                        "Set the budget guardrail: ${profile.budgetStyle.ifBlank { "choose tight, moderate or full service" }}",
                        "Name the current roadblock: ${profile.roadblock.ifBlank { "time, alignment, overwhelm or accountability" }}",
                        "Assign owners: $roleDelegation",
                        "Schedule the first 15-minute check-in",
                        "Review progress cadence: $cadence"
                    ),
                    planItems = planItems,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun String.toAmountOrNull(): Double? {
    return filter { it.isDigit() || it == '.' }
        .takeIf { it.isNotBlank() }
        ?.toDoubleOrNull()
}

private fun Double.toCurrencyText(): String {
    val wholeAmount = toInt()
    return "\$$wholeAmount/mo"
}
