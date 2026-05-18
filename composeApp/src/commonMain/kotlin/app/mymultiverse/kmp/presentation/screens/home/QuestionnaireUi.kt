package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class LocalizedSelectOption(
    val storedValue: String,
    val label: StringResource,
)

@Composable
fun LocalizedOptionChips(
    options: List<LocalizedSelectOption>,
    selectedValue: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = selectedValue == option.storedValue,
                onClick = { onSelected(option.storedValue) },
                label = { Text(stringResource(option.label)) },
            )
        }
    }
}

@Composable
fun LocalizedQuestionBlock(
    title: StringResource,
    description: StringResource,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(title),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = SharedJourneyColors.InkDeep,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(description),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = SharedJourneyColors.InkMuted,
        )
        content()
    }
}

@Composable
fun LocalizedPartySizeChips(
    options: List<LocalizedSelectOption>,
    selectedValue: String,
    onSelected: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = selectedValue == option.storedValue,
                onClick = { onSelected(option.storedValue) },
                label = { Text(stringResource(option.label)) },
            )
        }
    }
}
