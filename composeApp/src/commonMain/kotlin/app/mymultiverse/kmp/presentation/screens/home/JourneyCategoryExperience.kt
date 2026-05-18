package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.model.JourneyCategory
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource

data class JourneyCategoryExperienceResources(
    val displayName: StringResource,
    val description: StringResource,
    val dreamPrompt: StringResource,
    val outcome: StringResource,
    val questionsIntro: StringResource,
    val aiActionLabel: StringResource,
    val planReviewTitle: StringResource,
)

val JourneyCategory.experienceResources: JourneyCategoryExperienceResources
    get() = when (this) {
        JourneyCategory.CalendarLogistics -> JourneyCategoryExperienceResources(
            displayName = Res.string.category_calendar_title,
            description = Res.string.category_calendar_description,
            dreamPrompt = Res.string.category_calendar_dream_prompt,
            outcome = Res.string.category_calendar_outcome,
            questionsIntro = Res.string.category_calendar_questions_intro,
            aiActionLabel = Res.string.category_calendar_ai_action,
            planReviewTitle = Res.string.category_calendar_plan_title,
        )
        JourneyCategory.HouseholdManagement -> JourneyCategoryExperienceResources(
            displayName = Res.string.category_home_title,
            description = Res.string.category_home_description,
            dreamPrompt = Res.string.category_home_dream_prompt,
            outcome = Res.string.category_home_outcome,
            questionsIntro = Res.string.category_home_questions_intro,
            aiActionLabel = Res.string.category_home_ai_action,
            planReviewTitle = Res.string.category_home_plan_title,
        )
        JourneyCategory.MealPlanning -> JourneyCategoryExperienceResources(
            displayName = Res.string.category_meal_title,
            description = Res.string.category_meal_description,
            dreamPrompt = Res.string.category_meal_dream_prompt,
            outcome = Res.string.category_meal_outcome,
            questionsIntro = Res.string.category_meal_questions_intro,
            aiActionLabel = Res.string.category_meal_ai_action,
            planReviewTitle = Res.string.category_meal_plan_title,
        )
        JourneyCategory.HouseholdFinance -> JourneyCategoryExperienceResources(
            displayName = Res.string.category_finance_title,
            description = Res.string.category_finance_description,
            dreamPrompt = Res.string.category_finance_dream_prompt,
            outcome = Res.string.category_finance_outcome,
            questionsIntro = Res.string.category_finance_questions_intro,
            aiActionLabel = Res.string.category_finance_ai_action,
            planReviewTitle = Res.string.category_finance_plan_title,
        )
        JourneyCategory.HealthWellness -> JourneyCategoryExperienceResources(
            displayName = Res.string.category_health_title,
            description = Res.string.category_health_description,
            dreamPrompt = Res.string.category_health_dream_prompt,
            outcome = Res.string.category_health_outcome,
            questionsIntro = Res.string.category_health_questions_intro,
            aiActionLabel = Res.string.category_health_ai_action,
            planReviewTitle = Res.string.category_health_plan_title,
        )
        JourneyCategory.LongTermProjects -> JourneyCategoryExperienceResources(
            displayName = Res.string.category_long_term_title,
            description = Res.string.category_long_term_description,
            dreamPrompt = Res.string.category_long_term_dream_prompt,
            outcome = Res.string.category_long_term_outcome,
            questionsIntro = Res.string.category_long_term_questions_intro,
            aiActionLabel = Res.string.category_long_term_ai_action,
            planReviewTitle = Res.string.category_long_term_plan_title,
        )
    }
