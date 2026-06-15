package app.mymultiverse.kmp.presentation.screens.nutrition.spaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.SharingSpace
import app.mymultiverse.kmp.domain.sharing.sortedFeatures
import app.mymultiverse.kmp.presentation.components.FamilyLogisticCard
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.screenContentArea
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.navigation.NutritionSpaceContext
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_create_button
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_create_cancel
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_create_confirm
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_create_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_empty
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_error_features_required
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_error_generic
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_error_name_required
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_error_not_configured
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_feature_ai_advice
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_feature_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_feature_meal_plan
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_features_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_name_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_name_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_nutrition_spaces_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object NutritionSpacesTestTags {
    const val CREATE_BUTTON = "nutrition_spaces_create_button"
    const val SPACE_CARD = "nutrition_spaces_card"
    const val CREATE_DIALOG = "nutrition_spaces_create_dialog"
}

@Composable
fun NutritionSpacesScreen(
    onBack: () -> Unit,
    onSpaceSelected: (NutritionSpaceContext) -> Unit,
    screenModel: NutritionSpacesScreenModel = koinInject(),
) {
    val uiState by screenModel.uiState.collectAsState()
    val errorMessage = uiState.error?.let { error ->
        when (error) {
            NutritionSpacesError.Generic -> stringResource(Res.string.sharing_nutrition_spaces_error_generic)
            NutritionSpacesError.NameRequired -> stringResource(Res.string.sharing_nutrition_spaces_error_name_required)
            NutritionSpacesError.FeaturesRequired -> stringResource(Res.string.sharing_nutrition_spaces_error_features_required)
            NutritionSpacesError.NotConfigured -> stringResource(Res.string.sharing_nutrition_spaces_error_not_configured)
        }
    }

    NutritionScaffold(
        title = stringResource(Res.string.sharing_nutrition_spaces_title),
        subtitle = stringResource(Res.string.sharing_nutrition_spaces_subtitle),
        onBack = onBack,
    ) { padding ->
        when {
            uiState.isLoading && uiState.spaces.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .screenContentArea(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(Res.string.sharing_nutrition_spaces_loading),
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .screenContentArea(padding),
                    contentPadding = screenListPadding(),
                    verticalArrangement = Arrangement.spacedBy(ScreenLayout.sectionSpacing),
                ) {
                    item {
                        FamilyLogisticsSectionHeader(
                            title = stringResource(Res.string.sharing_nutrition_spaces_create_button),
                        )
                    }
                    item {
                        Button(
                            onClick = screenModel::openCreateDialog,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(NutritionSpacesTestTags.CREATE_BUTTON),
                        ) {
                            Text(stringResource(Res.string.sharing_nutrition_spaces_create_button))
                        }
                    }

                    if (uiState.spaces.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(Res.string.sharing_nutrition_spaces_empty),
                                color = SharedJourneyColors.InkDeep.copy(alpha = 0.75f),
                            )
                        }
                    } else {
                        items(uiState.spaces, key = { it.id }) { space ->
                            SharingSpaceCard(
                                space = space,
                                onClick = {
                                    screenModel.selectSpace(space) {
                                        onSpaceSelected(it.toContext())
                                    }
                                },
                            )
                        }
                    }

                    if (errorMessage != null) {
                        item {
                            Text(
                                text = errorMessage,
                                color = SharedJourneyColors.TerracottaOrange,
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateNutritionSpaceDialog(
            draft = uiState.createDraft,
            isCreating = uiState.isCreating,
            errorMessage = errorMessage,
            onNameChange = screenModel::onCreateNameChange,
            onFeatureToggle = screenModel::onCreateFeatureToggle,
            onDismiss = screenModel::dismissCreateDialog,
            onConfirm = {
                screenModel.submitCreateSpace { space ->
                    onSpaceSelected(space.toContext())
                }
            },
        )
    }
}

@Composable
private fun SharingSpaceCard(
    space: SharingSpace,
    onClick: () -> Unit,
) {
    val featureLabels = space.features.sortedFeatures().map { feature ->
        when (feature) {
            NutritionSharingFeature.Grocery -> stringResource(Res.string.sharing_nutrition_spaces_feature_grocery)
            NutritionSharingFeature.MealPlan -> stringResource(Res.string.sharing_nutrition_spaces_feature_meal_plan)
            NutritionSharingFeature.AiAdvice -> stringResource(Res.string.sharing_nutrition_spaces_feature_ai_advice)
        }
    }
    val featureSummary = featureLabels.joinToString(separator = " · ")

    FamilyLogisticCard(
        title = space.name,
        description = featureSummary,
        icon = AppIcons.Restaurant,
        accentColor = SharedJourneyColors.SageSoft,
        onClick = onClick,
        modifier = Modifier.testTag("${NutritionSpacesTestTags.SPACE_CARD}_${space.id}"),
    )
}

@Composable
private fun CreateNutritionSpaceDialog(
    draft: CreateNutritionSpaceDraft,
    isCreating: Boolean,
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onFeatureToggle: (NutritionSharingFeature, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(NutritionSpacesTestTags.CREATE_DIALOG),
        title = { Text(stringResource(Res.string.sharing_nutrition_spaces_create_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(Res.string.sharing_nutrition_spaces_name_label)) },
                    placeholder = { Text(stringResource(Res.string.sharing_nutrition_spaces_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(Res.string.sharing_nutrition_spaces_features_title))
                FeatureToggleRow(
                    label = stringResource(Res.string.sharing_nutrition_spaces_feature_grocery),
                    checked = draft.groceryEnabled,
                    onCheckedChange = { onFeatureToggle(NutritionSharingFeature.Grocery, it) },
                )
                FeatureToggleRow(
                    label = stringResource(Res.string.sharing_nutrition_spaces_feature_meal_plan),
                    checked = draft.mealPlanEnabled,
                    onCheckedChange = { onFeatureToggle(NutritionSharingFeature.MealPlan, it) },
                )
                FeatureToggleRow(
                    label = stringResource(Res.string.sharing_nutrition_spaces_feature_ai_advice),
                    checked = draft.aiAdviceEnabled,
                    onCheckedChange = { onFeatureToggle(NutritionSharingFeature.AiAdvice, it) },
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = SharedJourneyColors.TerracottaOrange,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isCreating,
            ) {
                if (isCreating) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text(stringResource(Res.string.sharing_nutrition_spaces_create_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCreating) {
                Text(stringResource(Res.string.sharing_nutrition_spaces_create_cancel))
            }
        },
    )
}

@Composable
private fun FeatureToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

private fun SharingSpace.toContext(): NutritionSpaceContext =
    NutritionSpaceContext(
        id = id,
        name = name,
        ownerId = ownerId,
        features = features,
    )
