package com.example.kmp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kmp.presentation.theme.SharedJourneyColors

@Composable
fun JourneyBanner(
    headline: String,
    supportingLine: String?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    SharedJourneyColors.Terracotta.copy(alpha = 0.18f),
                                    SharedJourneyColors.WarmBeige.copy(alpha = 0.9f),
                                    SharedJourneyColors.Sage.copy(alpha = 0.12f),
                                ),
                        ),
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "👨‍👩‍👧",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp),
                )
                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineMedium,
                    color = SharedJourneyColors.InkBrown,
                    modifier = Modifier.weight(1f),
                )
            }
            supportingLine?.let { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SharedJourneyColors.InkMuted,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
