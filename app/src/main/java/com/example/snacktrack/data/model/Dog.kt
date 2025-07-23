package com.example.snacktrack.data.model

import java.time.LocalDate

/**
 * Repräsentiert einen Hund im SnackTrack-System
 */
data class Dog(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val birthDate: LocalDate? = null,
    val breed: String = "",
    val sex: Sex = Sex.UNKNOWN,
    val weight: Double = 0.0,
    val targetWeight: Double? = null,
    val activityLevel: ActivityLevel = ActivityLevel.NORMAL,
    val imageId: String? = null,
    val teamId: String? = null
) {
    /**
     * Berechnet den täglichen Kalorienbedarf basierend auf RER × Aktivitätsfaktor
     * RER (Resting Energy Requirement) = 70 × (Gewicht in kg)^0.75
     */
    fun calculateDailyCalorieNeed(): Int {
        val rer = 70 * Math.pow(weight, 0.75)
        return (rer * activityLevel.factor).toInt()
    }
}

enum class Sex(val displayName: String) {
    MALE("Männlich"), 
    FEMALE("Weiblich"), 
    UNKNOWN("Unbekannt")
}

enum class ActivityLevel(val factor: Double, val displayName: String) {
    VERY_LOW(1.2, "Sehr niedrig"),
    LOW(1.4, "Niedrig"),
    NORMAL(1.6, "Normal"),
    HIGH(1.8, "Hoch"),
    VERY_HIGH(2.0, "Sehr hoch")
} 