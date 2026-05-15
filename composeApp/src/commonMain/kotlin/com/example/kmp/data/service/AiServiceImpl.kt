package com.example.kmp.data.service

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
}
