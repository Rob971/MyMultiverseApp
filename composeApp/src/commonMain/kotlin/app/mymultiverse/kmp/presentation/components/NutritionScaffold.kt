package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.action_back
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.presentation.navigation.NavigationTestTags
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showBackButton: Boolean = true,
    showGlobalLanguageAction: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
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
                            color = JourneySemanticColors.inkDeep(),
                        )
                    } else {
                        androidx.compose.foundation.layout.Column {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                color = JourneySemanticColors.inkDeep(),
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = JourneySemanticColors.inkMuted(),
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (showBackButton) {
                        JourneyIconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag(NavigationTestTags.BACK_BUTTON),
                        ) {
                            Icon(
                                imageVector = AppIcons.ArrowBack,
                                contentDescription = backLabel,
                                tint = JourneySemanticColors.brandTeal(),
                            )
                        }
                    }
                },
                actions = {
                    if (showGlobalLanguageAction) {
                        GlobalLanguageAction()
                    }
                    actions()
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        content = content,
    )
}
