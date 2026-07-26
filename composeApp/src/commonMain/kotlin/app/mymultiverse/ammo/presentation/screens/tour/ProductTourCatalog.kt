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
 */
object ProductTourCatalog {

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
