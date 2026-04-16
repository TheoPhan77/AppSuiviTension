package com.example.appsuivitension.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.appsuivitension.model.BloodPressureRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BloodPressureRepository(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("blood_pressure_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "records"

    fun getAllRecords(userId: String): List<BloodPressureRecord> {
        val allRecords = getAll()
        return allRecords.filter { it.userId == userId }
    }

    private fun getAll(): List<BloodPressureRecord> {
        val json = sharedPreferences.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<BloodPressureRecord>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun addRecord(record: BloodPressureRecord) {
        val records = getAll().toMutableList()
        records.add(record)
        saveRecords(records)
    }

    fun deleteRecord(recordId: String) {
        val records = getAll().filter { it.id != recordId }
        saveRecords(records)
    }

    fun updateRecord(updatedRecord: BloodPressureRecord) {
        val records = getAll().toMutableList()
        val index = records.indexOfFirst { it.id == updatedRecord.id }
        if (index != -1) {
            records[index] = updatedRecord
            saveRecords(records)
        }
    }

    private fun saveRecords(records: List<BloodPressureRecord>) {
        val json = gson.toJson(records)
        sharedPreferences.edit {
            putString(key, json)
        }
    }
}
