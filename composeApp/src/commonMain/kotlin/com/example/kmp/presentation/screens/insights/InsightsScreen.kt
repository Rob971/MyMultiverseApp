package com.example.kmp.presentation.screens.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmp.domain.model.Journey
import com.example.kmp.presentation.components.NapolitanBackground
import com.example.kmp.presentation.theme.SharedJourneyColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class InsightsScreen(val journey: Journey) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        NapolitanBackground {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(Res.string.insights_title), fontWeight = FontWeight.Black) },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.action_back))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = SharedJourneyColors.MediterraneanTeal,
                            navigationIconContentColor = SharedJourneyColors.MediterraneanTeal
                        )
                    )
                },
                containerColor = Color.Transparent
            ) { padding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    item {
                        Text(
                            text = journey.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = SharedJourneyColors.InkDeep,
                            fontWeight = FontWeight.Black
                        )
                    }

                    when (journey.id) {
                        "vesuvian-vitality" -> {
                            item { InsightCard("Il Piatto 'Quarto, Quarto, Metà'", "Visualizza il tuo nutrimento quotidiano.") { NutritionPlateChart() } }
                            item { InsightCard("Radar di Densità Nutrizionale", "Pomodorini del Piennolo vs. Industriale") { NutrientRadarChart() } }
                            item { InsightCard("Piramide di Idratazione ed Energia", "Equilibrio Mediterraneo") { EnergyPyramidChart() } }
                        }
                        "financial-masterplan" -> {
                            item { InsightCard("Entrate vs. Contributo", "Equità e Trasparenza.") { FinanceStackedBarChart() } }
                            item { InsightCard("Trend di Spesa Mensile", "Costi Fissi vs. Variabili") { FinanceAreaChart() } }
                            item { InsightCard("Progresso Fondi Accantonamento", "Casa, Vacanze, Regali") { SinkingFundPieChart() } }
                        }
                        "motore-unita" -> {
                            item { InsightCard("Costanza vs. Intensità", "Strategia a Lungo Termine") { ConsistencyCurveChart() } }
                            item { InsightCard("Sovraccarico Progressivo", "I Nostri Prossimi Passi") { OverloadStaircaseChart() } }
                            item { InsightCard("Vittorie Non Sulla Bilancia (NSV)", "Web del Benessere") { NsvRadarChart() } }
                        }
                    }
                    
                    item {
                        Text(
                            text = "Questi modelli sono progettati per rafforzare la psicologia della costanza rispetto alla perfezione. Ogni piccola azione costruisce il futuro della nostra famiglia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SharedJourneyColors.InkMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun InsightCard(title: String, subtitle: String, chart: @Composable () -> Unit) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SharedJourneyColors.GlassWhite),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = SharedJourneyColors.InkDeep)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = SharedJourneyColors.InkMuted)
                Spacer(Modifier.height(28.dp))
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    chart()
                }
            }
        }
    }
}

// --- NUTRITION CHARTS ---

@Composable
fun NutritionPlateChart() {
    Canvas(modifier = Modifier.size(160.dp)) {
        drawArc(Color(0xFF6B9A5E), -90f, 180f, true, size = Size(size.width, size.height)) // 50% Veg
        drawArc(SharedJourneyColors.TerracottaOrange, 90f, 90f, true, size = Size(size.width, size.height)) // 25% Protein
        drawArc(SharedJourneyColors.LemonZestYellow, 180f, 90f, true, size = Size(size.width, size.height)) // 25% Carbs
        drawCircle(Color.White, radius = 4f, center = center)
    }
}

@Composable
fun NutrientRadarChart() {
    Canvas(modifier = Modifier.size(160.dp)) {
        val levels = 5
        val sides = 5
        val radius = size.width / 2
        
        // Background Web
        for (i in 1..levels) {
            val r = radius * (i.toFloat() / levels)
            val path = Path()
            for (j in 0 until sides) {
                val angle = (j * 2 * PI / sides) - PI / 2
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, SharedJourneyColors.SageSoft.copy(alpha = 0.3f), style = Stroke(1f))
        }

        // Piennolo Data (High)
        val pPath = Path()
        val pData = listOf(0.9f, 0.8f, 0.95f, 0.7f, 0.85f)
        pData.forEachIndexed { i, d ->
            val angle = (i * 2 * PI / sides) - PI / 2
            val x = center.x + radius * d * cos(angle).toFloat()
            val y = center.y + radius * d * sin(angle).toFloat()
            if (i == 0) pPath.moveTo(x, y) else pPath.lineTo(x, y)
        }
        pPath.close()
        drawPath(pPath, SharedJourneyColors.TerracottaOrange.copy(alpha = 0.4f))
        drawPath(pPath, SharedJourneyColors.TerracottaOrange, style = Stroke(3f))
    }
}

