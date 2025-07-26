package com.example.snacktrack.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Analytics-related models for statistics and reporting
 */

// Analytics period enum
enum class AnalyticsPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY,
    CUSTOM
}

// Advanced statistics model
data class AdvancedStatistics(
    val id: String = "",
    val dogId: String = "",
    val period: AnalyticsPeriod = AnalyticsPeriod.MONTHLY,
    val weightAnalytics: WeightAnalytics = WeightAnalytics(),
    val nutritionAnalytics: NutritionAnalytics = NutritionAnalytics(),
    val healthAnalytics: HealthAnalytics = HealthAnalytics(),
    val activityAnalytics: ActivityAnalytics = ActivityAnalytics(),
    val costAnalytics: CostAnalytics = CostAnalytics(),
    val behaviorAnalytics: BehaviorAnalytics = BehaviorAnalytics(),
    val comparativeAnalysis: ComparativeAnalysis = ComparativeAnalysis(),
    val predictiveInsights: PredictiveInsights = PredictiveInsights(),
    val generatedAt: LocalDateTime = LocalDateTime.now()
)

// Weight analytics model
data class WeightAnalytics(
    val currentWeight: Double = 0.0,
    val idealWeight: Double = 0.0,
    val bodyConditionScore: Int = 5,
    val weightTrend: StatisticsTrendDirection = StatisticsTrendDirection.STABLE,
    val weightChangePercent: Double = 0.0,
    val projectedWeight: Double = 0.0,
    val weightGoalProgress: Double = 0.0,
    val weightVelocity: Double = 0.0,
    val consistencyScore: Double = 0.0,
    val historicalData: List<WeightDataPoint> = emptyList()
)

// Weight data point
data class WeightDataPoint(
    val date: LocalDate = LocalDate.now(),
    val weight: Double = 0.0,
    val note: String? = null
)

// Nutrition analytics model
data class NutritionAnalytics(
    val averageDailyCalories: Double = 0.0,
    val recommendedDailyCalories: Double = 0.0,
    val calorieBalance: CalorieBalance = CalorieBalance.BALANCED,
    val macronutrientBreakdown: MacronutrientBreakdown = MacronutrientBreakdown(),
    val micronutrientAnalysis: MicronutrientAnalysis = MicronutrientAnalysis(),
    val proteinQualityScore: Double = 0.0,
    val dietDiversityScore: Double = 0.0,
    val mealRegularityScore: Double = 0.0,
    val treatPercentage: Double = 0.0,
    val allergenExposure: List<AllergenExposure> = emptyList(),
    val foodPreferences: List<String> = emptyList(),
    val nutrientDeficiencies: List<String> = emptyList(),
    val nutrientExcesses: List<String> = emptyList(),
    val hydrationScore: Double = 0.0,
    val scheduleAdherence: Double = 0.0,
    val portionControlScore: Double = 0.0
)

// Calorie balance enum
enum class CalorieBalance {
    DEFICIT,
    BALANCED,
    SURPLUS
}

// Macronutrient breakdown
data class MacronutrientBreakdown(
    val proteinPercent: Double = 0.0,
    val fatPercent: Double = 0.0,
    val carbPercent: Double = 0.0,
    val fiberPercent: Double = 0.0
)

// Micronutrient analysis
data class MicronutrientAnalysis(
    val vitamins: Map<String, Double> = emptyMap(),
    val minerals: Map<String, Double> = emptyMap(),
    val adequacyScore: Double = 0.0
)

// Allergen exposure
data class AllergenExposure(
    val allergen: String = "",
    val exposureCount: Int = 0,
    val lastExposure: LocalDate = LocalDate.now(),
    val reactionObserved: Boolean = false
)

// Health analytics model
data class HealthAnalytics(
    val healthScore: Double = 0.0,
    val healthTrend: StatisticsTrendDirection = StatisticsTrendDirection.STABLE,
    val symptomFrequency: Map<String, Int> = emptyMap(),
    val medicationAdherence: Double = 0.0,
    val vaccineStatus: VaccineStatus = VaccineStatus(),
    val preventiveCareScore: Double = 0.0,
    val vetVisitFrequency: Int = 0,
    val healthRiskFactors: List<HealthRiskFactor> = emptyList(),
    val allergyManagementScore: Double = 0.0,
    val chronicConditionManagement: Map<String, Double> = emptyMap(),
    val healthTimeline: List<HealthEvent> = emptyList(),
    val predictedHealthIssues: List<PredictedHealthIssue> = emptyList()
)

