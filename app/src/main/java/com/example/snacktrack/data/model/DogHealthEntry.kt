package com.example.snacktrack.data.model

import java.time.LocalDateTime

/**
 * Represents a health diary entry for a dog
 */
data class DogHealthEntry(
    val id: String = "",
    val dogId: String = "",
    val entryDate: LocalDateTime = LocalDateTime.now(),
    val entryType: HealthEntryType = HealthEntryType.OBSERVATION,
    val symptoms: List<HealthSymptom> = emptyList(),
    val behaviorChanges: List<BehaviorChange> = emptyList(),
    val appetite: AppetiteLevel = AppetiteLevel.NORMAL,
    val energyLevel: EnergyLevel = EnergyLevel.NORMAL,
    val stoolQuality: StoolQuality? = null,
    val vomiting: Boolean = false,
    val temperature: Double? = null, // in Celsius
    val weight: Double? = null, // in kg
    val possibleTriggers: List<String> = emptyList(), // Food items or activities
    val veterinaryVisit: Boolean = false,
    val treatment: String? = null,
    val notes: String = "",
    val attachedImageIds: List<String> = emptyList()
)

enum class HealthEntryType(val displayName: String) {
    OBSERVATION("Beobachtung"),
    SYMPTOM("Symptom"),
    MEDICATION_GIVEN("Medikament verabreicht"),
    VET_VISIT("Tierarztbesuch"),
    VACCINATION("Impfung"),
    ROUTINE_CHECK("Routineuntersuchung")
}

enum class HealthSymptom(val displayName: String, val category: String) {
    // Digestive symptoms
    DIARRHEA("Durchfall", "Verdauung"),
    CONSTIPATION("Verstopfung", "Verdauung"),
    GAS("Blähungen", "Verdauung"),
    LOSS_OF_APPETITE("Appetitlosigkeit", "Verdauung"),
    EXCESSIVE_THIRST("Übermäßiger Durst", "Verdauung"),
    
    // Skin symptoms
    ITCHING("Juckreiz", "Haut"),
    RASH("Ausschlag", "Haut"),
    HOT_SPOTS("Hot Spots", "Haut"),
    HAIR_LOSS("Haarausfall", "Haut"),
    DRY_SKIN("Trockene Haut", "Haut"),
    
    // Respiratory symptoms
    COUGHING("Husten", "Atmung"),
    SNEEZING("Niesen", "Atmung"),
    WHEEZING("Keuchen", "Atmung"),
    NASAL_DISCHARGE("Nasenausfluss", "Atmung"),
    
    // General symptoms
    LETHARGY("Lethargie", "Allgemein"),
    FEVER("Fieber", "Allgemein"),
    PAIN("Schmerzen", "Allgemein"),
    SWELLING("Schwellung", "Allgemein"),
    LIMPING("Hinken", "Allgemein")
}

enum class BehaviorChange(val displayName: String) {
    AGGRESSIVE("Aggressiv"),
    ANXIOUS("Ängstlich"),
    RESTLESS("Unruhig"),
    WITHDRAWN("Zurückgezogen"),
    EXCESSIVE_BARKING("Übermäßiges Bellen"),
    HIDING("Verstecken"),
    CLINGY("Anhänglich"),
    DISORIENTED("Desorientiert")
}

enum class AppetiteLevel(val displayName: String) {
    NO_APPETITE("Kein Appetit"),
    DECREASED("Verringert"),
    NORMAL("Normal"),
    INCREASED("Erhöht"),
    EXCESSIVE("Übermäßig")
}

enum class EnergyLevel(val displayName: String) {
    VERY_LOW("Sehr niedrig"),
    LOW("Niedrig"),
    NORMAL("Normal"),
    HIGH("Hoch"),
    HYPERACTIVE("Hyperaktiv")
}

enum class StoolQuality(val displayName: String, val score: Int) {
    VERY_HARD(displayName = "Sehr hart", score = 1),
    HARD(displayName = "Hart", score = 2),
    IDEAL(displayName = "Ideal", score = 3),
    SOFT_FORMED(displayName = "Weich geformt", score = 4),
    VERY_SOFT(displayName = "Sehr weich", score = 5),
    LIQUID(displayName = "Flüssig", score = 6)
}