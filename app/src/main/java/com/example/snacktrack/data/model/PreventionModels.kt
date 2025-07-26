package com.example.snacktrack.data.model

import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Prevention tools models for proactive health management
 */

// Weight Management

data class WeightGoal(
    val id: String = "",
    val dogId: String = "",
    val targetWeight: Double = 0.0,
    val currentWeight: Double = 0.0,
    val startWeight: Double = 0.0,
    val targetDate: LocalDate = LocalDate.now().plusMonths(3),
    val startDate: LocalDate = LocalDate.now(),
    val goalType: WeightGoalType = WeightGoalType.MAINTAIN,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val strategy: WeightStrategy = WeightStrategy(),
    val progress: WeightProgress = WeightProgress(),
    val recommendations: List<PreventionRecommendation> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class WeightGoalType {
    LOSE_WEIGHT,
    GAIN_WEIGHT,
    MAINTAIN,
    BUILD_MUSCLE,
    SENIOR_MANAGEMENT
}

enum class GoalStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    COMPLETED,
    FAILED,
    REVISED
}

enum class PreventionActivityLevel {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

enum class PreventionActivityType {
    WALK,
    RUN,
    SWIM,
    PLAY,
    TRAINING,
    OTHER
}

data class WeightStrategy(
    val dailyCalorieTarget: Double = 0.0,
    val activityLevel: PreventionActivityLevel = PreventionActivityLevel.MODERATE,
    val feedingSchedule: FeedingSchedule = FeedingSchedule(),
    val treatAllowance: TreatAllowance = TreatAllowance(),
    val exercisePlan: ExercisePlan = ExercisePlan(),
    val adjustmentFactors: Map<String, Double> = emptyMap()
)

data class FeedingSchedule(
    val mealsPerDay: Int = 2,
    val mealTimes: List<String> = emptyList(),
    val portionSizes: Map<String, Double> = emptyMap(), // mealTime -> grams
    val foodMix: Map<String, Double> = emptyMap() // foodType -> percentage
)

data class TreatAllowance(
    val dailyCalories: Double = 0.0,
    val maxPercentOfDailyCalories: Double = 10.0,
    val approvedTreats: List<String> = emptyList(),
    val forbiddenTreats: List<String> = emptyList()
)

data class ExercisePlan(
    val dailyMinutes: Int = 30,
    val activities: List<PlannedActivity> = emptyList(),
    val intensityLevel: PreventionActivityLevel = PreventionActivityLevel.MODERATE,
    val restDays: List<String> = emptyList() // days of week
)

data class PlannedActivity(
    val type: PreventionActivityType = PreventionActivityType.WALK,
    val duration: Int = 0, // minutes
    val intensity: PreventionActivityLevel = PreventionActivityLevel.MODERATE,
    val frequency: String = "", // e.g., "daily", "3x per week"
    val notes: String? = null
)

data class WeightProgress(
    val percentComplete: Double = 0.0,
    val weightChange: Double = 0.0,
    val averageWeeklyChange: Double = 0.0,
    val projectedCompletionDate: LocalDate? = null,
    val isOnTrack: Boolean = true,
    val milestones: List<WeightMilestone> = emptyList()
)

data class WeightMilestone(
    val weight: Double = 0.0,
    val date: LocalDate = LocalDate.now(),
    val achieved: Boolean = false,
    val notes: String? = null
)

// Allergy Prevention

data class AllergyPrevention(
    val id: String = "",
    val dogId: String = "",
    val knownAllergens: List<KnownAllergen> = emptyList(),
    val suspectedAllergens: List<SuspectedAllergen> = emptyList(),
    val eliminationDiet: EliminationDiet? = null,
    val preventionProtocol: PreventionProtocol = PreventionProtocol(),
    val emergencyPlan: EmergencyPlan = EmergencyPlan(),
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

data class KnownAllergen(
    val allergen: String = "",
    val severity: PreventionAllergySeverity = PreventionAllergySeverity.MODERATE,
    val reactions: List<AllergyReaction> = emptyList(),
    val diagnosedBy: String? = null,
    val diagnosedDate: LocalDate? = null,
    val crossReactants: List<String> = emptyList()
)

enum class PreventionAllergySeverity {
    MILD,
    MODERATE,
    SEVERE,
    LIFE_THREATENING
}

data class AllergyReaction(
    val type: PreventionReactionType = PreventionReactionType.SKIN,
    val symptoms: List<String> = emptyList(),
    val onsetTime: String = "", // e.g., "immediate", "2-4 hours"
    val duration: String = "",
    val treatment: String? = null
)

enum class PreventionReactionType {
    SKIN,
    DIGESTIVE,
    RESPIRATORY,
    BEHAVIORAL,
    SYSTEMIC,
    ANAPHYLACTIC
}

data class SuspectedAllergen(
    val allergen: String = "",
    val suspicionLevel: SuspicionLevel = SuspicionLevel.POSSIBLE,
    val observedReactions: List<String> = emptyList(),
    val testingStatus: TestingStatus = TestingStatus.NOT_TESTED,
    val notes: String? = null
)

enum class SuspicionLevel {
    UNLIKELY,
    POSSIBLE,
    PROBABLE,
    VERY_LIKELY
}

enum class TestingStatus {
    NOT_TESTED,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    INCONCLUSIVE
}

data class EliminationDiet(
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val phase: DietPhase = DietPhase.ELIMINATION,
    val baseIngredients: List<String> = emptyList(),
    val eliminatedIngredients: List<String> = emptyList(),
    val reintroductionSchedule: List<ReintroductionItem> = emptyList(),
    val observations: List<DietObservation> = emptyList()
)

enum class DietPhase {
    ELIMINATION,
    REINTRODUCTION,
    CHALLENGE,
    MAINTENANCE
}

data class ReintroductionItem(
    val ingredient: String = "",
    val scheduledDate: LocalDate = LocalDate.now(),
    val actualDate: LocalDate? = null,
    val result: ReintroductionResult? = null,
    val notes: String? = null
)

enum class ReintroductionResult {
    NO_REACTION,
    MILD_REACTION,
    MODERATE_REACTION,
    SEVERE_REACTION,
    INCONCLUSIVE
}

data class DietObservation(
    val date: LocalDate = LocalDate.now(),
    val symptoms: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val overallCondition: ConditionRating = ConditionRating.NEUTRAL,
    val notes: String? = null
)

enum class ConditionRating {
    MUCH_WORSE,
    WORSE,
    NEUTRAL,
    BETTER,
    MUCH_BETTER
}

data class PreventionProtocol(
    val avoidanceStrategies: List<String> = emptyList(),
    val substitutionOptions: Map<String, List<String>> = emptyMap(), // allergen -> alternatives
    val environmentalControls: List<String> = emptyList(),
    val medicationSchedule: List<PreventiveMedication> = emptyList(),
    val monitoringFrequency: String = "weekly"
)

data class PreventiveMedication(
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val purpose: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

data class EmergencyPlan(
    val veterinarianContact: ContactInfo = ContactInfo(),
    val emergencyClinicContact: ContactInfo = ContactInfo(),
    val medications: List<EmergencyMedication> = emptyList(),
    val firstAidSteps: List<String> = emptyList(),
    val warningSignsToWatch: List<String> = emptyList()
)

data class ContactInfo(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val hours: String = "",
    val notes: String? = null
)

data class EmergencyMedication(
    val name: String = "",
    val dosage: String = "",
    val location: String = "",
    val expirationDate: LocalDate? = null,
    val administrationInstructions: String = ""
)

// Health Screening

data class HealthScreening(
    val id: String = "",
    val dogId: String = "",
    val screeningType: ScreeningType = ScreeningType.ROUTINE,
    val scheduledDate: LocalDate = LocalDate.now(),
    val completedDate: LocalDate? = null,
    val status: ScreeningStatus = ScreeningStatus.SCHEDULED,
    val tests: List<ScreeningTest> = emptyList(),
    val results: ScreeningResults? = null,
    val followUpActions: List<FollowUpAction> = emptyList(),
    val nextScreeningDate: LocalDate? = null
)

enum class ScreeningType {
    ROUTINE,
    BREED_SPECIFIC,
    AGE_BASED,
    SYMPTOM_TRIGGERED,
    PRE_BREEDING,
    SENIOR_PANEL,
    SPECIALIZED
}

enum class ScreeningStatus {
    SCHEDULED,
    REMINDER_SENT,
    IN_PROGRESS,
    COMPLETED,
    RESULTS_PENDING,
    FOLLOW_UP_NEEDED,
    CANCELLED
}

data class ScreeningTest(
    val name: String = "",
    val category: TestCategory = TestCategory.BLOOD_WORK,
    val purpose: String = "",
    val normalRange: String? = null,
    val frequency: String? = null,
    val cost: Double? = null
)

enum class TestCategory {
    BLOOD_WORK,
    URINE_ANALYSIS,
    IMAGING,
    GENETIC,
    PARASITE,
    DENTAL,
    CARDIAC,
    ORTHOPEDIC,
    NEUROLOGICAL,
    BEHAVIORAL
}

data class ScreeningResults(
    val overallStatus: PreventionHealthStatus = PreventionHealthStatus.NORMAL,
    val testResults: Map<String, TestResult> = emptyMap(),
    val abnormalFindings: List<AbnormalFinding> = emptyList(),
    val veterinarianNotes: String? = null,
    val recommendedActions: List<String> = emptyList()
)

enum class PreventionHealthStatus {
    EXCELLENT,
    NORMAL,
    MINOR_CONCERNS,
    MODERATE_CONCERNS,
    SERIOUS_CONCERNS,
    CRITICAL
}

data class TestResult(
    val value: String = "",
    val unit: String? = null,
    val normalRange: String? = null,
    val isNormal: Boolean = true,
    val interpretation: String? = null
)

data class AbnormalFinding(
    val test: String = "",
    val finding: String = "",
    val severity: FindingSeverity = FindingSeverity.MINOR,
    val recommendation: String = ""
)

enum class FindingSeverity {
    MINOR,
    MODERATE,
    SIGNIFICANT,
    SEVERE,
    CRITICAL
}

data class FollowUpAction(
    val action: String = "",
    val dueDate: LocalDate? = null,
    val priority: ActionPriority = ActionPriority.MEDIUM,
    val status: ActionStatus = ActionStatus.PENDING,
    val notes: String? = null
)

enum class ActionPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class ActionStatus {
    PENDING,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    OVERDUE
}

// Vaccination Management

data class VaccinationSchedule(
    val id: String = "",
    val dogId: String = "",
    val vaccines: List<Vaccine> = emptyList(),
    val customSchedule: Boolean = false,
    val reminders: List<VaccineReminder> = emptyList(),
    val complianceStatus: ComplianceStatus = ComplianceStatus(),
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

data class Vaccine(
    val name: String = "",
    val type: VaccineType = VaccineType.CORE,
    val doses: List<VaccineDose> = emptyList(),
    val manufacturer: String? = null,
    val protectionDuration: String = "",
    val sideEffects: List<String> = emptyList(),
    val contraindications: List<String> = emptyList()
)

enum class VaccineType {
    CORE,
    NON_CORE,
    LIFESTYLE,
    REGIONAL,
    TRAVEL
}

data class VaccineDose(
    val doseNumber: Int = 1,
    val scheduledDate: LocalDate = LocalDate.now(),
    val administeredDate: LocalDate? = null,
    val administeredBy: String? = null,
    val batchNumber: String? = null,
    val expirationDate: LocalDate? = null,
    val site: String? = null,
    val reaction: VaccineReaction? = null
)

data class VaccineReaction(
    val type: VaccineReactionType = VaccineReactionType.NONE,
    val symptoms: List<String> = emptyList(),
    val onsetTime: String = "",
    val duration: String = "",
    val treatment: String? = null,
    val reportedToManufacturer: Boolean = false
)

enum class VaccineReactionType {
    NONE,
    LOCAL_MILD,
    LOCAL_MODERATE,
    SYSTEMIC_MILD,
    SYSTEMIC_MODERATE,
    SEVERE,
    ANAPHYLACTIC
}

data class VaccineReminder(
    val vaccineId: String = "",
    val doseNumber: Int = 1,
    val reminderDate: LocalDate = LocalDate.now(),
    val reminderType: ReminderType = ReminderType.FIRST,
    val sent: Boolean = false,
    val acknowledged: Boolean = false
)

enum class ReminderType {
    FIRST,
    SECOND,
    FINAL,
    OVERDUE,
    URGENT
}

data class ComplianceStatus(
    val overallCompliance: Double = 100.0, // percentage
    val coreVaccineCompliance: Double = 100.0,
    val overdueVaccines: List<String> = emptyList(),
    val upcomingVaccines: List<String> = emptyList(),
    val lastReviewDate: LocalDate? = null
)

// Dental Care

data class DentalCare(
    val id: String = "",
    val dogId: String = "",
    val currentStatus: DentalStatus = DentalStatus(),
    val careRoutine: DentalCareRoutine = DentalCareRoutine(),
    val professionalCleanings: List<ProfessionalCleaning> = emptyList(),
    val homeCareLogs: List<HomeCareLog> = emptyList(),
    val preventionPlan: DentalPreventionPlan = DentalPreventionPlan()
)

data class DentalStatus(
    val overallHealth: DentalHealth = DentalHealth.GOOD,
    val plaqueLevel: PlaqueLevel = PlaqueLevel.MINIMAL,
    val gingivitisPresent: Boolean = false,
    val missingTeeth: List<String> = emptyList(),
    val problematicTeeth: List<ProblematicTooth> = emptyList(),
    val lastExamDate: LocalDate? = null
)

enum class DentalHealth {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    SEVERE
}

enum class PlaqueLevel {
    NONE,
    MINIMAL,
    MILD,
    MODERATE,
    HEAVY,
    SEVERE
}

data class ProblematicTooth(
    val toothId: String = "",
    val issue: DentalIssue = DentalIssue.PLAQUE,
    val severity: PreventionIssueSeverity = PreventionIssueSeverity.MILD,
    val treatmentNeeded: String? = null
)

enum class DentalIssue {
    PLAQUE,
    TARTAR,
    GINGIVITIS,
    PERIODONTAL,
    CAVITY,
    FRACTURE,
    ABSCESS,
    MALOCCLUSION
}

enum class PreventionIssueSeverity {
    MILD,
    MODERATE,
    SEVERE,
    CRITICAL
}

data class DentalCareRoutine(
    val brushingFrequency: String = "daily",
    val brushingProducts: List<String> = emptyList(),
    val dentalChews: List<DentalChew> = emptyList(),
    val waterAdditives: List<String> = emptyList(),
    val dietarySupport: List<String> = emptyList()
)

data class DentalChew(
    val name: String = "",
    val frequency: String = "",
    val size: String = "",
    val effectiveness: String? = null
)

data class ProfessionalCleaning(
    val date: LocalDate = LocalDate.now(),
    val performedBy: String = "",
    val findings: String = "",
    val proceduresDone: List<String> = emptyList(),
    val anesthesiaUsed: Boolean = true,
    val complications: String? = null,
    val cost: Double? = null,
    val nextRecommendedDate: LocalDate? = null
)

data class HomeCareLog(
    val date: LocalDate = LocalDate.now(),
    val careType: HomeCareType = HomeCareType.BRUSHING,
    val completed: Boolean = true,
    val notes: String? = null,
    val difficultyLevel: PreventionDifficultyLevel? = null
)

enum class HomeCareType {
    BRUSHING,
    DENTAL_CHEW,
    WATER_ADDITIVE,
    DENTAL_SPRAY,
    DENTAL_WIPE,
    OTHER
}

enum class PreventionDifficultyLevel {
    EASY,
    MODERATE,
    DIFFICULT,
    VERY_DIFFICULT,
    IMPOSSIBLE
}

data class DentalPreventionPlan(
    val goals: List<String> = emptyList(),
    val dailyRoutine: List<String> = emptyList(),
    val weeklyTasks: List<String> = emptyList(),
    val monthlyCheckpoints: List<String> = emptyList(),
    val warningSignsToMonitor: List<String> = emptyList()
)

// Risk Assessment

data class PreventionRiskAssessment(
    val id: String = "",
    val dogId: String = "",
    val assessmentDate: LocalDate = LocalDate.now(),
    val breedRisks: List<BreedSpecificRisk> = emptyList(),
    val ageRelatedRisks: List<AgeRelatedRisk> = emptyList(),
    val lifestyleRisks: List<LifestyleRisk> = emptyList(),
    val environmentalRisks: List<EnvironmentalRisk> = emptyList(),
    val overallRiskScore: Double = 0.0,
    val recommendations: List<RiskMitigation> = emptyList()
)

data class BreedSpecificRisk(
    val condition: String = "",
    val prevalence: Double = 0.0, // percentage
    val onsetAge: String = "",
    val screeningAvailable: Boolean = false,
    val preventable: Boolean = false,
    val managementStrategies: List<String> = emptyList()
)

data class AgeRelatedRisk(
    val condition: String = "",
    val currentAge: Int = 0,
    val riskAge: Int = 0,
    val riskLevel: PreventionRiskLevel = PreventionRiskLevel.LOW,
    val monitoringRequired: List<String> = emptyList(),
)

enum class PreventionRiskLevel {
    VERY_LOW,
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

data class LifestyleRisk(
    val factor: String = "",
    val currentStatus: String = "",
    val riskLevel: PreventionRiskLevel = PreventionRiskLevel.LOW,
    val modifiable: Boolean = true,
    val recommendations: List<String> = emptyList(),
)

data class EnvironmentalRisk(
    val hazard: String = "",
    val exposure: ExposureLevel = ExposureLevel.MINIMAL,
    val location: String = "",
    val mitigation: List<String> = emptyList()
)

enum class ExposureLevel {
    NONE,
    MINIMAL,
    LOW,
    MODERATE,
    HIGH,
    EXTREME
}

data class RiskMitigation(
    val risk: String = "",
    val strategy: String = "",
    val priority: MitigationPriority = MitigationPriority.MEDIUM,
    val timeline: String = "",
    val cost: CostLevel? = null,
    val effectiveness: String? = null
)

enum class MitigationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class CostLevel {
    FREE,
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

// Seasonal Care

data class SeasonalCare(
    val id: String = "",
    val dogId: String = "",
    val season: PreventionSeason = PreventionSeason.SPRING,
    val year: Int = LocalDate.now().year,
    val hazards: List<SeasonalHazard> = emptyList(),
    val preventiveMeasures: List<PreventiveMeasure> = emptyList(),
    val careAdjustments: CareAdjustments = CareAdjustments(),
    val reminders: List<SeasonalReminder> = emptyList()
)

enum class PreventionSeason {
    SPRING,
    SUMMER,
    FALL,
    WINTER
}

data class SeasonalHazard(
    val hazard: String = "",
    val riskPeriod: String = "",
    val severity: HazardSeverity = HazardSeverity.MODERATE,
    val symptoms: List<String> = emptyList(),
    val prevention: List<String> = emptyList()
)

enum class HazardSeverity {
    MINOR,
    MODERATE,
    SERIOUS,
    SEVERE,
    LIFE_THREATENING
}

data class PreventiveMeasure(
    val measure: String = "",
    val frequency: String = "",
    val products: List<String> = emptyList(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

data class CareAdjustments(
    val groomingChanges: List<String> = emptyList(),
    val exerciseModifications: List<String> = emptyList(),
    val dietaryAdjustments: List<String> = emptyList(),
    val shelterRequirements: List<String> = emptyList()
)

data class SeasonalReminder(
    val task: String = "",
    val dueDate: LocalDate = LocalDate.now(),
    val recurring: Boolean = false,
    val completed: Boolean = false,
    val notes: String? = null
)

data class PreventionSeasonalCare(
    val dogId: String = "",
    val season: PreventionSeason = PreventionSeason.SPRING,
    val year: Int = LocalDate.now().year,
    val hazards: List<SeasonalHazard> = emptyList(),
    val preventiveMeasures: List<PreventiveMeasure> = emptyList(),
    val careAdjustments: CareAdjustments = CareAdjustments(),
    val reminders: List<PreventionSeasonalReminder> = emptyList()
)

data class PreventionSeasonalReminder(
    val task: String = "",
    val dueDate: LocalDate = LocalDate.now(),
    val recurring: Boolean = false,
    val completed: Boolean = false,
    val notes: String? = null
)

// Prevention Recommendations - moved to HealthModels.kt to avoid duplication

enum class EvidenceLevel {
    ANECDOTAL,
    WEAK,
    MODERATE,
    STRONG,
    CONCLUSIVE
}

data class PreventionResource(
    val type: ResourceType = ResourceType.ARTICLE,
    val title: String = "",
    val url: String? = null,
    val author: String? = null,
    val cost: Double? = null
)

enum class ResourceType {
    ARTICLE,
    VIDEO,
    PRODUCT,
    SERVICE,
    BOOK,
    APP,
    TOOL
}

// Analytics

data class PreventionAnalytics(
    val id: String = "",
    val dogId: String = "",
    val period: LocalDateTime = LocalDateTime.now(),
    val complianceMetrics: ComplianceMetrics = ComplianceMetrics(),
    val healthImprovements: List<HealthImprovement> = emptyList(),
    val costBenefitAnalysis: CostBenefitAnalysis = CostBenefitAnalysis(),
    val trends: List<PreventionTrend> = emptyList()
)

data class ComplianceMetrics(
    val overallCompliance: Double = 0.0,
    val categoryCompliance: Map<PreventionCategory, Double> = emptyMap(),
    val taskCompletionRate: Double = 0.0,
    val consistencyScore: Double = 0.0,
    val improvementAreas: List<String> = emptyList()
)

data class HealthImprovement(
    val metric: String = "",
    val baseline: Double = 0.0,
    val current: Double = 0.0,
    val improvement: Double = 0.0,
    val unit: String = "",
    val significance: StatisticalSignificance = StatisticalSignificance.NOT_SIGNIFICANT
)

enum class StatisticalSignificance {
    NOT_SIGNIFICANT,
    MARGINALLY_SIGNIFICANT,
    SIGNIFICANT,
    HIGHLY_SIGNIFICANT
}

data class CostBenefitAnalysis(
    val totalCost: Double = 0.0,
    val estimatedSavings: Double = 0.0,
    val costsByCategory: Map<PreventionCategory, Double> = emptyMap(),
    val benefitScore: Double = 0.0,
    val returnOnInvestment: Double = 0.0
)

data class PreventionTrend(
    val metric: String = "",
    val direction: PreventionTrendDirection = PreventionTrendDirection.STABLE,
    val magnitude: Double = 0.0,
    val confidence: Double = 0.0,
    val projection: String? = null
)

enum class PreventionTrendDirection {
    IMPROVING,
    STABLE,
    DECLINING,
    VARIABLE
}