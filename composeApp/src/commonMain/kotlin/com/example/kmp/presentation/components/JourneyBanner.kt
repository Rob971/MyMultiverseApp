package com.example.kmp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kmp.presentation.theme.SharedJourneyColors

@Composable
fun JourneyBanner(
    headline: String,
    supportingLine: String?,
    onCalendarClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = shape,
        shadowElevation = 2.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SharedJourneyColors.Terracotta.copy(alpha = 0.15f),
                            SharedJourneyColors.WarmBeige.copy(alpha = 0.8f),
                            SharedJourneyColors.Sage.copy(alpha = 0.1f),
                        ),
                    )
                )
                .padding(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "👨‍👩‍👧",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
                    )
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
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

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onCalendarClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SharedJourneyColors.Terracotta,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Family Calendar", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
