package com.example.snacktrack.data.model

import java.time.LocalDateTime
import java.time.LocalTime

data class MedicationReminder(
    val id: String,
    val dogId: String,
    val medicationName: String,
    val dosage: String,
    val frequency: MedicationFrequency,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime? = null,
    val reminderTimes: List<LocalTime>,
    val instructions: String? = null,
    val isActive: Boolean = true,
    val lastGivenAt: LocalDateTime? = null,
    val nextDueAt: LocalDateTime? = null
)