@Composable
fun EnergyPyramidChart() {
    Canvas(modifier = Modifier.size(180.dp, 160.dp)) {
        val w = size.width
        val h = size.height
        
        // Base
        val base = Path().apply {
            moveTo(0f, h)
            lineTo(w, h)
            lineTo(w * 0.8f, h * 0.6f)
            lineTo(w * 0.2f, h * 0.6f)
            close()
        }
        drawPath(base, SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.2f))
        
        // Middle
        val mid = Path().apply {
            moveTo(w * 0.2f, h * 0.6f)
            lineTo(w * 0.8f, h * 0.6f)
            lineTo(w * 0.65f, h * 0.3f)
            lineTo(w * 0.35f, h * 0.3f)
            close()
        }
        drawPath(mid, SharedJourneyColors.LemonZestYellow.copy(alpha = 0.6f))
        
        // Apex
        val apex = Path().apply {
            moveTo(w * 0.35f, h * 0.3f)
            lineTo(w * 0.65f, h * 0.3f)
            lineTo(w * 0.5f, 0f)
            close()
        }
        drawPath(apex, SharedJourneyColors.TerracottaOrange)
    }
}

// --- FINANCE CHARTS ---

@Composable
fun FinanceStackedBarChart() {
    Canvas(modifier = Modifier.size(200.dp, 160.dp)) {
        val barW = 40.dp.toPx()
        // Income Bar
        drawRect(SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.3f), Offset(40f, 40f), Size(barW, 120f))
        drawRect(SharedJourneyColors.TerracottaOrange.copy(alpha = 0.3f), Offset(40f, 160f), Size(barW, 80f))
        
        // arrow
        drawLine(SharedJourneyColors.InkMuted, Offset(40f + barW + 10f, 120f), Offset(40f + barW + 40f, 120f), strokeWidth = 2f)
        
        // Contribution Bar
        drawRect(SharedJourneyColors.MediterraneanTeal, Offset(140f, 60f), Size(barW, 90f))
        drawRect(SharedJourneyColors.TerracottaOrange, Offset(140f, 150f), Size(barW, 60f))
    }
}

@Composable
fun FinanceAreaChart() {
    Canvas(modifier = Modifier.size(240.dp, 160.dp)) {
        val path = Path().apply {
            moveTo(0f, size.height)
            quadraticTo(size.width * 0.25f, size.height * 0.4f, size.width * 0.5f, size.height * 0.6f)
            quadraticTo(size.width * 0.75f, size.height * 0.2f, size.width, size.height * 0.5f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path, SharedJourneyColors.TerracottaOrange.copy(alpha = 0.2f))
        drawPath(path, SharedJourneyColors.TerracottaOrange, style = Stroke(4f))
        
        val path2 = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, size.height * 0.8f)
            quadraticTo(size.width * 0.3f, size.height * 0.7f, size.width * 0.6f, size.height * 0.85f)
            lineTo(size.width, size.height * 0.75f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path2, SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.3f))
    }
}

@Composable
fun SinkingFundPieChart() {
    Canvas(modifier = Modifier.size(160.dp)) {
        drawArc(SharedJourneyColors.TerracottaOrange, -90f, 120f, true)
        drawArc(SharedJourneyColors.MediterraneanTeal, 30f, 80f, true)
        drawArc(SharedJourneyColors.LemonZestYellow, 110f, 160f, true)
    }
}

// --- FITNESS CHARTS ---

@Composable
fun ConsistencyCurveChart() {
    Canvas(modifier = Modifier.size(200.dp, 160.dp)) {
        // Flat consistency line
        drawLine(SharedJourneyColors.MediterraneanTeal, Offset(0f, size.height * 0.7f), Offset(size.width, size.height * 0.7f), strokeWidth = 8f)
        // Spiky intensity peaks
        val path = Path().apply {
            moveTo(20f, size.height * 0.7f)
            lineTo(40f, 20f)
            lineTo(60f, size.height * 0.7f)
            moveTo(120f, size.height * 0.7f)
            lineTo(140f, 40f)
            lineTo(160f, size.height * 0.7f)
        }
        drawPath(path, SharedJourneyColors.TerracottaOrange, style = Stroke(2f))
    }
}

@Composable
fun OverloadStaircaseChart() {
    Canvas(modifier = Modifier.size(200.dp, 160.dp)) {
        val steps = 5
        val stepW = size.width / steps
        val stepH = size.height / steps
        for (i in 0 until steps) {
            drawRect(
                color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.2f + (i * 0.15f)),
                topLeft = Offset(i * stepW, size.height - (i + 1) * stepH),
                size = Size(stepW - 4f, (i + 1) * stepH)
            )
        }
    }
}

@Composable
fun NsvRadarChart() {
    Canvas(modifier = Modifier.size(160.dp)) {
        val levels = 5
        val sides = 5
        val radius = size.width / 2
        
        // Background Web
        for (i in 1..levels) {
            val r = radius * (i.toFloat() / levels)
            val path = Path()
            for (j in 0 until sides) {
                val angle = (j * 2 * PI / sides) - PI / 2
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f), style = Stroke(1f))
        }

        // NSV Data
        val pPath = Path()
        val pData = listOf(0.8f, 0.9f, 0.6f, 0.85f, 0.75f)
        pData.forEachIndexed { i, d ->
            val angle = (i * 2 * PI / sides) - PI / 2
            val x = center.x + radius * d * cos(angle).toFloat()
            val y = center.y + radius * d * sin(angle).toFloat()
            if (i == 0) pPath.moveTo(x, y) else pPath.lineTo(x, y)
        }
        pPath.close()
        drawPath(pPath, SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.4f))
        drawPath(pPath, SharedJourneyColors.MediterraneanTeal, style = Stroke(3f))
    }
}
