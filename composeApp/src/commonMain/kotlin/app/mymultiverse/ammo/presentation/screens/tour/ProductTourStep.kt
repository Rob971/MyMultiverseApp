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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * A single step in the product tour.
 *
 * @param id Stable identifier used to register spotlight coordinates via
 *   [Modifier.productTourTarget].
 * @param title Localised title shown in the tooltip card.
 * @param description Localised body text shown in the tooltip card.
 * @param targetTag The composable element to spotlight, matched by the same string passed to
 *   [Modifier.productTourTarget]. When null the overlay shows a centred modal without a cutout.
 * @param illustrationRes Optional drawable shown above the title in the tooltip card.
 */
data class ProductTourStep(
    val id: String,
    val title: StringResource,
    val description: StringResource,
    val targetTag: String? = null,
    val illustrationRes: DrawableResource? = null,
)

fun productTourSteps(): List<ProductTourStep> = listOf(
    ProductTourStep(
        id = "welcome",
        title = Res.string.tour_step_welcome_title,
        description = Res.string.tour_step_welcome_body,
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
