package com.example.kmp.data.service

import com.example.kmp.domain.model.MealPlanningProfile
import com.example.kmp.domain.model.SmartGoalProposal
import com.example.kmp.domain.service.AiService
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
                    )
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
