package com.example.snacktrack.data.model

/**
 * Schweregrad von Allergien
 */
enum class AllergySeverity(val displayName: String) {
    MILD("Mild"),
    MODERATE("Moderat"),
    SEVERE("Schwer"),
    LIFE_THREATENING("Lebensbedrohlich")
}

/**
 * Jahreszeiten für saisonale Pflege
 */
enum class Season(val displayName: String) {
    SPRING("Frühling"),
    SUMMER("Sommer"),
    FALL("Herbst"),
    WINTER("Winter")
}

/**
 * Synchronisierungsintervalle
 */
enum class SyncInterval(val displayName: String, val minutes: Long) {
    REALTIME("Echtzeit", 0),
    EVERY_5_MIN("Alle 5 Minuten", 5),
    EVERY_15_MIN("Alle 15 Minuten", 15),
    EVERY_30_MIN("Alle 30 Minuten", 30),
    HOURLY("Stündlich", 60),
    EVERY_6_HOURS("Alle 6 Stunden", 360),
    DAILY("Täglich", 1440),
    WEEKLY("Wöchentlich", 10080),
    MANUAL("Manuell", -1)
}

/**
 * Empfehlungstypen für AI
 */
enum class RecommendationType(val displayName: String) {
    NUTRITION("Ernährung"),
    EXERCISE("Bewegung"),
    HEALTH("Gesundheit"),
    PREVENTION("Prävention"),
    BEHAVIOR("Verhalten"),
    TRAINING("Training"),
    PRODUCT("Produkt"),
    GENERAL("Allgemein")
}

/**
 * Trendrichtungen für Statistiken
 */
enum class TrendDirection(val displayName: String, val symbol: String) {
    INCREASING("Steigend", "↑"),
    DECREASING("Fallend", "↓"),
    STABLE("Stabil", "→"),
    FLUCTUATING("Schwankend", "↕")
}

/**
 * Risikolevel
 */
enum class RiskLevel(val displayName: String, val color: Long) {
    LOW("Niedrig", 0xFF4CAF50),      // Green
    MEDIUM("Mittel", 0xFFFFC107),    // Amber
    HIGH("Hoch", 0xFFFF9800),        // Orange
    CRITICAL("Kritisch", 0xFFF44336)  // Red
}

/**
 * Arten von Gesundheitseinträgen
 */
enum class HealthEntryType(val displayName: String) {
    WEIGHT("Gewicht"),
    VACCINATION("Impfung"),
    MEDICATION("Medikation"),
    SYMPTOM("Symptom"),
    VET_VISIT("Tierarztbesuch"),
    TREATMENT("Behandlung"),
    ALLERGY("Allergie"),
    INJURY("Verletzung"),
    SURGERY("Operation"),
    LAB_RESULT("Laborergebnis"),
    OTHER("Sonstiges")
}

/**
 * Präventionskategorien
 */
enum class PreventionCategory(val displayName: String) {
    NUTRITION("Ernährung"),
    EXERCISE("Bewegung"),
    DENTAL("Zahnpflege"),
    GROOMING("Fellpflege"),
    MEDICAL("Medizinisch"),
    BEHAVIORAL("Verhalten"),
    ENVIRONMENTAL("Umgebung"),
    GENERAL("Allgemein")
}

/**
 * Prioritätsstufen für Empfehlungen
 */
enum class RecommendationPriority(val displayName: String) {
    LOW("Niedrig"),
    MEDIUM("Mittel"),
    HIGH("Hoch"),
    URGENT("Dringend")
}