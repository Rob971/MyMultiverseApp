package app.mymultiverse.kmp.i18n

/**
 * Canonical list of user-facing string keys for the Nutrition feature.
 * Every locale file under composeResources must define each key.
 */
object NutritionStringKeys {
    val all: Set<String> = setOf(
        "home_logistics_nutrition_title",
        "home_logistics_nutrition_description",
        "nutrition_hub_title",
        "nutrition_hub_subtitle",
        "nutrition_grocery_title",
        "nutrition_grocery_description",
        "nutrition_meal_plan_title",
        "nutrition_meal_plan_description",
        "nutrition_ai_title",
        "nutrition_ai_description",
        "nutrition_week_label",
        "nutrition_grocery_add_hint",
        "nutrition_grocery_add_button",
        "nutrition_grocery_empty",
        "nutrition_meal_lunch",
        "nutrition_meal_dinner",
        "nutrition_day_monday",
        "nutrition_day_tuesday",
        "nutrition_day_wednesday",
        "nutrition_day_thursday",
        "nutrition_day_friday",
        "nutrition_day_saturday",
        "nutrition_day_sunday",
        "nutrition_ai_question_hint",
        "nutrition_ai_ask_button",
        "nutrition_ai_loading",
        "nutrition_ai_error",
        "nutrition_ai_empty_question",
        "home_logistics_coming_soon",
        "nutrition_week_dates",
        "nutrition_grocery_progress",
        "nutrition_meal_plan_progress",
        "nutrition_today",
        "nutrition_delete_item",
        "nutrition_ai_try_again",
        "nutrition_ai_suggestions_title",
        "nutrition_ai_suggestion_protein",
        "nutrition_ai_suggestion_veggies",
        "nutrition_ai_suggestion_allergy",
    )

    val localeDirectories: List<String> = listOf(
        "values",
        "values-fr",
        "values-es",
        "values-de",
        "values-it",
        "values-ar",
        "values-ar-rSA",
        "values-nap",
    )
}
