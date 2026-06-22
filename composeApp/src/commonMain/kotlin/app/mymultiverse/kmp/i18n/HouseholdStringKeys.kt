package app.mymultiverse.kmp.i18n

/**
 * User-facing keys for household onboarding gate and membership flows.
 * Every locale file under composeResources must define each key.
 */
object HouseholdStringKeys {
    val all: Set<String> = setOf(
        "household_gate_title",
        "household_gate_subtitle",
        "household_gate_create_title",
        "household_gate_name_label",
        "household_gate_create_button",
        "household_gate_loading",
        "household_gate_error_generic",
        "household_gate_error_not_configured",
        "household_gate_error_already_active",
        "household_gate_retry",
        "household_gate_create_divider",
        "household_name_checking",
        "household_name_available",
        "household_name_taken",
        "household_name_invalid",
        "home_logistics_requires_household",
    )

    val localeDirectories: List<String> = NutritionStringKeys.localeDirectories
}
