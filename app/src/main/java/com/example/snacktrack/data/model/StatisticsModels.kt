package com.example.snacktrack.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Advanced statistics and analytics models
 */

data class AdvancedStatistics(
    val id: String = "",
    val dogId: String = "",
    val period: AnalyticsPeriod = AnalyticsPeriod.MONTH,
    val startDate: LocalDate = LocalDate.now().minusMonths(1),
    val endDate: LocalDate = LocalDate.now(),
    val generatedAt: LocalDateTime = LocalDateTime.now(),
    
    // Weight Analytics
    val weightAnalytics: WeightAnalytics = WeightAnalytics(),
    
    // Nutrition Analytics
    val nutritionAnalytics: NutritionAnalytics = NutritionAnalytics(),
    
    // Health Analytics
    val healthAnalytics: HealthAnalytics = HealthAnalytics(),
    
    // Activity Analytics
    val activityAnalytics: ActivityAnalytics = ActivityAnalytics(),
    
    // Cost Analytics
    val costAnalytics: CostAnalytics = CostAnalytics(),
    
    // Behavioral Analytics
    val behavioralAnalytics: BehavioralAnalytics = BehavioralAnalytics(),
    
    // Predictive Insights
    val predictiveInsights: PredictiveInsights = PredictiveInsights(),
    
    // Comparative Analysis
    val comparativeAnalysis: ComparativeAnalysis = ComparativeAnalysis()
)

data class WeightAnalytics(
    val currentWeight: Double = 0.0,
    val idealWeight: Double = 0.0,
    val weightTrend: StatisticsTrendDirection = StatisticsTrendDirection.STABLE,
    val weightChangePercent: Double = 0.0,
    val weightChangeAmount: Double = 0.0,
    val projectedWeight: Double = 0.0, // Next month
    val daysToIdealWeight: Int? = null,
    val weightVelocity: Double = 0.0, // kg per week
    val bodyConditionScore: Double = 0.0, // 1-9 scale
    val muscleConditionScore: MuscleCondition = MuscleCondition.NORMAL,
    val weightHistory: List<WeightDataPoint> = emptyList(),
    val weightVariability: Double = 0.0, // Standard deviation
    val consistencyScore: Double = 0.0 // How consistent weight measurements are
)

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
    val hydrationEstimate: Double = 0.0, // ml per kg body weight
    val nutritionalCompleteness: Double = 0.0, // 0-100%
    val topNutrientDeficiencies: List<NutrientDeficiency> = emptyList(),
    val topNutrientExcesses: List<NutrientExcess> = emptyList(),
    val feedingScheduleAdherence: Double = 0.0,
    val portionControlScore: Double = 0.0
)

data class HealthAnalytics(
    val healthScore: Double = 0.0, // 0-100
    val healthTrend: StatisticsTrendDirection = StatisticsTrendDirection.STABLE,
    val symptomFrequency: Map<String, Int> = emptyMap(),
    val medicationAdherence: Double = 0.0,
    val vaccineStatus: VaccineStatus = VaccineStatus(),
    val preventiveCareScore: Double = 0.0,
    val vetVisitFrequency: Int = 0, // visits per year
    val healthRiskFactors: List<HealthRiskFactor> = emptyList(),
    val allergyManagementScore: Double = 0.0,
    val chronicConditionManagement: Map<String, ConditionManagement> = emptyMap(),
    val emergencyReadinessScore: Double = 0.0,
    val healthEventTimeline: List<HealthEvent> = emptyList(),
    val predictedHealthIssues: List<PredictedHealthIssue> = emptyList()
)

data class ActivityAnalytics(
    val dailyActivityMinutes: Double = 0.0,
    val recommendedActivityMinutes: Double = 0.0,
    val activityLevel: StatisticsActivityLevel = StatisticsActivityLevel.MODERATE,
    val activityTrend: StatisticsTrendDirection = StatisticsTrendDirection.STABLE,
    val walkFrequency: Int = 0, // per week
    val averageWalkDuration: Double = 0.0,
    val playTimeMinutes: Double = 0.0,
    val trainingSessionsPerWeek: Double = 0.0,
    val restQualityScore: Double = 0.0,
    val energyExpenditureEstimate: Double = 0.0, // calories
    val activityConsistency: Double = 0.0,
    val exerciseTypeDistribution: Map<ExerciseType, Double> = emptyMap(),
    val peakActivityTimes: List<Int> = emptyList(), // hours of day
    val weatherImpactScore: Double = 0.0 // How weather affects activity
)

