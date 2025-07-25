package com.example.snacktrack.data.model

import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Health-related models for dog health tracking
 */

data class HealthEntry(
    val id: String = "",
    val dogId: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val entryType: HealthEntryType = HealthEntryType.OBSERVATION,
    val symptoms: List<String> = emptyList(),
    val notes: String = "",
    val overallRiskScore: Double = 0.0
)

data class PreventionRecommendation(
    val id: String = "",
    val dogId: String = "",
    val type: RecommendationType = RecommendationType.DIETARY,
    val title: String = "",
    val description: String = "",
    val priority: RecommendationPriority = RecommendationPriority.MEDIUM,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class WeightEntry(
    val id: String = "",
    val dogId: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val weight: Double = 0.0,
    val note: String? = null
)

enum class DateRange {
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_90_DAYS,
    LAST_YEAR,
    ALL_TIME,
    CUSTOM
}

// Common health-related enums
enum class AllergySeverity {
    MILD,
    MODERATE,
    SEVERE,
    LIFE_THREATENING
}

enum class Season {
    SPRING,
    SUMMER,
    FALL,
    WINTER
}

enum class SyncInterval {
    NEVER,
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class RecommendationType {
    DIETARY,
    EXERCISE,
    MEDICAL,
    BEHAVIORAL,
    PREVENTIVE,
    EMERGENCY
}

enum class TrendDirection {
    INCREASING,
    DECREASING,
    STABLE,
    VOLATILE
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class Breed {
    GOLDEN_RETRIEVER,
    LABRADOR_RETRIEVER,
    GERMAN_SHEPHERD,
    FRENCH_BULLDOG,
    BULLDOG,
    POODLE,
    BEAGLE,
    ROTTWEILER,
    GERMAN_SHORTHAIRED_POINTER,
    YORKSHIRE_TERRIER,
    MIXED_BREED,
    OTHER
}

enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class DogSize {
    EXTRA_SMALL,  // Under 10 lbs
    SMALL,        // 10-25 lbs  
    MEDIUM,       // 25-60 lbs
    LARGE,        // 60-90 lbs
    EXTRA_LARGE   // Over 90 lbs
}