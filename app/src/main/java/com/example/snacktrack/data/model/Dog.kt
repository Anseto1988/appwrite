package com.example.snacktrack.data.model

import androidx.compose.runtime.Stable
import java.time.LocalDate

/**
 * Repräsentiert einen Hund im SnackTrack-System
 */
@Stable
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
    val teamId: String? = null,
    val allergens: List<String> = emptyList()
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

enum class ActivityLevel(val factor: Double, val displayName: String, val description: String) {
    VERY_LOW(1.2, "Sehr niedrig", "Kastriert/sterilisiert, wenig Bewegung"),
    LOW(1.4, "Niedrig", "Normale Haustieraktivität"),
    NORMAL(1.6, "Normal", "Junge erwachsene Hunde"),
    HIGH(1.8, "Hoch", "Aktive/arbeitende Hunde"),
    VERY_HIGH(2.0, "Sehr hoch", "Hochleistungshunde")
} 