data class CostAnalytics(
    val totalMonthlySpend: Double = 0.0,
    val averageDailyCost: Double = 0.0,
    val costTrend: StatisticsTrendDirection = StatisticsTrendDirection.STABLE,
    val costBreakdown: CostBreakdown = CostBreakdown(),
    val costPerKg: Double = 0.0, // Cost per kg of body weight
    val budgetUtilization: Double = 0.0, // % of budget used
    val projectedAnnualCost: Double = 0.0,
    val costOptimizationOpportunities: List<CostOptimization> = emptyList(),
    val priceVariationAnalysis: Map<String, PriceVariation> = emptyMap(),
    val bulkPurchaseSavings: Double = 0.0,
    val brandLoyaltyCost: Double = 0.0, // Extra cost for brand preference
    val seasonalCostVariation: Map<StatisticsSeason, Double> = emptyMap()
)

data class BehavioralAnalytics(
    val eatingBehaviorScore: Double = 0.0,
    val foodMotivationLevel: FoodMotivation = FoodMotivation.NORMAL,
    val treatResponsePattern: TreatResponse = TreatResponse.MODERATE,
    val mealTimeConsistency: Double = 0.0,
    val foodAggressionIndicators: List<AggressionIndicator> = emptyList(),
    val pickeyEaterScore: Double = 0.0,
    val foodPreferences: List<FoodPreference> = emptyList(),
    val eatingSpeed: EatingSpeed = EatingSpeed.NORMAL,
    val beggingFrequency: BeggingLevel = BeggingLevel.OCCASIONAL,
    val stressEatingIndicators: List<StressIndicator> = emptyList(),
    val socialEatingBehavior: SocialEatingPattern = SocialEatingPattern.INDEPENDENT
)

data class PredictiveInsights(
    val weightPrediction: StatisticsWeightPrediction = StatisticsWeightPrediction(),
    val healthPredictions: List<HealthPrediction> = emptyList(),
    val nutritionPredictions: List<NutritionPrediction> = emptyList(),
    val costPredictions: CostPrediction = CostPrediction(),
    val behaviorPredictions: List<BehaviorPrediction> = emptyList(),
    val riskAssessment: StatisticsRiskAssessment = StatisticsRiskAssessment(),
    val recommendedInterventions: List<Intervention> = emptyList(),
    val lifestageTransitionPrediction: LifestageTransition? = null
)

data class ComparativeAnalysis(
    val breedComparison: BreedComparison = BreedComparison(),
    val ageGroupComparison: AgeGroupComparison = AgeGroupComparison(),
    val similarDogsComparison: SimilarDogsComparison = SimilarDogsComparison(),
    val historicalComparison: HistoricalComparison = HistoricalComparison(),
    val goalComparison: GoalComparison = GoalComparison(),
    val regionalComparison: RegionalComparison? = null
)

// Supporting data classes

data class WeightDataPoint(
    val date: LocalDate = LocalDate.now(),
    val weight: Double = 0.0,
    val bodyConditionScore: Double? = null
)

data class MacronutrientBreakdown(
    val proteinPercent: Double = 0.0,
    val fatPercent: Double = 0.0,
    val carbPercent: Double = 0.0,
    val fiberPercent: Double = 0.0,
    val moisturePercent: Double = 0.0,
    val proteinQuality: ProteinQuality = ProteinQuality.MODERATE,
    val fatQuality: FatQuality = FatQuality.MODERATE,
    val carbQuality: CarbQuality = CarbQuality.MODERATE
)

data class MicronutrientAnalysis(
    val vitamins: Map<Vitamin, NutrientStatus> = emptyMap(),
    val minerals: Map<Mineral, NutrientStatus> = emptyMap(),
    val essentialFattyAcids: Map<FattyAcid, NutrientStatus> = emptyMap(),
    val aminoAcids: Map<AminoAcid, NutrientStatus> = emptyMap()
)

