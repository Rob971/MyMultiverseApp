#!/usr/bin/env python3
"""Patch locale files for calendar, detail chrome, and dream card UI strings still in English."""
from __future__ import annotations

import html
import re
from pathlib import Path

RES = Path(__file__).resolve().parents[1] / "composeApp/src/commonMain/composeResources"

PATCHES: dict[str, dict[str, str]] = {
    "es": {
        "home_greeting": "El corazón latiente de nuestra familia.",
        "calendar_weekday_mon": "Lun",
        "calendar_weekday_tue": "Mar",
        "calendar_weekday_wed": "Mié",
        "calendar_weekday_thu": "Jue",
        "calendar_weekday_fri": "Vie",
        "calendar_weekday_sat": "Sáb",
        "calendar_weekday_sun": "Dom",
        "calendar_nav_previous": "Anterior",
        "calendar_nav_next": "Siguiente",
        "calendar_header_today": "Hoy, %1$d %2$s",
        "calendar_header_week_same_month": "%1$d - %2$d %3$s, %4$d",
        "calendar_header_week_diff_month": "%1$d %2$s - %3$d %4$s, %5$d",
        "calendar_header_month": "%1$s %2$d",
        "month_january": "Enero",
        "month_february": "Febrero",
        "month_march": "Marzo",
        "month_april": "Abril",
        "month_may": "Mayo",
        "month_june": "Junio",
        "month_july": "Julio",
        "month_august": "Agosto",
        "month_september": "Septiembre",
        "month_october": "Octubre",
        "month_november": "Noviembre",
        "month_december": "Diciembre",
        "dream_card_open_detail": "Abrir detalle",
        "dream_card_menu": "Menú",
        "dream_card_collapse_tasks": "Contraer actividades",
        "dream_card_expand_tasks": "Expandir actividades",
        "dream_card_streak_days": "%1$d días",
        "insights_footer": "Estos modelos refuerzan la psicología de la constancia sobre la perfección. Cada pequeña acción construye el futuro de nuestra familia.",
        "detail_celebration_subtitle": "Un corazón, una familia.",
        "detail_connection": "Conexión",
        "detail_energy": "Energía",
        "detail_stress": "Estrés",
        "detail_schedule": "Horario",
        "detail_show_strategy": "Ver estrategia",
        "detail_hide_strategy": "Ocultar estrategia",
        "detail_wellness_radar_title": "Radar de bienestar en pareja",
        "detail_wellness_radar_subtitle": "Tú + Ellos vs. El problema. Usa esta tarjeta para traducir la tensión en un siguiente paso compartido.",
    },
    "fr": {
        "home_greeting": "Le cœur battant de notre famille.",
        "calendar_weekday_mon": "Lun",
        "calendar_weekday_tue": "Mar",
        "calendar_weekday_wed": "Mer",
        "calendar_weekday_thu": "Jeu",
        "calendar_weekday_fri": "Ven",
        "calendar_weekday_sat": "Sam",
        "calendar_weekday_sun": "Dim",
        "calendar_nav_previous": "Précédent",
        "calendar_nav_next": "Suivant",
        "calendar_header_today": "Aujourd'hui, %1$d %2$s",
        "calendar_header_week_same_month": "%1$d - %2$d %3$s, %4$d",
        "calendar_header_week_diff_month": "%1$d %2$s - %3$d %4$s, %5$d",
        "calendar_header_month": "%1$s %2$d",
        "month_january": "Janvier",
        "month_february": "Février",
        "month_march": "Mars",
        "month_april": "Avril",
        "month_may": "Mai",
        "month_june": "Juin",
        "month_july": "Juillet",
        "month_august": "Août",
        "month_september": "Septembre",
        "month_october": "Octobre",
        "month_november": "Novembre",
        "month_december": "Décembre",
        "dream_card_open_detail": "Ouvrir le détail",
        "dream_card_menu": "Menu",
        "dream_card_collapse_tasks": "Réduire les activités",
        "dream_card_expand_tasks": "Développer les activités",
        "dream_card_streak_days": "%1$d jours",
        "insights_footer": "Ces modèles renforcent la constance plutôt que la perfection. Chaque petite action construit l'avenir de notre famille.",
        "detail_celebration_subtitle": "Un cœur, une famille.",
        "detail_connection": "Connexion",
        "detail_energy": "Énergie",
        "detail_stress": "Stress",
        "detail_schedule": "Planning",
        "detail_show_strategy": "Voir la stratégie",
        "detail_hide_strategy": "Masquer la stratégie",
        "detail_wellness_radar_title": "Radar bien-être du couple",
        "detail_wellness_radar_subtitle": "Vous + Eux vs. Le problème. Utilisez cette carte pour transformer la tension en prochaine étape partagée.",
    },
    "de": {
        "home_greeting": "Das schlagende Herz unserer Familie.",
        "calendar_weekday_mon": "Mo",
        "calendar_weekday_tue": "Di",
        "calendar_weekday_wed": "Mi",
        "calendar_weekday_thu": "Do",
        "calendar_weekday_fri": "Fr",
        "calendar_weekday_sat": "Sa",
        "calendar_weekday_sun": "So",
        "calendar_nav_previous": "Zurück",
        "calendar_nav_next": "Weiter",
        "calendar_header_today": "Heute, %1$d. %2$s",
        "calendar_header_week_same_month": "%1$d. - %2$d. %3$s %4$d",
        "calendar_header_week_diff_month": "%1$d. %2$s - %3$d. %4$s %5$d",
        "calendar_header_month": "%1$s %2$d",
        "month_january": "Januar",
        "month_february": "Februar",
        "month_march": "März",
        "month_april": "April",
        "month_may": "Mai",
        "month_june": "Juni",
        "month_july": "Juli",
        "month_august": "August",
        "month_september": "September",
        "month_october": "Oktober",
        "month_november": "November",
        "month_december": "Dezember",
        "dream_card_open_detail": "Details öffnen",
        "dream_card_menu": "Menü",
        "dream_card_collapse_tasks": "Aktivitäten einklappen",
        "dream_card_expand_tasks": "Aktivitäten ausklappen",
        "dream_card_streak_days": "%1$d Tage",
        "insights_footer": "Diese Modelle stärken Beständigkeit statt Perfektion. Jede kleine Handlung baut unsere Familienzukunft.",
        "detail_celebration_subtitle": "Ein Herz, eine Familie.",
        "detail_connection": "Verbindung",
        "detail_energy": "Energie",
        "detail_stress": "Stress",
        "detail_schedule": "Zeitplan",
        "detail_show_strategy": "Strategie anzeigen",
        "detail_hide_strategy": "Strategie ausblenden",
        "detail_wellness_radar_title": "Paar-Wellness-Radar",
        "detail_wellness_radar_subtitle": "Ihr + Sie vs. Das Problem. Nutzt diese Karte, um Spannung in einen gemeinsamen nächsten Schritt zu übersetzen.",
    },
    "nap": {
        "home_greeting": "'O core ca batte d' 'a famiglia.",
        "calendar_weekday_mon": "Lun",
        "calendar_weekday_tue": "Mar",
        "calendar_weekday_wed": "Mer",
        "calendar_weekday_thu": "Gio",
        "calendar_weekday_fri": "Ven",
        "calendar_weekday_sat": "Sab",
        "calendar_weekday_sun": "Dom",
        "calendar_nav_previous": "Arreto",
        "calendar_nav_next": "Avanti",
        "calendar_header_today": "Oggi, %1$d %2$s",
        "calendar_header_week_same_month": "%1$d - %2$d %3$s, %4$d",
        "calendar_header_week_diff_month": "%1$d %2$s - %3$d %4$s, %5$d",
        "calendar_header_month": "%1$s %2$d",
        "month_january": "Jennaro",
        "month_february": "Frevaro",
        "month_march": "Marzo",
        "month_april": "Aprile",
        "month_may": "Maggio",
        "month_june": "Giugno",
        "month_july": "Luglio",
        "month_august": "Aosto",
        "month_september": "Settembre",
        "month_october": "Ottobre",
        "month_november": "Nuvembre",
        "month_december": "Dicembre",
        "dream_card_open_detail": "Arè dettaglio",
        "dream_card_menu": "Menù",
        "dream_card_collapse_tasks": "Chiudi attività",
        "dream_card_expand_tasks": "Apri attività",
        "dream_card_streak_days": "%1$d juorne",
        "insights_footer": "Sti modelle rinforzano 'a costanza over 'a perfezione. Ogne piccola azione costruisce 'o futuro d' 'a famiglia.",
        "detail_celebration_subtitle": "Un core, una famiglia.",
        "detail_connection": "Connessione",
        "detail_energy": "Energia",
        "detail_stress": "Stress",
        "detail_schedule": "Agenda",
        "detail_show_strategy": "Vide strategia",
        "detail_hide_strategy": "Cua strategia",
        "detail_wellness_radar_title": "Radar benessere coppia",
        "detail_wellness_radar_subtitle": "Tu + Loro vs. 'O problema. Usa sta scheda pe' tradurre 'a tensione in passo condiviso.",
    },
}

# Italian and Arabic reuse FR/ES patterns lightly
PATCHES["it"] = {**PATCHES["fr"], "home_greeting": "Il cuore pulsante della nostra famiglia."}
PATCHES["ar"] = PATCHES["es"].copy()
PATCHES["ar-rSA"] = PATCHES["es"].copy()


def patch_locale(locale_dir: str, patches: dict[str, str]) -> int:
    path = RES / locale_dir / "strings.xml"
    text = path.read_text(encoding="utf-8")
    updated = 0
    for key, value in patches.items():
        escaped = html.escape(value, quote=False)
        pattern = rf'(<string name="{re.escape(key)}">)(.*?)(</string>)'
        new_text, count = re.subn(pattern, rf"\1{escaped}\3", text, count=1, flags=re.DOTALL)
        if count:
            text = new_text
            updated += 1
    path.write_text(text, encoding="utf-8")
    return updated


def main() -> None:
    for loc, patches in PATCHES.items():
        folder = f"values-{loc}" if loc != "en" else "values"
        n = patch_locale(folder, patches)
        print(f"values-{loc}: updated {n} strings")


if __name__ == "__main__":
    main()
