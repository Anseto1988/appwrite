package com.example.snacktrack.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Models specific to statistics and analysis features
 */

// Weight prediction model
data class WeightPrediction(
    val predictedWeight30Days: Double = 0.0,
    val predictedWeight90Days: Double = 0.0,
    val confidenceLevel: Float = 0.0f,
    val assumptions: List<String> = emptyList(),
    val scenarioAnalysis: Map<String, Double> = emptyMap()
)

// Risk assessment model  
data class RiskAssessment(
    val dogId: String = "",
    val assessmentDate: LocalDateTime = LocalDateTime.now(),
    val overallRiskScore: Double = 0.0,
    val riskCategories: Map<RiskCategory, StatisticsRiskLevel> = emptyMap(),
    val recommendations: List<String> = emptyList(),
    val timelinessScore: Double = 0.0
)

// Risk category enum
enum class RiskCategory {
    WEIGHT,
    NUTRITION,
    ACTIVITY,
    HEALTH,
    BEHAVIORAL,
    ENVIRONMENTAL
}

// Statistics-specific risk level (to avoid conflict with other RiskLevel enums)
enum class StatisticsRiskLevel {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

// Difficulty level for recommendations
enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}

// Data source for analytics
enum class DataSource {
    MANUAL_ENTRY,
    BARCODE_SCAN,
    IMPORT,
    CALCULATED,
    AI_GENERATED
}

// Activity level specific to statistics (avoiding conflicts)
enum class StatisticsActivityLevel {
    SEDENTARY,
    LOW,
    NORMAL,
    HIGH,
    VERY_HIGH
}

// Model to map between different activity level enums
fun ActivityLevel.toStatisticsActivityLevel(): StatisticsActivityLevel {
    return when (this) {
        ActivityLevel.VERY_LOW -> StatisticsActivityLevel.SEDENTARY
        ActivityLevel.LOW -> StatisticsActivityLevel.LOW
        ActivityLevel.NORMAL -> StatisticsActivityLevel.NORMAL
        ActivityLevel.HIGH -> StatisticsActivityLevel.HIGH
        ActivityLevel.VERY_HIGH -> StatisticsActivityLevel.VERY_HIGH
    }
}

// Model to map between different risk level enums
fun RiskLevel.toStatisticsRiskLevel(): StatisticsRiskLevel {
    return when (this) {
        RiskLevel.LOW -> StatisticsRiskLevel.LOW
        RiskLevel.MEDIUM -> StatisticsRiskLevel.MODERATE
        RiskLevel.HIGH -> StatisticsRiskLevel.HIGH
        RiskLevel.CRITICAL -> StatisticsRiskLevel.VERY_HIGH
    }
}

// Breed information for activity calculations
data class BreedInfo(
    val name: String = "",
    val energyLevel: Int = 3, // 1-5 scale
    val exerciseNeeds: Int = 3, // 1-5 scale
    val size: BreedSize = BreedSize.MEDIUM
)

enum class BreedSize {
    SMALL,
    MEDIUM,
    LARGE,
    GIANT
}

// Health event for timeline tracking
data class HealthEvent(
    val date: LocalDate = LocalDate.now(),
    val eventType: HealthEventType = HealthEventType.OBSERVATION,
    val description: String = "",
    val severity: SeverityLevel = SeverityLevel.MILD,
    val outcome: String = "",
    val treatmentGiven: String? = null
)

enum class HealthEventType {
    OBSERVATION,
    ILLNESS,
    INJURY,
    VACCINATION,
    MEDICATION,
    ROUTINE_CHECKUP,
    EMERGENCY_VISIT
}

enum class SeverityLevel {
    MILD,
    MODERATE,
    SEVERE,
    CRITICAL
}

// Predicted health issue model
data class PredictedHealthIssue(
    val condition: String = "",
    val probability: Double = 0.0,
    val timeframe: String = "",
    val preventiveMeasures: List<String> = emptyList(),
    val riskFactors: List<String> = emptyList()
)

// Health prediction model (alias for PredictedHealthIssue for compatibility)
typealias HealthPrediction = PredictedHealthIssue

// Risk model for risk assessment
data class Risk(
    val name: String = "",
    val category: RiskCategory = RiskCategory.HEALTH,
    val severity: RiskLevel = RiskLevel.LOW,
    val likelihood: Double = 0.0,
    val impact: String = "",
    val timeframe: String = ""
)