data class NutrientDeficiency(
    val nutrient: String = "",
    val currentLevel: Double = 0.0,
    val recommendedLevel: Double = 0.0,
    val deficiencyPercent: Double = 0.0,
    val healthImpact: HealthImpactLevel = HealthImpactLevel.LOW,
    val recommendations: List<String> = emptyList()
)

data class NutrientExcess(
    val nutrient: String = "",
    val currentLevel: Double = 0.0,
    val recommendedLevel: Double = 0.0,
    val excessPercent: Double = 0.0,
    val healthImpact: HealthImpactLevel = HealthImpactLevel.LOW,
    val recommendations: List<String> = emptyList()
)

data class VaccineStatus(
    val coreVaccinesUpToDate: Boolean = false,
    val nonCoreVaccinesUpToDate: Boolean = false,
    val nextVaccineDue: LocalDate? = null,
    val overdueVaccines: List<String> = emptyList(),
    val vaccineHistory: List<VaccineRecord> = emptyList()
)

data class VaccineRecord(
    val vaccineName: String = "",
    val dateAdministered: LocalDate = LocalDate.now(),
    val nextDueDate: LocalDate? = null,
    val veterinarian: String = ""
)

data class HealthRiskFactor(
    val factor: String = "",
    val riskLevel: StatisticsRiskLevel = StatisticsRiskLevel.LOW,
    val category: StatisticsRiskCategory = StatisticsRiskCategory.GENETIC,
    val mitigationStrategies: List<String> = emptyList(),
    val monitoringRequired: Boolean = false
)

data class ConditionManagement(
    val condition: String = "",
    val managementScore: Double = 0.0,
    val medicationAdherence: Double = 0.0,
    val dietCompliance: Double = 0.0,
    val monitoringFrequency: String = "",
    val lastCheckup: LocalDate? = null,
    val trend: StatisticsTrendDirection = StatisticsTrendDirection.STABLE
)

data class HealthEvent(
    val date: LocalDate = LocalDate.now(),
    val eventType: HealthEventType = HealthEventType.ROUTINE_CHECKUP,
    val description: String = "",
    val severity: SeverityLevel = SeverityLevel.MILD,
    val outcome: String = "",
    val followUpRequired: Boolean = false
)

data class PredictedHealthIssue(
    val issue: String = "",
    val probability: Double = 0.0,
    val timeframe: String = "",
    val riskFactors: List<String> = emptyList(),
    val preventiveMeasures: List<String> = emptyList()
)

data class CostBreakdown(
    val food: Double = 0.0,
    val treats: Double = 0.0,
    val supplements: Double = 0.0,
    val medication: Double = 0.0,
    val vetCare: Double = 0.0,
    val grooming: Double = 0.0,
    val accessories: Double = 0.0,
    val other: Double = 0.0
)

data class CostOptimization(
    val category: String = "",
    val currentCost: Double = 0.0,
    val optimizedCost: Double = 0.0,
    val savingsAmount: Double = 0.0,
    val savingsPercent: Double = 0.0,
    val recommendation: String = "",
    val implementationDifficulty: StatisticsDifficultyLevel = StatisticsDifficultyLevel.EASY
)

data class PriceVariation(
    val product: String = "",
    val lowestPrice: Double = 0.0,
    val highestPrice: Double = 0.0,
    val averagePrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val priceVolatility: Double = 0.0,
    val bestPurchaseTime: String = ""
)

data class FoodPreference(
    val foodType: String = "",
    val preferenceScore: Double = 0.0,
    val consistencyLevel: Double = 0.0,
    val texture: FoodTexture = FoodTexture.MIXED,
    val temperature: FoodTemperature = FoodTemperature.ROOM_TEMP,
    val flavorProfile: FlavorProfile = FlavorProfile.MEATY
)

data class AggressionIndicator(
    val behavior: String = "",
    val frequency: FrequencyLevel = FrequencyLevel.RARE,
    val triggers: List<String> = emptyList(),
    val severity: SeverityLevel = SeverityLevel.MILD
)

data class StressIndicator(
    val indicator: String = "",
    val frequency: FrequencyLevel = FrequencyLevel.OCCASIONAL,
    val correlatedFactors: List<String> = emptyList()
)

