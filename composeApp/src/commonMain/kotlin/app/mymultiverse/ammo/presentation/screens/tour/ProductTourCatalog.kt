package app.mymultiverse.ammo.presentation.screens.tour

import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.tour_step_grocery_body
import ammo.composeapp.generated.resources.tour_step_grocery_title
import ammo.composeapp.generated.resources.tour_step_home_body
import ammo.composeapp.generated.resources.tour_step_home_title
import ammo.composeapp.generated.resources.tour_step_meal_plan_body
import ammo.composeapp.generated.resources.tour_step_meal_plan_title
import ammo.composeapp.generated.resources.tour_step_welcome_body
import ammo.composeapp.generated.resources.tour_step_welcome_title

/**
 * Canonical product-tour steps shown after the authenticated Home shell is ready.
 *
 * Kept in one place so [App] and instrumented smoke tests share the same step tags
 * and copy resources (composeApp `Res` is not visible outside this module).
 *
 * Persistence uses [TOUR_ID] (not [AppBuildInfo.VERSION_NAME]) so a new user completes
 * the walkthrough **once** across alpha/beta/prod builds of the same tour content.
 * Bump [TOUR_ID] only when intentionally re-introducing the tour for all users.
 */
object ProductTourCatalog {

    /**
     * Stable persistence key for [ProductTourStore].
     * Independent of CI prerelease suffixes (`1.6.5-alpha.N`) which would otherwise
     * re-show the tour on every alpha distribute.
     */
    const val TOUR_ID = "spotlight_v1"

    const val STEP_COUNT = 4

    fun defaultSteps(): List<ProductTourStep> = listOf(
        ProductTourStep(
            id = "welcome",
            title = Res.string.tour_step_welcome_title,
            description = Res.string.tour_step_welcome_body,
            targetTag = null,
        ),
        ProductTourStep(
            id = "home_hub",
            title = Res.string.tour_step_home_title,
            description = Res.string.tour_step_home_body,
            targetTag = ProductTourTestTags.TARGET_HOME_HUB,
        ),
        ProductTourStep(
            id = "meal_plan_tab",
            title = Res.string.tour_step_meal_plan_title,
            description = Res.string.tour_step_meal_plan_body,
            targetTag = ProductTourTestTags.TARGET_MEAL_PLAN_TAB,
        ),
        ProductTourStep(
            id = "grocery_tab",
            title = Res.string.tour_step_grocery_title,
            description = Res.string.tour_step_grocery_body,
            targetTag = ProductTourTestTags.TARGET_GROCERY_TAB,
        ),
    )
}
