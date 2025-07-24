package com.example.snacktrack.data.model

import java.time.LocalDate

/**
 * Represents an allergy or food sensitivity for a dog
 */
data class DogAllergy(
    val id: String = "",
    val dogId: String = "",
    val allergen: String = "",
    val allergyType: AllergyType = AllergyType.FOOD,
    val severity: AllergySeverity = AllergySeverity.MILD,
    val symptoms: List<String> = emptyList(),
    val diagnosedDate: LocalDate? = null,
    val diagnosedBy: String? = null, // Veterinarian name
    val notes: String? = null,
    val isActive: Boolean = true
)

enum class AllergyType(val displayName: String) {
    FOOD("Futtermittelallergie"),
    ENVIRONMENTAL("Umweltallergie"),
    CONTACT("Kontaktallergie"),
    MEDICATION("Medikamentenallergie"),
    OTHER("Sonstige")
}

enum class AllergySeverity(val displayName: String, val colorCode: Long) {
    MILD("Leicht", 0xFFFFC107),      // Yellow
    MODERATE("Mittel", 0xFFFF9800),   // Orange
    SEVERE("Schwer", 0xFFFF5722),     // Deep Orange
    CRITICAL("Kritisch", 0xFFF44336)  // Red
}