data class StatisticsWeightPrediction(
    val predictedWeight30Days: Double = 0.0,
    val predictedWeight90Days: Double = 0.0,
    val confidenceLevel: Double = 0.0,
    val assumptions: List<String> = emptyList(),
    val scenarioAnalysis: Map<String, Double> = emptyMap() // Different scenarios
)

data class HealthPrediction(
    val condition: String = "",
    val probability: Double = 0.0,
    val timeframe: String = "",
    val earlyWarningSignals: List<String> = emptyList(),
    val preventiveMeasures: List<String> = emptyList()
)

data class NutritionPrediction(
    val predictedDeficiency: String? = null,
    val timeToDeficiency: Int? = null, // days
    val predictedExcess: String? = null,
    val timeToExcess: Int? = null,
    val optimalAdjustments: List<NutritionAdjustment> = emptyList()
)

data class NutritionAdjustment(
    val nutrient: String = "",
    val currentAmount: Double = 0.0,
    val recommendedAmount: Double = 0.0,
    val adjustmentMethod: String = "",
    val expectedOutcome: String = ""
)

data class CostPrediction(
    val next30DaysCost: Double = 0.0,
    val next90DaysCost: Double = 0.0,
    val annualProjection: Double = 0.0,
    val inflationAdjustedCost: Double = 0.0,
    val budgetAlerts: List<BudgetAlert> = emptyList()
)

data class BudgetAlert(
    val alertType: AlertType = AlertType.INFO,
    val message: String = "",
    val threshold: Double = 0.0,
    val projectedDate: LocalDate? = null
)

data class BehaviorPrediction(
    val behavior: String = "",
    val likelihood: Double = 0.0,
    val triggeringFactors: List<String> = emptyList(),
    val preventiveActions: List<String> = emptyList()
)

data class StatisticsRiskAssessment(
    val overallRiskScore: Double = 0.0,
    val healthRiskScore: Double = 0.0,
    val nutritionRiskScore: Double = 0.0,
    val behaviorRiskScore: Double = 0.0,
    val highestRisks: List<Risk> = emptyList(),
    val mitigationPlan: List<MitigationStep> = emptyList()
)

data class Risk(
    val name: String = "",
    val category: StatisticsRiskCategory = StatisticsRiskCategory.MEDICAL,
    val severity: StatisticsRiskLevel = StatisticsRiskLevel.LOW,
    val likelihood: Double = 0.0,
    val impact: String = "",
    val timeframe: String = ""
)

data class MitigationStep(
    val action: String = "",
    val priority: PriorityLevel = PriorityLevel.MEDIUM,
    val timeline: String = "",
    val resources: List<String> = emptyList(),
    val expectedOutcome: String = ""
)

data class Intervention(
    val type: InterventionType = InterventionType.DIETARY,
    val title: String = "",
    val description: String = "",
    val urgency: UrgencyLevel = UrgencyLevel.MODERATE,
    val expectedBenefit: String = "",
    val implementationSteps: List<String> = emptyList(),
    val monitoringRequired: Boolean = false
)

data class LifestageTransition(
    val currentStage: LifeStage = LifeStage.ADULT,
    val nextStage: LifeStage = LifeStage.SENIOR,
    val estimatedTransitionDate: LocalDate? = null,
    val preparationSteps: List<String> = emptyList(),
    val dietaryChangesRequired: List<String> = emptyList()
)

data class BreedComparison(
    val breedAverage: Map<String, Double> = emptyMap(),
    val yourDog: Map<String, Double> = emptyMap(),
    val percentileRanking: Map<String, Double> = emptyMap(),
    val areasOfConcern: List<String> = emptyList(),
    val areasOfExcellence: List<String> = emptyList()
)

data class AgeGroupComparison(
    val ageGroupAverage: Map<String, Double> = emptyMap(),
    val yourDog: Map<String, Double> = emptyMap(),
    val developmentalStatus: DevelopmentalStatus = DevelopmentalStatus.ON_TRACK
)

data class SimilarDogsComparison(
    val comparisonGroup: String = "", // e.g., "Medium-sized adult dogs"
    val sampleSize: Int = 0,
    val metrics: Map<String, ComparisonMetric> = emptyMap()
)

data class ComparisonMetric(
    val yourDogValue: Double = 0.0,
    val groupAverage: Double = 0.0,
    val percentile: Double = 0.0,
    val interpretation: String = ""
)

