package com.example.appsuivitension.model

import java.util.UUID

data class BloodPressureRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val notes: String = ""
)
