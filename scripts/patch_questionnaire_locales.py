#!/usr/bin/env python3
"""Apply questionnaire and detail translations from scripts/translations/*.json."""
from __future__ import annotations

import html
import json
import re
from pathlib import Path

RES = Path(__file__).resolve().parents[1] / "composeApp/src/commonMain/composeResources"
TRANSLATIONS = Path(__file__).resolve().parent / "translations"

LOCALE_FILES = {
    "es": "es.json",
    "fr": "fr.json",
    "de": "de.json",
    "it": "it.json",
    "nap": "nap.json",
    "ar": "ar.json",
    "ar-rSA": "ar.json",
}

CALENDAR_FILES = {
    "it": "it_calendar.json",
    "ar": "ar_calendar.json",
    "ar-rSA": "ar_calendar.json",
}

# Fix triple-encoded ampersands introduced during sync
AMP_FIX = {
    "finance_q_bill_tracking_title": "Bill Tracking & Splitting",
    "finance_spend_kids_pets": "Kids & Pets",
    "finance_opt_bill_kids_pets": "Kids & Pets (Daycare, Tuition, Pet Insurance)",
}


def load_json(name: str) -> dict[str, str]:
    return json.loads((TRANSLATIONS / name).read_text(encoding="utf-8"))


def patch_locale(locale: str, patches: dict[str, str]) -> int:
    folder = f"values-{locale}" if locale != "en" else "values"
    path = RES / folder / "strings.xml"
    text = path.read_text(encoding="utf-8")
    updated = 0
    for key, value in patches.items():
        escaped = html.escape(value, quote=False)
        pattern = rf'(<string name="{re.escape(key)}">)(.*?)(</string>)'

        def repl(m: re.Match[str], val: str = escaped) -> str:
            return m.group(1) + val + m.group(3)

        new_text, count = re.subn(pattern, repl, text, count=1, flags=re.DOTALL)
        if count:
            text = new_text
            updated += 1
    path.write_text(text, encoding="utf-8")
    return updated


def localized_amp_fix(locale: str) -> dict[str, str]:
    if locale not in ("es", "fr", "de", "it", "nap", "ar", "ar-rSA"):
        return {}
    data = load_json(LOCALE_FILES.get(locale, "es.json") if locale != "ar-rSA" else "ar.json")
    out: dict[str, str] = {}
    for key in AMP_FIX:
        if key in data:
            out[key] = data[key]
    return out


def main() -> None:
    for locale, json_file in LOCALE_FILES.items():
        patches = load_json(json_file)
        if locale in CALENDAR_FILES:
            patches = {**patches, **load_json(CALENDAR_FILES[locale])}
        n = patch_locale(locale, patches)
        print(f"values-{locale}: updated {n} strings")

    # Fix base English double-encoding
    en_fixes = AMP_FIX.copy()
    n = patch_locale("en", en_fixes)
    print(f"values (en): amp fix updated {n} strings")


if __name__ == "__main__":
    main()
