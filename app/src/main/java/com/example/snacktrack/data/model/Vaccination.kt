package com.example.snacktrack.data.model

import java.time.LocalDate

/**
 * Repräsentiert eine Impfung für einen Hund
 */
data class Vaccination(
    val id: String = "",
    val dogId: String = "",
    val name: String = "",
    val vaccineName: String = "",
    val dueDate: LocalDate = LocalDate.now(),
    val lastDate: LocalDate? = null,
    val notes: String? = null
) {
    // Alias for backwards compatibility
    constructor(
        id: String = "",
        dogId: String = "",
        name: String = "",
        dueDate: LocalDate = LocalDate.now(),
        lastDate: LocalDate? = null,
        notes: String? = null
    ) : this(id, dogId, name, name, dueDate, lastDate, notes)
}