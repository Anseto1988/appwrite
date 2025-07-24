package com.example.snacktrack.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * AI-based food recommendation
 */
data class FoodRecommendation(
    val id: String = "",
    val dogId: String = "",
    val generatedAt: LocalDateTime = LocalDateTime.now(),
    val recommendationType: RecommendationType = RecommendationType.DAILY,
    val recommendations: List<FoodRecommendationItem> = emptyList(),
    val reasoning: String = "",
    val confidenceScore: Float = 0f,
    val factors: RecommendationFactors = RecommendationFactors()
)

data class FoodRecommendationItem(
    val foodId: String = "",
    val foodName: String = "",
    val brand: String = "",
    val recommendedAmount: Double = 0.0, // in grams
    val frequency: FeedingFrequency = FeedingFrequency.DAILY,
    val reason: String = "",
    val nutritionMatch: Float = 0f, // 0-1 score
    val allergenSafe: Boolean = true
)

data class RecommendationFactors(
    val breed: String = "",
    val age: Double = 0.0, // in years
    val weight: Double = 0.0,
    val activityLevel: ActivityLevel = ActivityLevel.NORMAL,
    val healthConditions: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val preferences: List<String> = emptyList()
)

enum class RecommendationType {
    DAILY,          // Daily feeding plan
    WEEKLY,         // Weekly rotation
    TRANSITION,     // Food transition plan
    SPECIAL_DIET,   // Special dietary needs
    WEIGHT_LOSS,    // Weight loss program
    WEIGHT_GAIN     // Weight gain program
}

enum class FeedingFrequency {
    ONCE_DAILY,
    TWICE_DAILY,
    THREE_TIMES_DAILY,
    FREE_FEEDING,
    CUSTOM
}

/**
 * Weight prediction model
 */
data class WeightPrediction(
    val id: String = "",
    val dogId: String = "",
    val predictionDate: LocalDate = LocalDate.now(),
    val currentWeight: Double = 0.0,
    val predictions: List<WeightPredictionPoint> = emptyList(),
    val confidenceInterval: ConfidenceInterval = ConfidenceInterval(),
    val modelVersion: String = "1.0",
    val factors: WeightPredictionFactors = WeightPredictionFactors()
)

data class WeightPredictionPoint(
    val date: LocalDate,
    val predictedWeight: Double,
    val confidenceLevel: Float // 0-1
)

data class ConfidenceInterval(
    val lower: List<Double> = emptyList(),
    val upper: List<Double> = emptyList()
)

data class WeightPredictionFactors(
    val historicalWeights: List<WeightEntry> = emptyList(),
    val averageDailyCalories: Int = 0,
    val activityLevel: ActivityLevel = ActivityLevel.NORMAL,
    val breed: String = "",
    val age: Double = 0.0,
    val isNeutered: Boolean = false,
    val healthConditions: List<String> = emptyList()
)

/**
 * Anomaly detection for eating habits
 */
data class EatingAnomaly(
    val id: String = "",
    val dogId: String = "",
    val detectedAt: LocalDateTime = LocalDateTime.now(),
    val anomalyType: AnomalyType = AnomalyType.UNUSUAL_AMOUNT,
    val severity: AnomalySeverity = AnomalySeverity.LOW,
    val description: String = "",
    val affectedMeals: List<FoodIntake> = emptyList(),
    val baselineData: BaselineEatingPattern = BaselineEatingPattern(),
    val recommendation: String = "",
    val requiresVetAttention: Boolean = false
)

enum class AnomalyType {
    UNUSUAL_AMOUNT,      // Eating too much or too little
    SKIPPED_MEALS,       // Missing regular meals
    TIME_PATTERN,        // Eating at unusual times
    FOOD_REJECTION,      // Rejecting usual food
    RAPID_CONSUMPTION,   // Eating too fast
    SLOW_CONSUMPTION,    // Eating too slow
    FREQUENT_SNACKING,   // Too many treats
    WATER_INTAKE         // Unusual water consumption
}

enum class AnomalySeverity {
    LOW,      // Minor deviation, monitor
    MEDIUM,   // Noticeable change, consider action
    HIGH,     // Significant change, action recommended
    CRITICAL  // Immediate attention needed
}

data class BaselineEatingPattern(
    val averageDailyCalories: Int = 0,
    val averageMealSize: Double = 0.0,
    val normalMealTimes: List<String> = emptyList(), // HH:mm format
    val normalEatingDuration: Int = 0, // minutes
    val preferredFoods: List<String> = emptyList()
)

/**
 * AI Training Data for continuous improvement
 */
data class AITrainingData(
    val id: String = "",
    val modelType: AIModelType = AIModelType.FOOD_RECOMMENDATION,
    val inputData: Map<String, Any> = emptyMap(),
    val actualOutcome: Map<String, Any> = emptyMap(),
    val userFeedback: UserFeedback? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class AIModelType {
    FOOD_RECOMMENDATION,
    WEIGHT_PREDICTION,
    ANOMALY_DETECTION,
    HEALTH_PREDICTION
}

data class UserFeedback(
    val accurate: Boolean = false,
    val helpful: Boolean = false,
    val comments: String = "",
    val rating: Int = 0 // 1-5 stars
)

/**
 * AI-based health risk assessment
 */
data class HealthRiskAssessment(
    val id: String = "",
    val dogId: String = "",
    val assessmentDate: LocalDateTime = LocalDateTime.now(),
    val overallRiskScore: Float = 0f, // 0-1, higher is worse
    val riskFactors: List<RiskFactor> = emptyList(),
    val recommendations: List<HealthRecommendation> = emptyList(),
    val nextAssessmentDate: LocalDate = LocalDate.now().plusMonths(1)
)

data class RiskFactor(
    val factor: String = "",
    val category: RiskCategory = RiskCategory.NUTRITION,
    val severity: Float = 0f, // 0-1
    val description: String = "",
    val improvementPlan: String = ""
)

enum class RiskCategory {
    NUTRITION,
    WEIGHT,
    ACTIVITY,
    GENETICS,
    AGE,
    ENVIRONMENT,
    MEDICAL_HISTORY
}

data class HealthRecommendation(
    val priority: Int = 0, // 1 = highest
    val category: String = "",
    val action: String = "",
    val expectedBenefit: String = "",
    val timeframe: String = ""
)