// Vaccine status
data class VaccineStatus(
    val upToDate: Boolean = true,
    val overdue: List<String> = emptyList(),
    val upcoming: List<String> = emptyList()
)

// Health risk factor
data class HealthRiskFactor(
    val factor: String = "",
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val description: String = "",
    val recommendations: List<String> = emptyList()
)

// Activity analytics model
data class ActivityAnalytics(
    val dailyActivityMinutes: Double = 0.0,
    val recommendedActivityMinutes: Double = 0.0,
    val activityLevel: StatisticsActivityLevel = StatisticsActivityLevel.NORMAL,
    val activityTrend: StatisticsTrendDirection = StatisticsTrendDirection.STABLE,
    val walkFrequency: Int = 0,
    val averageWalkDuration: Double = 0.0,
    val playTimeMinutes: Double = 0.0,
    val energyExpenditure: Double = 0.0,
    val exerciseConsistency: Double = 0.0,
    val activityGoalProgress: Double = 0.0,
    val activityConsistency: Float = 0.0f,
    val exerciseTypeDistribution: Map<ExerciseType, Float> = emptyMap(),
    val restDays: Int = 0,
    val overexerciseRisk: Double = 0.0,
    val weatherImpact: Double = 0.0,
    val seasonalAdjustment: Double = 0.0,
    val trainingProgress: Map<String, Double> = emptyMap()
)

// Cost analytics model
data class CostAnalytics(
    val monthlyAverage: Double = 0.0,
    val yearlyProjection: Double = 0.0,
    val costBreakdown: CostBreakdown = CostBreakdown(),
    val costTrend: StatisticsTrendDirection = StatisticsTrendDirection.STABLE,
    val costOptimizations: List<CostOptimization> = emptyList(),
    val priceVariations: Map<String, PriceVariation> = emptyMap(),
    val bulkPurchaseSavings: Double = 0.0,
    val loyaltyDiscounts: Double = 0.0,
    val brandLoyaltyCost: Double = 0.0,
    val seasonalCostAdjustment: Double = 0.0,
    val totalMonthlySpend: Double = 0.0,
    val averageDailyCost: Double = 0.0,
    val costPerKg: Double = 0.0,
    val costOptimizationOpportunities: List<String> = emptyList(),
    val projectedAnnualCost: Double = 0.0
)

// Cost breakdown
data class CostBreakdown(
    val food: Double = 0.0,
    val treats: Double = 0.0,
    val healthcare: Double = 0.0,
    val grooming: Double = 0.0,
    val toys: Double = 0.0,
    val other: Double = 0.0
)

// Cost optimization
data class CostOptimization(
    val category: String = "",
    val currentCost: Double = 0.0,
    val optimizedCost: Double = 0.0,
    val savingsAmount: Double = 0.0,
    val savingsPercent: Double = 0.0,
    val recommendation: String = "",
    val implementationDifficulty: DifficultyLevel = DifficultyLevel.EASY
)

// Price variation
data class PriceVariation(
    val productId: String = "",
    val priceHistory: List<PricePoint> = emptyList(),
    val averagePrice: Double = 0.0,
    val lowestPrice: Double = 0.0,
    val highestPrice: Double = 0.0
)

// Price point
data class PricePoint(
    val date: LocalDate = LocalDate.now(),
    val price: Double = 0.0,
    val source: String = ""
)

// Behavior analytics model
data class BehaviorAnalytics(
    val behaviorScore: Double = 0.0,
    val eatingBehavior: Double = 0.0,
    val foodMotivation: FoodMotivation = FoodMotivation.NORMAL,
    val treatResponse: TreatResponse = TreatResponse.MODERATE,
    val mealTimeConsistency: Double = 0.0,
    val pickiness: Double = 0.0,
    val foodPreferences: List<FoodPreference> = emptyList(),
    val eatingSpeed: EatingSpeed = EatingSpeed.NORMAL,
    val eatingBehaviorScore: Double = 0.0,
    val foodMotivationLevel: Double = 0.0,
    val pickeyEaterScore: Double = 0.0,
    val stressEatingPatterns: List<StressIndicator> = emptyList()
)

