package com.example.kmp.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kmp.domain.model.JourneyTask
import com.example.kmp.presentation.theme.SharedJourneyColors
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditDialog(
    journeyId: String,
    task: JourneyTask? = null,
    onDismiss: () -> Unit,
    onConfirm: (JourneyTask) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var planning by remember { mutableStateOf(task?.planning ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SharedJourneyColors.SunDrenchedWhite,
        title = {
            Text(
                if (task == null) "Aggiungi Attività" else "Modifica Attività",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = SharedJourneyColors.MediterraneanTeal
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titolo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = planning,
                    onValueChange = { planning = it },
                    label = { Text("Pianificazione (es: Mercato ogni sabato)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTask = (task ?: JourneyTask(
                        id = Clock.System.now().toEpochMilliseconds().toString(),
                        journeyId = journeyId,
                        title = "",
                        planning = "",
                        isCompleted = false,
                        label = "Custom"
                    )).copy(
                        title = title,
                        planning = planning
                    )
                    onConfirm(newTask)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = SharedJourneyColors.InkMuted)
            }
        }
    )
}
