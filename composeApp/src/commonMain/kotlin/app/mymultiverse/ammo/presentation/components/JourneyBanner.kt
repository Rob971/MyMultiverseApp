package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors

/**
 * 'Napolitan Heart' Banner: Ammò round disc logo and vibrant gradients.
 */
@Composable
fun JourneyBanner(
    headline: String,
    supportingLine: String?,
    description: String? = null,
    headlineTestTag: String? = null,
    supportingLineTestTag: String? = null,
    descriptionTestTag: String? = null,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(32.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = shape,
        shadowElevation = 0.dp,
        color = JourneySemanticColors.bannerSurface(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SharedJourneyColors.TerracottaOrange.copy(alpha = 0.1f),
                            SharedJourneyColors.LemonZestYellow.copy(alpha = 0.05f),
                        ),
                    ),
                )
                .padding(28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AmmoRoundLogo(modifier = Modifier.size(64.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = JourneySemanticColors.onBannerHeadline(),
                        textAlign = TextAlign.Center,
                        modifier = headlineTestTag?.let { Modifier.testTag(it) } ?: Modifier,
                    )
                    description?.let { text ->
                        if (text.isNotEmpty()) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = JourneySemanticColors.onBannerDescription(),
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .then(
                                        descriptionTestTag?.let { Modifier.testTag(it) } ?: Modifier,
                                    ),
                            )
                        }
                    }
                    supportingLine?.let { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.labelMedium,
                            color = SharedJourneyColors.TerracottaOrange,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .then(
                                    supportingLineTestTag?.let { Modifier.testTag(it) } ?: Modifier,
                                ),
                        )
                    }
                }
            }
        }
    }
}
