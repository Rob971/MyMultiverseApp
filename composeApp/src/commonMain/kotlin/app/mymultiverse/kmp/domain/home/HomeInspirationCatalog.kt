package app.mymultiverse.kmp.domain.home

import app.mymultiverse.kmp.domain.manager.SupportedAppLanguages
import kotlin.random.Random

object HomeInspirationCatalog {

    fun pick(localeCode: String, random: Random = Random.Default): String {
        val lines = linesFor(SupportedAppLanguages.normalize(localeCode))
        return lines[random.nextInt(lines.size)]
    }

    internal fun linesFor(localeCode: String): List<String> =
        when (localeCode) {
            "en" -> english
            "es" -> spanish
            "fr" -> french
            "de" -> german
            "it" -> italian
            "nap" -> neapolitan
            "ar-rSA", "ar" -> arabic
            else -> english
        }

    private val english = listOf(
        "Small steps keep the week calm.",
        "Planning together makes room for joy.",
        "A shared list is a shared load.",
        "One meal planned is one worry off the table.",
        "Your household rhythm starts here.",
    )

    private val italian = listOf(
        "Piccoli passi rendono la settimana più tranquilla.",
        "Pianificare insieme lascia spazio alla gioia.",
        "Una lista condivisa divide il carico.",
        "Un pasto pianificato toglie una preoccupazione.",
        "Il ritmo di famiglia inizia da qui.",
    )

    private val neapolitan = listOf(
        "Picceri passi fanno 'a semmana cchiù calma.",
        "Quanno planificammo insieme, ce sta cchiù allegria.",
        "'Na lista condivisa sparte 'o peso.",
        "Un pasto pianificato levano 'na preoccupazione.",
        "'O ritmo 'e famiglia accummincia d'accà.",
    )

    private val spanish = listOf(
        "Pequeños pasos mantienen la semana tranquila.",
        "Planificar juntos deja espacio para la alegría.",
        "Una lista compartida reparte la carga.",
        "Una comida planificada quita una preocupación.",
        "El ritmo familiar empieza aquí.",
    )

    private val french = listOf(
        "De petits pas rendent la semaine plus calme.",
        "Planifier ensemble laisse place à la joie.",
        "Une liste partagée allège la charge.",
        "Un repas planifié enlève une inquiétude.",
        "Le rythme du foyer commence ici.",
    )

    private val german = listOf(
        "Kleine Schritte halten die Woche ruhig.",
        "Gemeinsam planen schafft Raum für Freude.",
        "Eine geteilte Liste teilt die Last.",
        "Eine geplante Mahlzeit nimmt eine Sorge.",
        "Der Haushaltsrhythmus beginnt hier.",
    )

    private val arabic = listOf(
        "خطوات صغيرة تجعل الأسبوع أكثر هدوءًا.",
        "التخطيط معًا يفسح مجالًا للفرح.",
        "قائمة مشتركة تعني عبئًا مشتركًا.",
        "وجبة مخططة تزيل همًا واحدًا.",
        "إيقاع الأسرة يبدأ من هنا.",
    )
}
