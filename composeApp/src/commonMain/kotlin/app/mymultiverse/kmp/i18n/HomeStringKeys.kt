package app.mymultiverse.kmp.i18n

/**
 * User-facing keys for the home screen and shared logistics chrome.
 * Every locale file under composeResources must define each key.
 */
object HomeStringKeys {
    val all: Set<String> = setOf(
        "home_banner_headline",
        "home_banner_loading",
        "home_banner_description",
        "home_dreams_title",
        "home_logistics_nutrition_title",
        "home_logistics_nutrition_description",
        "home_logistics_adventures_title",
        "home_logistics_adventures_description",
        "home_logistics_budget_title",
        "home_logistics_budget_description",
        "home_logistics_coming_soon",
        "home_refresh_inspirations",
        "home_greeting",
        "home_app_version",
        "home_app_version_rc",
        "action_back",
    )

    val localeDirectories: List<String> = NutritionStringKeys.localeDirectories
}