// Food motivation enum
enum class FoodMotivation {
    LOW,
    NORMAL,
    HIGH,
    OBSESSIVE
}

// Treat response enum
enum class TreatResponse {
    MINIMAL,
    MODERATE,
    HIGH,
    OBSESSIVE
}

// Food preference
data class FoodPreference(
    val foodType: String = "",
    val preferenceScore: Double = 0.0,
    val frequency: Int = 0
)

// Eating speed enum
enum class EatingSpeed {
    VERY_SLOW,
    SLOW,
    NORMAL,
    FAST,
    VERY_FAST
}

// Stress indicator
data class StressIndicator(
    val pattern: String = "",
    val frequency: Int = 0,
    val severity: Double = 0.0
)

// Comparative analysis model
data class ComparativeAnalysis(
    val breedComparison: BreedComparison = BreedComparison(),
    val ageGroupComparison: AgeGroupComparison = AgeGroupComparison(),
    val localAreaComparison: LocalAreaComparison = LocalAreaComparison(),
    val similarDogsComparison: SimilarDogsComparison = SimilarDogsComparison(),
    val yourDog: ComparisonMetrics = ComparisonMetrics(),
    val breedAverage: ComparisonMetrics = ComparisonMetrics(),
    val ageAverage: ComparisonMetrics = ComparisonMetrics(),
    val sampleSize: Int = 0,
    val metrics: List<MetricComparison> = emptyList(),
    val historicalComparison: List<HistoricalComparisonPeriod> = emptyList()
)

// Breed comparison
data class BreedComparison(
    val weightPercentile: Double = 0.0,
    val activityPercentile: Double = 0.0,
    val healthPercentile: Double = 0.0,
    val recommendations: List<String> = emptyList()
)

// Age group comparison
data class AgeGroupComparison(
    val weightPercentile: Double = 0.0,
    val activityPercentile: Double = 0.0,
    val healthPercentile: Double = 0.0,
    val recommendations: List<String> = emptyList()
)

// Local area comparison
data class LocalAreaComparison(
    val averageWeight: Double = 0.0,
    val averageActivity: Double = 0.0,
    val popularFoods: List<String> = emptyList(),
    val commonHealthIssues: List<String> = emptyList()
)

// Similar dogs comparison
data class SimilarDogsComparison(
    val criteria: List<String> = emptyList(),
    val matchingDogs: Int = 0,
    val insights: List<String> = emptyList()
)

// Comparison metrics
data class ComparisonMetrics(
    val weight: Double = 0.0,
    val activity: Double = 0.0,
    val health: Double = 0.0,
    val nutrition: Double = 0.0
)

// Metric comparison
data class MetricComparison(
    val name: String = "",
    val yourValue: Double = 0.0,
    val averageValue: Double = 0.0,
    val unit: String = ""
)

// Historical comparison period
data class HistoricalComparisonPeriod(
    val period: String = "",
    val periods: List<PeriodData> = emptyList()
)

// Period data
data class PeriodData(
    val label: String = "",
    val value: Double = 0.0
)

// Predictive insights model
data class PredictiveInsights(
    val weightPrediction: WeightPrediction = WeightPrediction(),
    val healthPredictions: List<PredictedHealthIssue> = emptyList(),
    val costPrediction: CostPrediction = CostPrediction(),
    val behaviorPredictions: List<BehaviorPrediction> = emptyList(),
    val riskAssessment: RiskAssessment = RiskAssessment(),
    val recommendedInterventions: List<String> = emptyList(),
    val lifestageTransitionPrediction: LifestageTransition? = null
)

// Cost prediction
data class CostPrediction(
    val next30DaysCost: Double = 0.0,
    val next90DaysCost: Double = 0.0,
    val annualProjection: Double = 0.0,
    val inflationAdjustedCost: Double = 0.0,
    val budgetAlerts: List<String> = emptyList()
)

