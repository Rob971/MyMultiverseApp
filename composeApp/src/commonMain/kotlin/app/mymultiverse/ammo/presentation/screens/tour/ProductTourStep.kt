package app.mymultiverse.ammo.presentation.screens.tour

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
