package com.example.snacktrack.data.model

import java.time.LocalDateTime

/**
 * Repräsentiert eine Mahlzeit, die ein Hund zu sich genommen hat
 */
data class FoodIntake(
    val id: String = "",
    val dogId: String = "",
    val foodId: String? = null, // Wenn null, dann manueller Eintrag
    val foodName: String = "",
    val amountGram: Double = 0.0,
    val calories: Int = 0,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val note: String? = null,
    // Optional für manuelle Einträge
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null
) 