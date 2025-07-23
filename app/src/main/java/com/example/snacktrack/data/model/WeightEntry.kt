package com.example.snacktrack.data.model

import java.time.LocalDateTime

/**
 * Repräsentiert einen Gewichtseintrag für einen Hund
 */
data class WeightEntry(
    val id: String = "",
    val dogId: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val weight: Double = 0.0,
    val note: String? = null
) 