package com.example.appsuivitension.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (systolic: Int, diastolic: Int, pulse: Int, notes: String) -> Unit
) {
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle mesure", fontSize = 24.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
                
                OutlinedTextField(
                    value = systolic,
                    onValueChange = { if (it.length <= 3) systolic = it },
                    label = { Text("Systolique (70-250)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = systolic.isNotEmpty() && (systolic.toIntOrNull() ?: 0 !in 70..250)
                )
                OutlinedTextField(
                    value = diastolic,
                    onValueChange = { if (it.length <= 3) diastolic = it },
                    label = { Text("Diastolique (40-150)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = diastolic.isNotEmpty() && (diastolic.toIntOrNull() ?: 0 !in 40..150)
                )
                OutlinedTextField(
                    value = pulse,
                    onValueChange = { if (it.length <= 3) pulse = it },
                    label = { Text("Pouls (30-200)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = pulse.isNotEmpty() && (pulse.toIntOrNull() ?: 0 !in 30..200)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 140) notes = it },
                    label = { Text("Notes (Max 140 car.)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${notes.length}/140") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = systolic.toIntOrNull()
                    val d = diastolic.toIntOrNull()
                    val p = pulse.toIntOrNull()
                    
                    if (s == null || d == null || p == null) {
                        error = "Veuillez remplir tous les champs numériques."
                    } else if (s !in 70..250 || d !in 40..150 || p !in 30..200) {
                        error = "Certaines valeurs sont hors des limites autorisées."
                    } else {
                        onConfirm(s, d, p, notes)
                    }
                },
                modifier = Modifier.height(56.dp)
            ) {
                Text("ENREGISTRER", fontSize = 18.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ANNULER", fontSize = 18.sp)
            }
        }
    )
}
