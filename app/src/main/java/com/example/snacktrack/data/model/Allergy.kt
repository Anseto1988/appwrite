package com.example.snacktrack.data.model

/**
 * Repräsentiert eine Allergie oder Unverträglichkeit
 */
data class Allergy(
    val id: String = "",
    val dogId: String = "",
    val allergen: String = "",
    val severity: AllergySeverity = AllergySeverity.MILD,
    val notes: String? = null
)