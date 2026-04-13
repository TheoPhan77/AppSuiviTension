package com.example.appsuivitension.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.appsuivitension.model.BloodPressureRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {
    fun exportToCsv(context: Context, records: List<BloodPressureRecord>) {
        val fileName = "suivi_tension_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        try {
            file.printWriter().use { out ->
                out.println("Date;Systolique;Diastolique;Pouls;Notes")
                records.forEach { record ->
                    val dateStr = dateFormat.format(Date(record.timestamp))
                    out.println("${dateStr};${record.systolic};${record.diastolic};${record.pulse};${record.notes}")
                }
            }
            
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Envoyer le rapport"))
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
