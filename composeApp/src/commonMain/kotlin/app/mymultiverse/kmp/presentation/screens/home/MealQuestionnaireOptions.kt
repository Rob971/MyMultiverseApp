package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.runtime.Composable
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*

@Composable
fun mealPartySizeOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption("1", Res.string.meal_opt_party_1),
    LocalizedSelectOption("2", Res.string.meal_opt_party_2),
    LocalizedSelectOption("3-4", Res.string.meal_opt_party_3_4),
    LocalizedSelectOption("5+", Res.string.meal_opt_party_5_plus),
)

@Composable
fun mealDietaryOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption("Vegetarian", Res.string.meal_opt_diet_vegetarian),
    LocalizedSelectOption("Vegan", Res.string.meal_opt_diet_vegan),
    LocalizedSelectOption("Gluten-Free", Res.string.meal_opt_diet_gluten_free),
    LocalizedSelectOption("Keto", Res.string.meal_opt_diet_keto),
    LocalizedSelectOption("Nut Allergy", Res.string.meal_opt_diet_nut_allergy),
    LocalizedSelectOption(MealQuestionnaireValues.DIETARY_NONE, Res.string.meal_opt_diet_none),
)

@Composable
fun mealWeeknightTimeOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(MealQuestionnaireValues.TIME_UNDER_15, Res.string.meal_opt_time_under_15),
    LocalizedSelectOption(MealQuestionnaireValues.TIME_15_30, Res.string.meal_opt_time_15_30),
    LocalizedSelectOption(MealQuestionnaireValues.TIME_30_60, Res.string.meal_opt_time_30_60),
)

@Composable
fun mealSkillOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(MealQuestionnaireValues.SKILL_SIMPLE, Res.string.meal_opt_skill_simple),
    LocalizedSelectOption(MealQuestionnaireValues.SKILL_AVERAGE, Res.string.meal_opt_skill_average),
    LocalizedSelectOption(MealQuestionnaireValues.SKILL_CHALLENGE, Res.string.meal_opt_skill_challenge),
)

@Composable
fun mealLunchOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(MealQuestionnaireValues.LUNCH_LEFTOVERS, Res.string.meal_opt_lunch_leftovers),
    LocalizedSelectOption(MealQuestionnaireValues.LUNCH_FRESH, Res.string.meal_opt_lunch_fresh),
)

@Composable
fun mealRightNowGoalOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(MealQuestionnaireValues.GOAL_WEEK, Res.string.meal_opt_goal_week),
    LocalizedSelectOption(MealQuestionnaireValues.GOAL_FRIDGE, Res.string.meal_opt_goal_fridge),
    LocalizedSelectOption(MealQuestionnaireValues.GOAL_GROCERY, Res.string.meal_opt_goal_grocery),
)

@Composable
fun mealLocationOptions(): List<LocalizedSelectOption> = listOf(
    LocalizedSelectOption(MealQuestionnaireValues.LOCATION_GPS, Res.string.meal_opt_location_gps),
    LocalizedSelectOption(MealQuestionnaireValues.LOCATION_MANUAL, Res.string.meal_opt_location_manual),
)
