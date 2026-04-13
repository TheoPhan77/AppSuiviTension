package com.example.appsuivitension.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsuivitension.model.BloodPressureRecord
import com.example.appsuivitension.repository.BloodPressureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BloodPressureViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BloodPressureRepository(application)
    
    private val _records = MutableStateFlow<List<BloodPressureRecord>>(emptyList())
    val records: StateFlow<List<BloodPressureRecord>> = _records

    init {
        loadRecords()
    }

    fun loadRecords() {
        viewModelScope.launch {
            _records.value = repository.getAllRecords().sortedByDescending { it.timestamp }
        }
    }

    fun addRecord(systolic: Int, diastolic: Int, pulse: Int, notes: String) {
        val record = BloodPressureRecord(
            systolic = systolic,
            diastolic = diastolic,
            pulse = pulse,
            notes = notes
        )
        repository.addRecord(record)
        loadRecords()
    }

    fun deleteRecord(recordId: String) {
        repository.deleteRecord(recordId)
        loadRecords()
    }

    fun updateRecord(record: BloodPressureRecord) {
        repository.updateRecord(record)
        loadRecords()
    }
}