data class HistoricalComparison(
    val currentPeriod: Map<String, Double> = emptyMap(),
    val previousPeriod: Map<String, Double> = emptyMap(),
    val changePercent: Map<String, Double> = emptyMap(),
    val improvements: List<String> = emptyList(),
    val declines: List<String> = emptyList()
)

data class GoalComparison(
    val goals: Map<String, GoalProgress> = emptyMap(),
    val overallProgress: Double = 0.0,
    val projectedCompletionDates: Map<String, LocalDate> = emptyMap()
)

data class GoalProgress(
    val goalValue: Double = 0.0,
    val currentValue: Double = 0.0,
    val progressPercent: Double = 0.0,
    val trend: StatisticsTrendDirection = StatisticsTrendDirection.STABLE,
    val onTrack: Boolean = false
)

data class RegionalComparison(
    val region: String = "",
    val regionalAverages: Map<String, Double> = emptyMap(),
    val yourDog: Map<String, Double> = emptyMap(),
    val regionalTrends: List<String> = emptyList()
)

// Enums

enum class AnalyticsPeriod {
    WEEK,
    MONTH,
    QUARTER,
    YEAR,
    CUSTOM
}

enum class StatisticsTrendDirection {
    INCREASING,
    DECREASING,
    STABLE,
    VOLATILE
}

enum class CalorieBalance {
    DEFICIT,
    BALANCED,
    SURPLUS
}

enum class MuscleCondition {
    SEVERE_LOSS,
    MODERATE_LOSS,
    MILD_LOSS,
    NORMAL,
    WELL_DEVELOPED
}

