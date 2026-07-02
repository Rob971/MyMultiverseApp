package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_grocery_list
import ammo.composeapp.generated.resources.nutrition_meal_plan
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Shared grocery list and weekly meal plan — raster art for reliable visibility on all devices. */
enum class NutritionFeatureKind {
    MealPlan,
    Grocery,
}

fun NutritionFeatureKind.drawable(): DrawableResource = when (this) {
    NutritionFeatureKind.MealPlan -> Res.drawable.nutrition_meal_plan
    NutritionFeatureKind.Grocery -> Res.drawable.nutrition_grocery_list
}

@Composable
fun NutritionFeatureArt(
    feature: NutritionFeatureKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(feature.drawable()),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
