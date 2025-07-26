package com.example.snacktrack.data.model

import java.time.LocalDate

/**
 * Repräsentiert den Gesundheitsstatus eines Hundes
 */
data class HealthStatus(
    val id: String = "",
    val dogId: String = "",
    val overallHealth: String = "Gut",
    val lastCheckup: LocalDate? = null,
    val notes: String? = null
)