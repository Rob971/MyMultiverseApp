#!/usr/bin/env python3
"""Build questionnaire/detail translation JSON files from embedded locale data."""
from __future__ import annotations

import html
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / "composeApp/src/commonMain/composeResources/values/strings.xml"
OUT = Path(__file__).resolve().parent

DETAIL_KEYS = [
    "detail_all_plan_steps_complete",
    "detail_longterm_milestone",
    "detail_longterm_roadblock",
    "detail_wellness_appreciation",
    "detail_wellness_weekly_drain",
    "detail_wellness_quick_action",
    "detail_wellness_no_budget",
    "detail_wellness_date_night_format",
    "detail_wellness_appreciation_format",
    "detail_wellness_score",
    "detail_partner_a",
    "detail_partner_b",
    "detail_partner_no_one",
    "detail_variable_bill_tracker",
    "detail_variable_bill_desc",
    "detail_saved_split_rule",
    "detail_log_variable_bill",
    "detail_finance_balance_a_owes_b",
    "detail_finance_balance_b_owes_a",
    "detail_finance_all_balanced",
    "detail_bill_entry_meta",
    "detail_bill_entry_owes",
    "detail_finance_dialog_desc",
    "detail_finance_receipt_placeholder",
    "detail_finance_extract_receipt",
    "detail_finance_merchant",
    "detail_finance_date_placeholder",
    "detail_finance_amount_placeholder",
    "detail_finance_category",
    "detail_finance_paid_by",
    "detail_finance_split_preview",
    "detail_finance_save_bill",
    "detail_spending_explorer",
    "detail_spending_period_day",
    "detail_spending_period_week",
    "detail_spending_period_month",
    "detail_spending_period_quarter",
    "detail_reported_spend",
    "detail_top_category",
    "detail_not_enough_data",
    "detail_top_category_share",
    "detail_spending_disclaimer",
    "detail_task_claimed",
    "detail_task_claim",
    "detail_hearts_count",
    "content_more_options",
    "content_cheer",
]

CALENDAR_KEYS = [
    "home_greeting",
    "calendar_weekday_mon",
    "calendar_weekday_tue",
    "calendar_weekday_wed",
    "calendar_weekday_thu",
    "calendar_weekday_fri",
    "calendar_weekday_sat",
    "calendar_weekday_sun",
    "calendar_nav_previous",
    "calendar_nav_next",
    "calendar_header_today",
    "calendar_header_week_same_month",
    "calendar_header_week_diff_month",
    "calendar_header_month",
    "month_january",
    "month_february",
    "month_march",
    "month_april",
    "month_may",
    "month_june",
    "month_july",
    "month_august",
    "month_september",
    "month_october",
    "month_november",
    "month_december",
    "dream_card_open_detail",
    "dream_card_menu",
    "dream_card_collapse_tasks",
    "dream_card_expand_tasks",
    "dream_card_streak_days",
    "insights_footer",
    "detail_celebration_subtitle",
    "detail_schedule",
    "detail_show_strategy",
    "detail_hide_strategy",
    "detail_wellness_radar_title",
    "detail_wellness_radar_subtitle",
    "detail_energy",
    "detail_stress",
    "detail_connection",
]


def load_english() -> dict[str, str]:
    text = RES.read_text(encoding="utf-8")
    raw = dict(re.findall(r'<string name="([^"]+)">(.*?)</string>', text, re.DOTALL))
    return {k: html.unescape(v).replace("\\'", "'") for k, v in raw.items()}


def main_keys(en: dict[str, str]) -> list[str]:
    keys = sorted(k for k in en if k.startswith(("meal_", "finance_", "wellness_", "longterm_")))
    for k in DETAIL_KEYS:
        if k not in keys:
            keys.append(k)
    return sorted(keys)


def write_json(path: Path, data: dict[str, str]) -> int:
    path.write_text(
        json.dumps(dict(sorted(data.items())), ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    return len(data)


def main() -> None:
    from locale_data import CALENDAR, TRANSLATIONS  # noqa: WPS433

    en = load_english()
    keys = main_keys(en)

    for locale, patches in TRANSLATIONS.items():
        missing = [k for k in keys if k not in patches]
        if missing:
            raise SystemExit(f"{locale}: missing {len(missing)} keys: {missing[:5]}...")
        data = {k: patches[k] for k in keys}
        n = write_json(OUT / f"{locale}.json", data)
        print(f"{locale}.json: {n} keys")

    it_cal = {k: CALENDAR["it"][k] for k in CALENDAR_KEYS}
    ar_cal = {k: CALENDAR["ar"][k] for k in CALENDAR_KEYS}
    write_json(OUT / "it_calendar.json", it_cal)
    write_json(OUT / "ar_calendar.json", ar_cal)
    print(f"it_calendar.json: {len(it_cal)} keys")
    print(f"ar_calendar.json: {len(ar_cal)} keys")


if __name__ == "__main__":
    main()
