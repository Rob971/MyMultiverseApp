package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

/**
 * 'Napolitan Heart' Banner: Custom Logo and vibrant gradients.
 */
@Composable
fun JourneyBanner(
    headline: String,
    supportingLine: String?,
    description: String? = null,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(32.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = shape,
        shadowElevation = 0.dp,
        color = SharedJourneyColors.GlassWhite
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
                    )
                )
                .padding(28.dp),
        ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Centered Logo
                    VesuvianHeartLogo(modifier = Modifier.size(64.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = headline,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = SharedJourneyColors.MediterraneanTeal,
                            textAlign = TextAlign.Center
                        )
                        description?.let { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SharedJourneyColors.InkDeep.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        supportingLine?.let { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.labelMedium,
                                color = SharedJourneyColors.TerracottaOrange,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

            }
        }
    }
}
