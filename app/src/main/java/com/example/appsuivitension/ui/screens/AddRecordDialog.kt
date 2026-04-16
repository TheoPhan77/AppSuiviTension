package com.example.appsuivitension.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

enum class AddStep {
    INTRO, WHEN, VALUES, NOTES, SUMMARY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (systolic: Int, diastolic: Int, pulse: Int, notes: String) -> Unit
) {
    var currentStep by remember { mutableStateOf(AddStep.INTRO) }
    
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    var selectedDate by remember { mutableStateOf(dateFormatter.format(calendar.time)) }
    var selectedTime by remember { mutableStateOf(timeFormatter.format(calendar.time)) }

    val datePickerDialog = remember {
        DatePickerDialog(context, { _, y, m, d ->
            calendar.set(Calendar.YEAR, y)
            calendar.set(Calendar.MONTH, m)
            calendar.set(Calendar.DAY_OF_MONTH, d)
            selectedDate = dateFormatter.format(calendar.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    val timePickerDialog = remember {
        TimePickerDialog(context, { _, h, min ->
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, min)
            selectedTime = timeFormatter.format(calendar.time)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header with Back button and Next/Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep != AddStep.INTRO) {
                        IconButton(onClick = {
                            currentStep = when (currentStep) {
                                AddStep.WHEN -> AddStep.INTRO
                                AddStep.VALUES -> AddStep.WHEN
                                AddStep.NOTES -> AddStep.VALUES
                                AddStep.SUMMARY -> AddStep.NOTES
                                else -> AddStep.INTRO
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    // Progress indicator
                    Text(
                        text = "${currentStep.ordinal + 1} / ${AddStep.values().size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline
                    )

                    if (currentStep != AddStep.SUMMARY && currentStep != AddStep.INTRO) {
                        Button(
                            onClick = {
                                currentStep = when (currentStep) {
                                    AddStep.WHEN -> AddStep.VALUES
                                    AddStep.VALUES -> AddStep.NOTES
                                    AddStep.NOTES -> AddStep.SUMMARY
                                    else -> AddStep.SUMMARY
                                }
                            },
                            enabled = when (currentStep) {
                                AddStep.VALUES -> systolic.isNotEmpty() && diastolic.isNotEmpty() && pulse.isNotEmpty()
                                else -> true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Suivant")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(width = 80.dp, height = 40.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content based on step
                Box(modifier = Modifier.minHeight(200.dp)) {
                    when (currentStep) {
                        AddStep.INTRO -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Vous allez ajouter une nouvelle mesure.",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 32.dp)
                                )
                                Button(
                                    onClick = { currentStep = AddStep.WHEN },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Continuer", fontSize = 18.sp)
                                }
                            }
                        }
                        AddStep.WHEN -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Quand avez-vous fait la mesure ?", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(32.dp))
                                TextButton(onClick = { datePickerDialog.show() }) {
                                    Text(selectedDate, fontSize = 32.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = { timePickerDialog.show() }) {
                                    Text(selectedTime, fontSize = 32.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        AddStep.VALUES -> {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Entrez vos mesures", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                ValueInputRow("Systolique", systolic, "mmHg") { systolic = it }
                                ValueInputRow("Diastolique", diastolic, "mmHg") { diastolic = it }
                                ValueInputRow("Pouls", pulse, "bpm") { pulse = it }
                            }
                        }
                        AddStep.NOTES -> {
                            Column {
                                Text("Avez-vous un commentaire à ajouter ?", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = notes,
                                    onValueChange = { if (it.length <= 140) notes = it },
                                    placeholder = { Text("Ecrivez ici") },
                                    modifier = Modifier.fillMaxWidth().height(150.dp),
                                    supportingText = { Text("${notes.length}/140 caractères") }
                                )
                            }
                        }
                        AddStep.SUMMARY -> {
                            Column {
                                Text("Résumé de la nouvelle mesure", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Le $selectedDate à $selectedTime", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SummaryRow("Systolique", systolic, "mmHg")
                                    SummaryRow("Diastolique", diastolic, "mmHg")
                                    SummaryRow("Pouls", pulse, "bpm")
                                    if (notes.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Notes :", fontWeight = FontWeight.Bold)
                                        Text(notes)
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        val s = systolic.toIntOrNull() ?: 0
                                        val d = diastolic.toIntOrNull() ?: 0
                                        val p = pulse.toIntOrNull() ?: 0
                                        onConfirm(s, d, p, notes)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Valider", fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ValueInputRow(label: String, value: String, unit: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 18.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = value,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) onValueChange(it) },
                modifier = Modifier.width(80.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(unit, fontSize = 16.sp)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text("$value $unit", fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

fun Modifier.minHeight(height: androidx.compose.ui.unit.Dp) = this.defaultMinSize(minHeight = height)
