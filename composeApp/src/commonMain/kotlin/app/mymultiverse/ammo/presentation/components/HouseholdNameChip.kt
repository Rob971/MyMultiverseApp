package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
import org.jetbrains.compose.resources.stringResource
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.home_household_name_edit

object HouseholdNameChipTestTags {
    const val CHIP = "home_household_name_chip"
    const val EDIT = "home_household_name_edit"
}

@Composable
fun HouseholdNameChip(
    name: String,
    canEdit: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.testTag(HouseholdNameChipTestTags.CHIP),
        shape = RoundedCornerShape(20.dp),
        color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.14f),
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (canEdit) {
                        Modifier.clickable(onClick = onEditClick)
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SharedJourneyColors.MediterraneanTeal,
            )
            if (canEdit) {
                Text(
                    text = stringResource(Res.string.home_household_name_edit),
                    style = MaterialTheme.typography.labelMedium,
                    color = SharedJourneyColors.MediterraneanTeal,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .testTag(HouseholdNameChipTestTags.EDIT),
                )
            }
        }
    }
}
