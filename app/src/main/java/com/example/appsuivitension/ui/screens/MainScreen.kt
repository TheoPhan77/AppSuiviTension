package com.example.appsuivitension.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsuivitension.model.BloodPressureRecord
import com.example.appsuivitension.utils.ExportUtils
import com.example.appsuivitension.utils.SettingsManager
import com.example.appsuivitension.utils.ThemeMode
import com.example.appsuivitension.viewmodel.BloodPressureViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: BloodPressureViewModel = viewModel(),
    onThemeChange: (ThemeMode) -> Unit = {},
    onReminderChange: () -> Unit = {}
) {
    val records by viewModel.records.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<BloodPressureRecord?>(null) }
    var selectedRange by remember { mutableIntStateOf(7) }
    var alertData by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    var selectedRecordForDetails by remember { mutableStateOf<BloodPressureRecord?>(null) }

    val context = LocalContext.current
    val dateFormatShort = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    val filteredRecords = remember(records, selectedRange) {
        val cutoff = System.currentTimeMillis() - (selectedRange.toLong() * 24 * 60 * 60 * 1000)
        records.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MON SUIVI TENSION", fontWeight = FontWeight.Black, fontSize = 22.sp) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres", modifier = Modifier.size(30.dp))
                    }
                    IconButton(onClick = { ExportUtils.exportToCsv(context, records) }) {
                        Icon(Icons.Default.Share, contentDescription = "Exporter", modifier = Modifier.size(30.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .size(80.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter", modifier = Modifier.size(48.dp))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            // Filtre de période
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(7, 30, 90).forEach { range ->
                    FilterButton(
                        text = "${range}j",
                        isSelected = selectedRange == range,
                        onClick = { selectedRange = range },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(250.dp).padding(horizontal = 16.dp)) {
                if (filteredRecords.isNotEmpty()) {
                    TensionChart(filteredRecords, dateFormatShort) { record ->
                        selectedRecordForDetails = record
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucune mesure enregistrée", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Text(
                "Historique",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = records, key = { it.id }) { record ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteRecord(record.id)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                                    else -> Color.Red
                                }
                            )
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.White)
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        RecordItem(
                            record = record,
                            onDelete = { viewModel.deleteRecord(record.id) },
                            onLongClick = { recordToEdit = record }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddRecordDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { sys, dia, pulse, notes ->
                    viewModel.addRecord(sys, dia, pulse, notes)
                    val alert = getAlertMessage(sys, dia, pulse)
                    if (alert != null) {
                        alertData = alert
                        triggerVibration(context, alert.first == "CRITIQUE")
                    }
                    showAddDialog = false
                }
            )
        }

        if (recordToEdit != null) {
            EditRecordDialog(
                record = recordToEdit!!,
                onDismiss = { recordToEdit = null },
                onConfirm = { updatedRecord ->
                    viewModel.updateRecord(updatedRecord)
                    recordToEdit = null
                }
            )
        }

        if (showSettings) {
            SettingsDialog(
                onDismiss = { showSettings = false },
                onThemeChange = onThemeChange,
                onReminderChange = onReminderChange
            )
        }

        if (selectedRecordForDetails != null) {
            DetailsDialog(
                record = selectedRecordForDetails!!,
                onDismiss = { selectedRecordForDetails = null }
            )
        }

        alertData?.let { (title, message) ->
            AlertDialog(
                onDismissRequest = { alertData = null },
                icon = { if (title == "CRITIQUE") Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp)) },
                title = { Text(title, fontWeight = FontWeight.Bold, color = if (title == "CRITIQUE") Color.Red else Color(0xFFE65100)) },
                text = { Text(message, fontSize = 18.sp) },
                confirmButton = {
                    Button(onClick = { alertData = null }, modifier = Modifier.fillMaxWidth()) { Text("OK") }
                }
            )
        }
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun TensionChart(
    records: List<BloodPressureRecord>,
    dateFormat: SimpleDateFormat,
    onPointClick: (BloodPressureRecord) -> Unit
) {
    // On utilise rememberUpdatedState pour garantir que le listener a toujours les dernières données
    val currentRecords by rememberUpdatedState(records)
    val currentOnPointClick by rememberUpdatedState(onPointClick)

    val contentColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    AndroidView(
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false) // Désactivé pour prioriser le clic sur les points
                setDoubleTapToZoomEnabled(false)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textColor = contentColor
                }

                axisRight.isEnabled = false

                axisLeft.apply {
                    setDrawGridLines(true)
                    textColor = contentColor
                    gridColor = outlineColor
                }

                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    textColor = contentColor
                }

                // Augmente la zone de détection pour faciliter le clic sur mobile
                maxHighlightDistance = 40f

                setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                        e?.let {
                            val index = it.x.toInt()
                            if (index >= 0 && index < currentRecords.size) {
                                currentOnPointClick(currentRecords[index])
                            }
                        }
                    }
                    override fun onNothingSelected() {}
                })
            }
        },
        update = { chart ->
            chart.xAxis.textColor = contentColor
            chart.axisLeft.textColor = contentColor
            chart.axisLeft.gridColor = outlineColor
            chart.legend.textColor = contentColor

            val sysEntries = records.mapIndexed { index, r -> Entry(index.toFloat(), r.systolic.toFloat()) }
            val diaEntries = records.mapIndexed { index, r -> Entry(index.toFloat(), r.diastolic.toFloat()) }

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < records.size) {
                        dateFormat.format(Date(records[index].timestamp))
                    } else ""
                }
            }

            val sysDataSet = LineDataSet(sysEntries, "SYS").apply {
                color = android.graphics.Color.RED
                setCircleColor(android.graphics.Color.RED)
                lineWidth = 3f
                setDrawValues(false)
                setCircleRadius(6f)
                setDrawCircleHole(true)
                circleHoleColor = android.graphics.Color.WHITE
                highlightLineWidth = 2f
                highLightColor = android.graphics.Color.LTGRAY
            }
            val diaDataSet = LineDataSet(diaEntries, "DIA").apply {
                color = android.graphics.Color.BLUE
                setCircleColor(android.graphics.Color.BLUE)
                lineWidth = 3f
                setDrawValues(false)
                setCircleRadius(6f)
                setDrawCircleHole(true)
                circleHoleColor = android.graphics.Color.WHITE
                highlightLineWidth = 2f
                highLightColor = android.graphics.Color.LTGRAY
            }

            chart.data = LineData(sysDataSet, diaDataSet)
            chart.highlightValues(null) // Reset highlight on update
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun DetailsDialog(record: BloodPressureRecord, onDismiss: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val alert = getAlertMessage(record.systolic, record.diastolic, record.pulse)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Détails de la mesure", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(dateFormat.format(Date(record.timestamp)), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Tension", style = MaterialTheme.typography.labelMedium)
                        Text("${record.systolic}/${record.diastolic} mmHg", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pouls", style = MaterialTheme.typography.labelMedium)
                        Text("${record.pulse} bpm", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    }
                }

                if (alert != null) {
                    Surface(
                        color = if (alert.first == "CRITIQUE") Color(0xFFFFEBEE) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            alert.second,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (alert.first == "CRITIQUE") Color.Red else Color(0xFFE65100)
                        )
                    }
                }

                if (record.notes.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Notes :", fontWeight = FontWeight.Bold)
                    Text(record.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Fermer") }
        }
    )
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onReminderChange: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    
    var reminderEnabled by remember { mutableStateOf(settingsManager.reminderEnabled) }
    var reminderHour by remember { mutableIntStateOf(settingsManager.reminderHour) }
    var reminderMinute by remember { mutableIntStateOf(settingsManager.reminderMinute) }
    var themeMode by remember { mutableStateOf(settingsManager.themeMode) }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                reminderHour = hour
                reminderMinute = minute
                settingsManager.reminderHour = hour
                settingsManager.reminderMinute = minute
                onReminderChange()
            },
            reminderHour,
            reminderMinute,
            true
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paramètres", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Rappel Quotidien
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Rappel quotidien (${String.format("%02dh%02d", reminderHour, reminderMinute)})",
                            modifier = Modifier.weight(1f),
                            fontSize = 18.sp
                        )
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = {
                                reminderEnabled = it
                                settingsManager.reminderEnabled = it
                                onReminderChange()
                            }
                        )
                    }
                    if (reminderEnabled) {
                        TextButton(onClick = { timePickerDialog.show() }) {
                            Text("Modifier l'heure du rappel")
                        }
                    }
                }

                HorizontalDivider()

                // Thème
                Column {
                    Text("Thème de l'application", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOption(
                            text = "Automatique (Système)",
                            isSelected = themeMode == ThemeMode.SYSTEM,
                            onClick = {
                                themeMode = ThemeMode.SYSTEM
                                onThemeChange(ThemeMode.SYSTEM)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        ThemeOption(
                            text = "Mode Clair",
                            isSelected = themeMode == ThemeMode.LIGHT,
                            onClick = {
                                themeMode = ThemeMode.LIGHT
                                onThemeChange(ThemeMode.LIGHT)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        ThemeOption(
                            text = "Mode Sombre",
                            isSelected = themeMode == ThemeMode.DARK,
                            onClick = {
                                themeMode = ThemeMode.DARK
                                onThemeChange(ThemeMode.DARK)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider()

                Text("L'export génère un fichier CSV que vous pouvez envoyer par email à votre médecin.", fontSize = 14.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Fermer") }
        }
    )
}

@Composable
fun ThemeOption(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(selected = isSelected, onClick = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 18.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

fun getAlertMessage(sys: Int, dia: Int, pulse: Int): Pair<String, String>? {
    return when {
        sys > 180 || dia > 120 ->
            "CRITIQUE" to "Votre tension est très élevée ($sys/$dia). Veuillez contacter les urgences ou votre médecin immédiatement."
        pulse < 50 || pulse > 120 ->
            "ALERTE POULS" to "Votre rythme cardiaque ($pulse bpm) est anormal. Reposez-vous et reprenez la mesure plus tard."
        else -> null
    }
}

fun triggerVibration(context: Context, isCritical: Boolean) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = if (isCritical) longArrayOf(0, 500, 200, 500) else longArrayOf(0, 300)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(if (isCritical) 500L else 300L)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordItem(
    record: BloodPressureRecord,
    onDelete: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(record.timestamp))

    val isHighPressure = record.systolic > 180 || record.diastolic > 120
    val isPulseAlert = record.pulse < 50 || record.pulse > 120

    val pulseColor = if (isPulseAlert) {
        Color(0xFFE65100)
    } else if (isSystemInDarkTheme() || MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
        Color.LightGray
    } else {
        Color.DarkGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onLongClick
            )
            .border(
                width = 2.dp,
                color = if (isHighPressure || isPulseAlert) {
                    Color.Red.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(12.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isHighPressure -> Color(0xFFFFEBEE)
                isPulseAlert -> Color(0xFFFFF3E0)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = dateString, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${record.systolic}/${record.diastolic}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isHighPressure) Color.Red else Color.Unspecified
                    )
                    Text(" mmHg", fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
                Text(
                    text = "❤ ${record.pulse} bpm",
                    fontSize = 20.sp,
                    color = pulseColor
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun EditRecordDialog(
    record: BloodPressureRecord,
    onDismiss: () -> Unit,
    onConfirm: (BloodPressureRecord) -> Unit
) {
    var systolic by remember { mutableStateOf(record.systolic.toString()) }
    var diastolic by remember { mutableStateOf(record.diastolic.toString()) }
    var pulse by remember { mutableStateOf(record.pulse.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la mesure") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = systolic, onValueChange = { systolic = it }, label = { Text("Systolique") })
                OutlinedTextField(value = diastolic, onValueChange = { diastolic = it }, label = { Text("Diastolique") })
                OutlinedTextField(value = pulse, onValueChange = { pulse = it }, label = { Text("Pouls") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(record.copy(
                    systolic = systolic.toIntOrNull() ?: record.systolic,
                    diastolic = diastolic.toIntOrNull() ?: record.diastolic,
                    pulse = pulse.toIntOrNull() ?: record.pulse
                ))
            }) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
