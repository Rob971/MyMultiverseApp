package com.example.kmp.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
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
    var scheduledDays by remember { mutableStateOf(task?.scheduledDays ?: emptyList<Int>()) }
    var reminderTime by remember { mutableStateOf(task?.reminderTime ?: "") }

    val daysOfWeek = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SharedJourneyColors.SunDrenchedWhite,
        title = {
            Text(
                if (task == null) stringResource(Res.string.task_edit_title_add) else stringResource(Res.string.task_edit_title_edit),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = SharedJourneyColors.MediterraneanTeal
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(Res.string.task_edit_field_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = planning,
                    onValueChange = { planning = it },
                    label = { Text(stringResource(Res.string.task_edit_field_planning)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    "Giorni Programmati",
                    style = MaterialTheme.typography.labelLarge,
                    color = SharedJourneyColors.InkDeep,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEachIndexed { index, day ->
                        val dayNum = index + 1
                        val isSelected = scheduledDays.contains(dayNum)

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                scheduledDays = if (isSelected) {
                                    scheduledDays.filter { it != dayNum }
                                } else {
                                    (scheduledDays + dayNum).sorted()
                                }
                            },
                            label = { Text(day.first().toString()) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SharedJourneyColors.MediterraneanTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text(stringResource(Res.string.task_edit_field_reminder)) },
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
                        planning = planning,
                        scheduledDays = scheduledDays,
                        reminderTime = if (reminderTime.isBlank()) null else reminderTime
                    )
                    onConfirm(newTask)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal)
            ) {
                Text(stringResource(Res.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel), color = SharedJourneyColors.InkMuted)
            }
        }
    )
}