// Lifestage transition
data class LifestageTransition(
    val nextStage: String = "",
    val expectedDate: LocalDate = LocalDate.now(),
    val recommendations: List<String> = emptyList()
)

// Behavior prediction
data class BehaviorPrediction(
    val behavior: String = "",
    val likelihood: Double = 0.0,
    val timeframe: String = "",
    val triggers: List<String> = emptyList(),
    val preventiveMeasures: List<String> = emptyList()
)

// Trend direction enum
enum class StatisticsTrendDirection {
    INCREASING,
    STABLE,
    DECREASING
}

// Report type enum
enum class ReportType {
    STANDARD,
    DETAILED,
    EXECUTIVE,
    CUSTOM
}

// Section type enum
enum class SectionType {
    SUMMARY,
    CHART,
    TABLE,
    ANALYSIS,
    RECOMMENDATIONS
}

// Visualization type enum
enum class VisualizationType {
    LINE_CHART,
    BAR_CHART,
    PIE_CHART,
    SCATTER_PLOT,
    HEATMAP,
    TABLE
}

// Report section
data class ReportSection(
    val id: String = "",
    val title: String = "",
    val type: SectionType = SectionType.SUMMARY,
    val dataSource: DataSource = DataSource.CALCULATED,
    val visualization: VisualizationType = VisualizationType.LINE_CHART,
    val metrics: List<String> = emptyList(),
    val customization: ReportCustomization = ReportCustomization()
)

// Report customization
data class ReportCustomization(
    val colors: List<String> = emptyList(),
    val fontSize: Int = 12,
    val showLegend: Boolean = true,
    val showDataLabels: Boolean = false
)

// Priority level enum
enum class PriorityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

// Mitigation step
data class MitigationStep(
    val action: String = "",
    val priority: PriorityLevel = PriorityLevel.MEDIUM,
    val timeline: String = "",
    val resources: List<String> = emptyList(),
    val expectedOutcome: String = ""
)

// Missing enums and data classes for StatisticsRepository

enum class StatisticsSeason {
    SPRING,
    SUMMER,
    FALL,
    WINTER
}

data class AggressionIndicator(
    val type: String = "",
    val frequency: Int = 0,
    val triggers: List<String> = emptyList()
)

enum class BeggingLevel {
    NONE,
    MINIMAL,
    MODERATE,
    EXCESSIVE
}

enum class SocialEatingPattern {
    INDEPENDENT,
    SOCIAL,
    COMPETITIVE,
    ANXIOUS
}

data class CustomReport(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val reportType: ReportType = ReportType.CUSTOM,
    val sections: List<ReportSection> = emptyList(),
    val filters: ReportFilters = ReportFilters(),
    val schedule: ReportSchedule? = null
)

data class ReportFilters(
    val dateRange: String = "",
    val categories: List<String> = emptyList(),
    val metrics: List<String> = emptyList(),
    val dogs: List<String> = emptyList(),
    val comparison: String? = null
)

data class ReportSchedule(
    val frequency: String = "",
    val dayOfWeek: Int? = null,
    val dayOfMonth: Int? = null,
    val time: String = "",
    val nextRunDate: LocalDate? = null,
    val isActive: Boolean = true,
    val recipients: List<String> = emptyList(),
    val format: ReportFormat = ReportFormat.PDF
)

enum class ReportFormat {
    PDF,
    EXCEL,
    CSV,
    JSON,
    HTML
}

enum class MuscleCondition {
    NORMAL,
    MILD_LOSS,
    MODERATE_LOSS,
    SEVERE_LOSS
}

enum class ProteinQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}

enum class FatQuality {
    OPTIMAL,
    ACCEPTABLE,
    HIGH,
    LOW
}

enum class CarbQuality {
    COMPLEX,
    SIMPLE,
    MIXED,
    MINIMAL
}

data class NutrientDeficiency(
    val nutrient: String = "",
    val severity: String = "",
    val impact: String = ""
)

data class NutrientExcess(
    val nutrient: String = "",
    val amount: Double = 0.0,
    val risk: String = ""
)