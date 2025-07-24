package com.example.snacktrack.data.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Represents a medication schedule for a dog
 */
data class DogMedication(
    val id: String = "",
    val dogId: String = "",
    val medicationName: String = "",
    val medicationType: MedicationType = MedicationType.ORAL,
    val dosage: String = "",
    val frequency: MedicationFrequency = MedicationFrequency.DAILY,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val reminderTimes: List<LocalTime> = emptyList(),
    val foodInteraction: FoodInteraction = FoodInteraction.NONE,
    val purpose: String = "",
    val veterinarianName: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true
)

enum class MedicationType(val displayName: String) {
    ORAL("Oral"),
    TOPICAL("Topisch"),
    INJECTION("Injektion"),
    EYE_DROPS("Augentropfen"),
    EAR_DROPS("Ohrentropfen"),
    OTHER("Sonstige")
}

enum class MedicationFrequency(val displayName: String) {
    ONCE("Einmal"),
    DAILY("Täglich"),
    TWICE_DAILY("Zweimal täglich"),
    THREE_TIMES_DAILY("Dreimal täglich"),
    WEEKLY("Wöchentlich"),
    AS_NEEDED("Bei Bedarf"),
    CUSTOM("Benutzerdefiniert")
}

enum class FoodInteraction(val displayName: String, val instruction: String) {
    NONE("Keine", "Kann jederzeit verabreicht werden"),
    WITH_FOOD("Mit Futter", "Mit dem Futter verabreichen"),
    EMPTY_STOMACH("Nüchtern", "Mindestens 1 Stunde vor oder 2 Stunden nach dem Futter"),
    BEFORE_FOOD("Vor dem Futter", "30 Minuten vor dem Futter verabreichen"),
    AFTER_FOOD("Nach dem Futter", "Nach dem Futter verabreichen")
}