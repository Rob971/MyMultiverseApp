package app.mymultiverse.ammo.i18n

/**
 * User-facing keys for the product tour / onboarding spotlight walkthrough.
 * Every locale file under composeResources must define each key.
 */
object ProductTourStringKeys {
    val all: Set<String> = setOf(
        "tour_step_counter",
        "tour_action_skip",
        "tour_action_next",
        "tour_action_previous",
        "tour_action_finish",
        "tour_step_welcome_title",
        "tour_step_welcome_body",
        "tour_step_home_title",
        "tour_step_home_body",
        "tour_step_meal_plan_title",
        "tour_step_meal_plan_body",
        "tour_step_grocery_title",
        "tour_step_grocery_body",
    )

    val localeDirectories: List<String> = NutritionStringKeys.localeDirectories
}
