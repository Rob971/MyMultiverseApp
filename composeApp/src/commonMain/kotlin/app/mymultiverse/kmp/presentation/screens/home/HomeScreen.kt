package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.presentation.components.FamilyLogisticCard
import app.mymultiverse.kmp.presentation.components.JourneyBanner
import app.mymultiverse.kmp.presentation.components.LanguagePicker
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    onOpenNutrition: () -> Unit,
) {
    val screenModel = koinInject<HomeScreenModel>()
    val greeting by screenModel.greeting.collectAsState()
    val isRefreshing by screenModel.isRefreshing.collectAsState()

    HomeContent(
        greeting = greeting,
        isRefreshing = isRefreshing,
        onRefreshClick = { screenModel.refresh() },
        onOpenNutrition = onOpenNutrition,
    )
}

@Composable
fun HomeContent(
    greeting: Greeting?,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onOpenNutrition: () -> Unit,
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    LanguagePicker()
                },
            )
        },
        containerColor = Color.Transparent,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .safeDrawingPadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                JourneyBanner(
                    headline = stringResource(Res.string.home_banner_headline),
                    supportingLine =
                        when (greeting) {
                            null -> stringResource(Res.string.home_banner_loading)
                            else -> stringResource(Res.string.home_greeting)
                        },
                    description = stringResource(Res.string.home_banner_description)
                )
            }

            item {
                if (greeting == null || isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = SharedJourneyColors.MediterraneanTeal,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.home_dreams_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = SharedJourneyColors.InkDeep,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.home_logistics_nutrition_title),
                    description = stringResource(Res.string.home_logistics_nutrition_description),
                    accentColor = SharedJourneyColors.SageSoft,
                    icon = AppIcons.Restaurant,
                    onClick = onOpenNutrition,
                )
            }

            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.home_logistics_adventures_title),
                    description = stringResource(Res.string.home_logistics_adventures_description),
                    accentColor = SharedJourneyColors.TerracottaOrange,
                    icon = AppIcons.Explore,
                    onClick = {},
                )
            }

            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.home_logistics_budget_title),
                    description = stringResource(Res.string.home_logistics_budget_description),
                    accentColor = SharedJourneyColors.MediterraneanTeal,
                    icon = AppIcons.AccountBalance,
                    onClick = {},
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = onRefreshClick,
                        enabled = !isRefreshing,
                    ) {
                        Text(
                            stringResource(Res.string.home_refresh_inspirations),
                            style = MaterialTheme.typography.labelMedium,
                            color = SharedJourneyColors.InkMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
