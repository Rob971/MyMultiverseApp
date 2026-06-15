"""Embedded translations for questionnaire/detail JSON export."""
from __future__ import annotations

from locale_sections import ar, de, es, fr, it, nap

TRANSLATIONS: dict[str, dict[str, str]] = {
    "es": es.DATA,
    "fr": fr.DATA,
    "de": de.DATA,
    "it": it.DATA,
    "nap": nap.DATA,
    "ar": ar.DATA,
}

CALENDAR: dict[str, dict[str, str]] = {
    "it": it.CALENDAR,
    "ar": ar.CALENDAR,
}
