package app.mymultiverse.kmp.presentation.components

import androidx.compose.runtime.Composable
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_friday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_monday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_saturday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_sunday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_thursday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_tuesday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_wednesday
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun nutritionDayLabel(dayIndex: Int): String {
    val labels = nutritionDayLabelResources()
    return stringResource(labels[dayIndex.coerceIn(0, labels.lastIndex)])
}

fun nutritionDayLabelResources(): List<StringResource> = listOf(
    Res.string.nutrition_day_monday,
    Res.string.nutrition_day_tuesday,
    Res.string.nutrition_day_wednesday,
    Res.string.nutrition_day_thursday,
    Res.string.nutrition_day_friday,
    Res.string.nutrition_day_saturday,
    Res.string.nutrition_day_sunday,
)