enum class StatisticsActivityLevel {
    SEDENTARY,
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

enum class FoodMotivation {
    LOW,
    NORMAL,
    HIGH,
    EXCESSIVE
}

enum class TreatResponse {
    MINIMAL,
    MODERATE,
    HIGH,
    OBSESSIVE
}

enum class EatingSpeed {
    VERY_SLOW,
    SLOW,
    NORMAL,
    FAST,
    VERY_FAST
}

enum class BeggingLevel {
    NEVER,
    RARE,
    OCCASIONAL,
    FREQUENT,
    CONSTANT
}

enum class SocialEatingPattern {
    INDEPENDENT,
    PREFERS_COMPANY,
    COMPETITIVE,
    ANXIOUS
}

enum class ProteinQuality {
    LOW,
    MODERATE,
    HIGH,
    EXCELLENT
}

enum class FatQuality {
    LOW,
    MODERATE,
    HIGH,
    EXCELLENT
}

enum class CarbQuality {
    LOW,
    MODERATE,
    HIGH,
    EXCELLENT
}

enum class Vitamin {
    A, D, E, K, B1, B2, B3, B5, B6, B7, B9, B12, C
}

enum class Mineral {
    CALCIUM, PHOSPHORUS, MAGNESIUM, SODIUM, POTASSIUM, CHLORIDE,
    IRON, ZINC, COPPER, MANGANESE, SELENIUM, IODINE
}

enum class FattyAcid {
    OMEGA3, OMEGA6, DHA, EPA, ALA, LA
}

enum class AminoAcid {
    ARGININE, HISTIDINE, ISOLEUCINE, LEUCINE, LYSINE, METHIONINE,
    PHENYLALANINE, THREONINE, TRYPTOPHAN, VALINE
}

enum class NutrientStatus {
    DEFICIENT,
    SUBOPTIMAL,
    OPTIMAL,
    EXCESS
}

enum class HealthImpactLevel {
    NONE,
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}

enum class StatisticsRiskLevel {
    VERY_LOW,
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

enum class StatisticsRiskCategory {
    GENETIC,
    ENVIRONMENTAL,
    DIETARY,
    BEHAVIORAL,
    AGE_RELATED,
    MEDICAL
}

enum class HealthEventType {
    ROUTINE_CHECKUP,
    EMERGENCY,
    SURGERY,
    DIAGNOSIS,
    TREATMENT_START,
    TREATMENT_END,
    VACCINATION,
    INJURY,
    ILLNESS
}

enum class SeverityLevel {
    MILD,
    MODERATE,
    SEVERE,
    CRITICAL
}

enum class StatisticsDifficultyLevel {
    EASY,
    MODERATE,
    HARD
}

enum class StatisticsSeason {
    SPRING,
    SUMMER,
    FALL,
    WINTER
}

enum class ExerciseType {
    WALKING,
    RUNNING,
    SWIMMING,
    PLAYING,
    TRAINING,
    HIKING,
    AGILITY
}

enum class FoodTexture {
    DRY,
    WET,
    MIXED,
    RAW,
    SOFT
}

enum class FoodTemperature {
    COLD,
    ROOM_TEMP,
    WARM
}

enum class FlavorProfile {
    MEATY,
    FISHY,
    POULTRY,
    MIXED,
    VEGETABLE
}

enum class FrequencyLevel {
    NEVER,
    RARE,
    OCCASIONAL,
    FREQUENT,
    CONSTANT
}

enum class AlertType {
    INFO,
    WARNING,
    CRITICAL
}

enum class PriorityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class InterventionType {
    DIETARY,
    EXERCISE,
    MEDICAL,
    BEHAVIORAL,
    ENVIRONMENTAL
}

enum class UrgencyLevel {
    LOW,
    MODERATE,
    HIGH,
    IMMEDIATE
}

enum class LifeStage {
    PUPPY,
    ADOLESCENT,
    ADULT,
    SENIOR,
    GERIATRIC
}

enum class DevelopmentalStatus {
    DELAYED,
    SLIGHTLY_BEHIND,
    ON_TRACK,
    ADVANCED,
    EXCEPTIONAL
}

/**
 * Custom report builder
 */
data class CustomReport(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val reportType: ReportType = ReportType.STANDARD,
    val sections: List<ReportSection> = emptyList(),
    val filters: ReportFilters = ReportFilters(),
    val schedule: ReportSchedule? = null,
    val recipients: List<String> = emptyList(),
    val format: ReportFormat = ReportFormat.PDF
)

data class ReportSection(
    val id: String = "",
    val title: String = "",
    val type: SectionType = SectionType.CHART,
    val dataSource: StatisticsDataSource = StatisticsDataSource.WEIGHT,
    val visualization: VisualizationType = VisualizationType.LINE_CHART,
    val metrics: List<String> = emptyList(),
    val customization: SectionCustomization = SectionCustomization()
)

data class ReportFilters(
    val dateRange: DateRange = DateRange.LAST_30_DAYS,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val dogs: List<String> = emptyList(), // Empty = all dogs
    val categories: List<String> = emptyList()
)

data class ReportSchedule(
    val frequency: ScheduleFrequency = ScheduleFrequency.WEEKLY,
    val dayOfWeek: Int? = null, // 1-7
    val dayOfMonth: Int? = null, // 1-31
    val time: String = "09:00",
    val nextRunDate: LocalDateTime? = null,
    val isActive: Boolean = true
)

data class SectionCustomization(
    val colors: List<String> = emptyList(),
    val showLegend: Boolean = true,
    val showLabels: Boolean = true,
    val showTrendline: Boolean = false,
    val comparisonMode: ComparisonMode? = null,
    val aggregation: AggregationType = AggregationType.AVERAGE
)

enum class ReportType {
    STANDARD,
    DETAILED,
    SUMMARY,
    COMPARATIVE,
    PREDICTIVE
}

enum class SectionType {
    CHART,
    TABLE,
    METRIC,
    TEXT,
    HEATMAP,
    TIMELINE
}

enum class StatisticsDataSource {
    WEIGHT,
    NUTRITION,
    HEALTH,
    ACTIVITY,
    COST,
    BEHAVIOR,
    CUSTOM
}

enum class VisualizationType {
    LINE_CHART,
    BAR_CHART,
    PIE_CHART,
    DONUT_CHART,
    AREA_CHART,
    SCATTER_PLOT,
    HEATMAP,
    GAUGE,
    TIMELINE,
    TABLE
}

// DateRange moved to HealthModels.kt to avoid duplication

enum class ScheduleFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY
}

enum class ReportFormat {
    PDF,
    EXCEL,
    CSV,
    HTML,
    JSON
}

enum class ComparisonMode {
    PREVIOUS_PERIOD,
    YEAR_OVER_YEAR,
    GOAL,
    AVERAGE
}

enum class AggregationType {
    SUM,
    AVERAGE,
    MIN,
    MAX,
    COUNT
}