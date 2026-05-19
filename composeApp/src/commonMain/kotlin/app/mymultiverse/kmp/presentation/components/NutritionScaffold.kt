package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.action_back
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val backLabel = stringResource(Res.string.action_back)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    if (subtitle == null) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            color = SharedJourneyColors.InkDeep,
                        )
                    } else {
                        androidx.compose.foundation.layout.Column {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                color = SharedJourneyColors.InkDeep,
                            )
                            Text(
                                text = subtitle,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                color = SharedJourneyColors.InkMuted,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = backLabel,
                            tint = SharedJourneyColors.MediterraneanTeal,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = bottomBar,
        content = content,
    )
}
