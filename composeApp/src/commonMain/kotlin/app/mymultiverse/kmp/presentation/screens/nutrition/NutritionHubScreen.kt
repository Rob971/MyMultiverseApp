package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_hub_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_hub_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_title
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.presentation.components.FamilyLogisticCard
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

@Composable
fun NutritionHubScreen(
    onBack: () -> Unit,
    onOpenSection: (NutritionSection) -> Unit,
) {
    NutritionScaffold(
        title = stringResource(Res.string.nutrition_hub_title),
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.nutrition_hub_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SharedJourneyColors.InkMuted,
                    fontWeight = FontWeight.Medium,
                )
            }
            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.nutrition_grocery_title),
                    description = stringResource(Res.string.nutrition_grocery_description),
                    accentColor = SharedJourneyColors.SageSoft,
                    icon = AppIcons.Restaurant,
                    onClick = { onOpenSection(NutritionSection.Grocery) },
                )
            }
            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.nutrition_meal_plan_title),
                    description = stringResource(Res.string.nutrition_meal_plan_description),
                    accentColor = SharedJourneyColors.TerracottaOrange,
                    icon = AppIcons.DateRange,
                    onClick = { onOpenSection(NutritionSection.MealPlan) },
                )
            }
            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.nutrition_ai_title),
                    description = stringResource(Res.string.nutrition_ai_description),
                    accentColor = SharedJourneyColors.MediterraneanTeal,
                    icon = AppIcons.Sparkles,
                    onClick = { onOpenSection(NutritionSection.AiAdvice) },
                )
            }
        }
    }
}
