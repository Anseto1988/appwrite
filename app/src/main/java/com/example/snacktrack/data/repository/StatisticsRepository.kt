package com.example.snacktrack.data.repository

import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class StatisticsRepository(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : BaseRepository() {
    
    companion object {
        const val STATISTICS_COLLECTION_ID = "advanced_statistics"
        const val CUSTOM_REPORTS_COLLECTION_ID = "custom_reports"
        const val REPORT_SCHEDULES_COLLECTION_ID = "report_schedules"
        const val ANALYTICS_CACHE_COLLECTION_ID = "analytics_cache"
    }
    
    // Dependency repositories
    private val weightRepository = WeightRepository(context)
    private val nutritionRepository = NutritionRepository(context, appwriteService)
    private val healthRepository = HealthRepository(context, appwriteService)
    private val foodIntakeRepository = FoodIntakeRepository(context)
    private val dogRepository = DogRepository(context)
    
    // Generate comprehensive statistics
    suspend fun generateAdvancedStatistics(
        dogId: String,
        period: AnalyticsPeriod = AnalyticsPeriod.MONTHLY
    ): Result<AdvancedStatistics> = safeApiCall {
        val (startDate, endDate) = getDateRangeForPeriod(period)
        
        // Generate all analytics components in parallel
        val weightAnalytics = generateWeightAnalytics(dogId, startDate, endDate)
        val nutritionAnalytics = generateNutritionAnalytics(dogId, startDate, endDate)
        val healthAnalytics = generateHealthAnalytics(dogId, startDate, endDate)
        val activityAnalytics = generateActivityAnalytics(dogId, startDate, endDate)
        val costAnalytics = generateCostAnalytics(dogId, startDate, endDate)
        val behavioralAnalytics = generateBehavioralAnalytics(dogId, startDate, endDate)
        val predictiveInsights = generatePredictiveInsights(dogId, weightAnalytics, nutritionAnalytics, healthAnalytics)
        val comparativeAnalysis = generateComparativeAnalysis(dogId, startDate, endDate)
        
        val statistics = AdvancedStatistics(
            dogId = dogId,
            period = period,
            generatedAt = LocalDateTime.now(),
            weightAnalytics = weightAnalytics,
            nutritionAnalytics = nutritionAnalytics,
            healthAnalytics = healthAnalytics,
            activityAnalytics = activityAnalytics,
            costAnalytics = costAnalytics,
            behaviorAnalytics = behavioralAnalytics,
            predictiveInsights = predictiveInsights,
            comparativeAnalysis = comparativeAnalysis
        )
        
        // Cache the statistics
        cacheStatistics(statistics)
        
        statistics
    }
    
    // Weight Analytics
    private suspend fun generateWeightAnalytics(
        dogId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): WeightAnalytics {
        // Get weight entries for the date range
        val weightEntries = mutableListOf<WeightEntry>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dailyWeights = weightRepository.getWeightHistory(dogId).first().filter { 
                it.timestamp.toLocalDate() == currentDate 
            }
            weightEntries.addAll(dailyWeights)
            currentDate = currentDate.plusDays(1)
        }
        val weights = weightEntries
        
        if (weights.isEmpty()) {
            return WeightAnalytics()
        }
        
        val currentWeight = weights.maxByOrNull { it.timestamp }?.weight ?: 0.0
        val dog = dogRepository.getDogById(dogId).getOrNull()
        val idealWeight = dog?.targetWeight ?: calculateIdealWeight(dog)
        
        // Calculate trend
        val trend = calculateWeightTrend(weights)
        val changeAmount = if (weights.size >= 2) {
            currentWeight - weights.first().weight
        } else 0.0
        val changePercent = if (weights.first().weight > 0) {
            (changeAmount / weights.first().weight) * 100
        } else 0.0
        
        // Project future weight
        val projectedWeight = projectWeight(weights, 30)
        
        // Calculate days to ideal weight
        val daysToIdeal = if (trend != StatisticsTrendDirection.STABLE && idealWeight > 0) {
            val dailyChange = changeAmount / ChronoUnit.DAYS.between(startDate, endDate)
            if (dailyChange != 0.0) {
                ((idealWeight - currentWeight) / dailyChange).toInt()
            } else null
        } else null
        
        // Calculate weight velocity (kg per week)
        val velocity = if (weights.size >= 2) {
            val weeks = ChronoUnit.WEEKS.between(weights.first().timestamp, weights.last().timestamp).toDouble()
            if (weeks > 0) changeAmount / weeks else 0.0
        } else 0.0
        
        // Calculate variability (standard deviation)
        val weightValues = weights.map { it.weight }
        val variability = calculateStandardDeviation(weightValues)
        
        // Calculate consistency score (inverse of coefficient of variation)
        val consistencyScore = if (currentWeight > 0) {
            100.0 - (variability / currentWeight * 100).coerceIn(0.0, 100.0)
        } else 0.0
        
        return WeightAnalytics(
            currentWeight = currentWeight,
            idealWeight = idealWeight,
            weightTrend = trend,
            weightChangePercent = changePercent,
            projectedWeight = projectedWeight,
            weightVelocity = velocity,
            bodyConditionScore = calculateBodyConditionScore(currentWeight, idealWeight).toInt(),
            historicalData = weights.map { 
                WeightDataPoint(
                    date = it.timestamp.toLocalDate(),
                    weight = it.weight
                )
            },
            consistencyScore = consistencyScore
        )
    }
    
    // Nutrition Analytics
    private suspend fun generateNutritionAnalytics(
        dogId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): NutritionAnalytics {
        // Get food intakes for the date range
        val intakes = mutableListOf<FoodIntake>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dailyIntakes = foodIntakeRepository.getFoodIntakesForDog(dogId, currentDate).first()
            intakes.addAll(dailyIntakes)
            currentDate = currentDate.plusDays(1)
        }
        
        // Get nutrition analysis for the period
        val nutritionAnalyses = nutritionRepository.getNutritionAnalysisRange(dogId, startDate, endDate)
            .getOrNull() ?: emptyList()
        
        if (nutritionAnalyses.isEmpty()) {
            return NutritionAnalytics()
        }
        
        val dog = dogRepository.getDogById(dogId).getOrNull() ?: return NutritionAnalytics()
        
        // Calculate average daily calories
        val days = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        val totalCalories = intakes.sumOf { it.calories ?: 0 }
        val averageDailyCalories = totalCalories.toDouble() / days
        
        // Calculate recommended calories based on dog's characteristics
        val recommendedCalories = calculateRecommendedCalories(dog)
        
        // Determine calorie balance
        val calorieBalance = when {
            averageDailyCalories < recommendedCalories * 0.9 -> CalorieBalance.DEFICIT
            averageDailyCalories > recommendedCalories * 1.1 -> CalorieBalance.SURPLUS
            else -> CalorieBalance.BALANCED
        }
        
        // Calculate macronutrient breakdown
        val macroBreakdown = calculateMacronutrientBreakdown(intakes)
        
        // Calculate micronutrient analysis
        val microAnalysis = analyzeMicronutrients(intakes)
        
        // Calculate various scores
        val proteinQuality = evaluateProteinQuality(intakes)
        val dietDiversity = calculateDietDiversity(intakes)
        val mealRegularity = calculateMealRegularity(intakes)
        val treatPercentage = calculateTreatPercentage(intakes)
        
        // Estimate hydration
        val hydrationEstimate = estimateHydration(dog.weight, intakes)
        
        // Nutritional completeness
        val completeness = calculateNutritionalCompleteness(microAnalysis)
        
        // Identify deficiencies and excesses
        val deficiencies = identifyNutrientDeficiencies(microAnalysis, dog)
        val excesses = identifyNutrientExcesses(microAnalysis, dog)
        
        // Calculate adherence and portion control
        val scheduleAdherence = calculateFeedingScheduleAdherence(intakes)
        val portionControl = calculatePortionControlScore(intakes, recommendedCalories)
        
        return NutritionAnalytics(
            averageDailyCalories = averageDailyCalories,
            recommendedDailyCalories = recommendedCalories,
            calorieBalance = calorieBalance,
            macronutrientBreakdown = macroBreakdown,
            micronutrientAnalysis = microAnalysis,
            proteinQualityScore = proteinQuality,
            dietDiversityScore = dietDiversity,
            mealRegularityScore = mealRegularity,
            treatPercentage = treatPercentage,
            hydrationScore = hydrationEstimate,
            nutrientDeficiencies = deficiencies.take(5).map { it.nutrient },
            nutrientExcesses = excesses.take(5).map { it.nutrient },
            scheduleAdherence = scheduleAdherence,
            portionControlScore = portionControl
        )
    }
    
    // Health Analytics
    private suspend fun generateHealthAnalytics(
        dogId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): HealthAnalytics {
        val healthEntries = healthRepository.getHealthEntries(
            dogId, 
            startDate.atStartOfDay(), 
            endDate.plusDays(1).atStartOfDay()
        ).getOrNull() ?: emptyList()
        
        val medications = healthRepository.getActiveMedications(dogId)
            .getOrNull() ?: emptyList()
        
        val allergies = healthRepository.getAllergiesForDog(dogId)
            .getOrNull() ?: emptyList()
        
        // Calculate health score
        val healthScore = calculateOverallHealthScore(healthEntries, medications, allergies)
        
        // Determine health trend
        val healthTrend = analyzeHealthTrend(healthEntries)
        
        // Analyze symptom frequency
        val symptomFrequency = healthEntries
            .flatMap { it.symptoms }
            .groupBy { it.name } // Convert to String for compatibility
            .mapValues { it.value.size }
        
        // Calculate medication adherence
        val medicationAdherence = calculateMedicationAdherence(medications, healthEntries)
        
        // Get vaccine status
        val vaccineStatus = getVaccineStatus(dogId)
        
        // Calculate preventive care score
        val preventiveCareScore = calculatePreventiveCareScore(healthEntries, vaccineStatus)
        
        // Count vet visits
        val vetVisits = healthEntries.count { it.veterinaryVisit }
        val vetVisitFrequency = (vetVisits * 365) / ChronoUnit.DAYS.between(startDate, endDate).toInt()
        
        // Identify health risk factors
        val riskFactors = identifyHealthRiskFactors(dogId, healthEntries, allergies)
        
        // Calculate allergy management score
        val allergyManagementScore = calculateAllergyManagementScore(allergies, healthEntries)
        
        // Analyze chronic condition management
        val chronicConditions = analyzeChronicConditions(healthEntries, medications)
        
        // Calculate emergency readiness
        val emergencyReadiness = calculateEmergencyReadinessScore(dogId)
        
        // Create health event timeline
        val healthTimeline = createHealthEventTimeline(healthEntries)
        
        // Predict potential health issues
        val predictedIssues = predictHealthIssues(dogId, healthTimeline, riskFactors)
        
        return HealthAnalytics(
            healthScore = healthScore,
            healthTrend = healthTrend,
            symptomFrequency = symptomFrequency,
            medicationAdherence = medicationAdherence,
            vaccineStatus = vaccineStatus,
            preventiveCareScore = preventiveCareScore,
            vetVisitFrequency = vetVisitFrequency,
            healthRiskFactors = riskFactors,
            allergyManagementScore = allergyManagementScore,
            chronicConditionManagement = chronicConditions,
            healthTimeline = healthTimeline,
            predictedHealthIssues = predictedIssues
        )
    }
    
    // Activity Analytics
    private suspend fun generateActivityAnalytics(
        dogId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): ActivityAnalytics {
        // In a real implementation, this would fetch activity data from a fitness tracker or manual logs
        // For now, we'll use estimated values based on dog characteristics
        
        val dog = dogRepository.getDogById(dogId).getOrNull() ?: return ActivityAnalytics()
        
        // Estimate activity based on breed, age, and health
        val breed = getBreedInfo(dog.breed)
        val ageInYears = ChronoUnit.YEARS.between(dog.birthDate, LocalDate.now()).toInt()
        
        val recommendedActivity = calculateRecommendedActivity(breed, ageInYears, dog.weight)
        val estimatedActivity = estimateCurrentActivity(dog, breed)
        
        val activityLevel = when {
            estimatedActivity < recommendedActivity * 0.5 -> ActivityLevel.VERY_LOW
            estimatedActivity < recommendedActivity * 0.75 -> ActivityLevel.LOW
            estimatedActivity < recommendedActivity * 1.25 -> ActivityLevel.NORMAL
            estimatedActivity < recommendedActivity * 1.5 -> ActivityLevel.HIGH
            else -> ActivityLevel.VERY_HIGH
        }
        
        // Estimate other activity metrics
        val walkFrequency = estimateWalkFrequency(breed, ageInYears)
        val walkDuration = estimateWalkDuration(breed, dog.weight)
        val playTime = estimatePlayTime(ageInYears, breed)
        val trainingFrequency = estimateTrainingFrequency(ageInYears)
        
        // Calculate energy expenditure
        val energyExpenditure = calculateEnergyExpenditure(
            dog.weight,
            estimatedActivity,
            activityLevel
        )
        
        // Activity consistency (mock data for now)
        val activityConsistency = 75.0 // Would be calculated from daily activity logs
        
        // Training progress distribution
        val trainingProgress = mapOf(
            "Walking" to 50.0,
            "Playing" to 30.0,
            "Training" to 15.0,
            "Running" to 5.0
        )
        
        // Peak activity times (typical for dogs)
        val peakTimes = listOf(7, 8, 17, 18) // Morning and evening
        
        // Weather impact (mock data)
        val weatherImpact = 20.0 // 20% reduction in bad weather
        
        return ActivityAnalytics(
            dailyActivityMinutes = estimatedActivity,
            recommendedActivityMinutes = recommendedActivity,
            activityLevel = activityLevel.toStatisticsActivityLevel(),
            activityTrend = StatisticsTrendDirection.STABLE, // Would be calculated from historical data
            walkFrequency = walkFrequency,
            averageWalkDuration = walkDuration,
            playTimeMinutes = playTime,
            energyExpenditure = energyExpenditure,
            exerciseConsistency = activityConsistency,
            trainingProgress = trainingProgress,
            weatherImpact = weatherImpact
        )
    }
    
    // Cost Analytics
    private suspend fun generateCostAnalytics(
        dogId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): CostAnalytics {
        // This would integrate with purchase records and expense tracking
        // For now, we'll estimate based on food consumption and typical costs
        
        // Get food intakes for the date range
        val intakes = mutableListOf<FoodIntake>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dailyIntakes = foodIntakeRepository.getFoodIntakesForDog(dogId, currentDate).first()
            intakes.addAll(dailyIntakes)
            currentDate = currentDate.plusDays(1)
        }
        
        val dog = dogRepository.getDogById(dogId).getOrNull() ?: return CostAnalytics()
        
        // Estimate costs based on food consumption
        val foodCost = estimateFoodCost(intakes)
        val treatCost = estimateTreatCost(intakes)
        val supplementCost = estimateSupplementCost(dog)
        val medicationCost = estimateMedicationCost(dogId)
        val vetCost = estimateVetCost(dogId, startDate, endDate)
        val groomingCost = estimateGroomingCost(dog.breed)
        val accessoriesCost = estimateAccessoriesCost()
        
        val totalMonthly = foodCost + treatCost + supplementCost + medicationCost + 
                          vetCost + groomingCost + accessoriesCost
        
        val days = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        val dailyCost = totalMonthly * 12 / 365
        
        val costBreakdown = CostBreakdown(
            food = foodCost,
            treats = treatCost,
            healthcare = supplementCost + medicationCost + vetCost,
            grooming = groomingCost,
            toys = accessoriesCost,
            other = 0.0
        )
        
        // Calculate cost per kg of body weight
        val costPerKg = if (dog.weight > 0) totalMonthly / dog.weight else 0.0
        
        // Identify optimization opportunities
        val optimizations = identifyCostOptimizations(costBreakdown, intakes)
        
        // Price variation analysis (mock data)
        val priceVariations = analyzePriceVariations(intakes)
        
        // Calculate potential bulk savings
        val bulkSavings = calculateBulkPurchaseSavings(intakes)
        
        // Brand loyalty cost
        val brandLoyaltyCost = calculateBrandLoyaltyCost(intakes)
        
        // Seasonal variations
        val seasonalVariations = mapOf(
            StatisticsSeason.SPRING to totalMonthly * 0.95,
            StatisticsSeason.SUMMER to totalMonthly * 1.05,
            StatisticsSeason.FALL to totalMonthly * 1.0,
            StatisticsSeason.WINTER to totalMonthly * 1.1
        )
        
        return CostAnalytics(
            monthlyAverage = totalMonthly,
            yearlyProjection = totalMonthly * 12,
            costBreakdown = costBreakdown,
            costTrend = analyzeCostTrend(totalMonthly), // Would compare to historical data
            costOptimizations = optimizations,
            priceVariations = priceVariations,
            bulkPurchaseSavings = bulkSavings,
            loyaltyDiscounts = 0.0,
            brandLoyaltyCost = brandLoyaltyCost,
            seasonalCostAdjustment = seasonalVariations[StatisticsSeason.SUMMER] ?: totalMonthly
        )
    }
    
    // Behavioral Analytics
    private suspend fun generateBehavioralAnalytics(
        dogId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): BehaviorAnalytics {
        // Get food intakes for the date range
        val intakes = mutableListOf<FoodIntake>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dailyIntakes = foodIntakeRepository.getFoodIntakesForDog(dogId, currentDate).first()
            intakes.addAll(dailyIntakes)
            currentDate = currentDate.plusDays(1)
        }
        
        // Analyze eating patterns
        val eatingScore = analyzeEatingBehavior(intakes)
        val foodMotivation = analyzeFoodMotivation(intakes)
        val treatResponse = analyzeTreatResponse(intakes)
        val mealTimeConsistency = analyzeMealTimeConsistency(intakes)
        
        // Check for concerning behaviors (would need behavioral data)
        val aggressionIndicators = emptyList<AggressionIndicator>() // Mock
        
        // Analyze pickiness
        val pickeyScore = analyzePickiness(intakes)
        
        // Identify food preferences
        val preferences = identifyFoodPreferences(intakes)
        
        // Analyze eating speed
        val eatingSpeed = analyzeEatingSpeed(intakes)
        
        // Begging frequency (would need observation data)
        val beggingLevel = BeggingLevel.MODERATE // Mock
        
        // Stress indicators
        val stressIndicators = identifyStressEatingPatterns(intakes)
        
        // Social eating pattern
        val socialPattern = SocialEatingPattern.INDEPENDENT // Mock
        
        return BehaviorAnalytics(
            behaviorScore = eatingScore,
            eatingBehavior = eatingScore,
            foodMotivation = foodMotivation,
            treatResponse = treatResponse,
            mealTimeConsistency = mealTimeConsistency,
            pickiness = pickeyScore,
            foodPreferences = preferences,
            eatingSpeed = eatingSpeed,
            stressEatingPatterns = stressIndicators
        )
    }
    
    // Predictive Insights
    private suspend fun generatePredictiveInsights(
        dogId: String,
        weightAnalytics: WeightAnalytics,
        nutritionAnalytics: NutritionAnalytics,
        healthAnalytics: HealthAnalytics
    ): PredictiveInsights {
        // Weight predictions
        val weightPrediction = predictWeight(weightAnalytics)
        
        // Health predictions
        val healthPredictions = predictHealthIssues(
            dogId,
            healthAnalytics.healthTimeline,
            healthAnalytics.healthRiskFactors
        )
        
        // Nutrition predictions
        val nutritionPredictions = predictNutritionalIssues(nutritionAnalytics)
        
        // Cost predictions
        val costPrediction = predictCosts(dogId)
        
        // Behavior predictions
        val behaviorPredictions = predictBehavioralChanges(dogId)
        
        // Risk assessment
        val riskAssessment = assessOverallRisk(
            weightAnalytics,
            nutritionAnalytics,
            healthAnalytics
        )
        
        // Recommended interventions
        val interventions = recommendInterventions(
            riskAssessment,
            weightAnalytics,
            nutritionAnalytics,
            healthAnalytics
        )
        
        // Life stage transition
        val transition = predictLifestageTransition(dogId)
        
        return PredictiveInsights(
            weightPrediction = weightPrediction,
            healthPredictions = healthPredictions,
            costPrediction = costPrediction,
            behaviorPredictions = behaviorPredictions
        )
    }
    
    // Comparative Analysis
    private suspend fun generateComparativeAnalysis(
        dogId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): ComparativeAnalysis {
        val dog = dogRepository.getDogById(dogId).getOrNull() ?: return ComparativeAnalysis()
        
        // Compare to breed averages
        val breedComparison = compareToBreedAverages(dog)
        
        // Compare to age group
        val ageComparison = compareToAgeGroup(dog)
        
        // Compare to similar dogs
        val similarComparison = compareToSimilarDogs(dog)
        
        // Historical comparison
        val historicalComparison = compareToHistoricalData(dogId, startDate, endDate)
        
        // Goal comparison
        val goalComparison = compareToGoals(dogId)
        
        // Regional comparison (if location data available)
        val regionalComparison = null // Would require location data
        
        return ComparativeAnalysis(
            breedComparison = breedComparison,
            ageGroupComparison = ageComparison,
            localAreaComparison = LocalAreaComparison(), // Would require location data
            similarDogsComparison = similarComparison
        )
    }
    
    // Custom Reports
    
    suspend fun createCustomReport(report: CustomReport): Result<CustomReport> = safeApiCall {
        val data = mapOf(
            "name" to report.name,
            "description" to report.description,
            "createdBy" to report.createdBy,
            "createdAt" to report.createdAt.toString(),
            "reportType" to report.reportType.name,
            "sections" to report.sections.map { section ->
                mapOf(
                    "id" to section.id,
                    "title" to section.title,
                    "type" to section.type.name,
                    "dataSource" to section.dataSource.name,
                    "visualization" to section.visualization.name,
                    "metrics" to section.metrics,
                    "customization" to mapOf(
                        "colors" to section.customization.colors,
                        "fontSize" to section.customization.fontSize,
                        "showLegend" to section.customization.showLegend,
                        "showDataLabels" to section.customization.showDataLabels
                    )
                )
            },
            "filters" to mapOf(
                "dateRange" to report.filters.dateRange,
                "categories" to report.filters.categories,
                "metrics" to report.filters.metrics,
                "dogs" to report.filters.dogs,
                "comparison" to report.filters.comparison
            ),
            "schedule" to report.schedule?.let {
                mapOf(
                    "frequency" to it.frequency,
                    "dayOfWeek" to it.dayOfWeek,
                    "dayOfMonth" to it.dayOfMonth,
                    "time" to it.time,
                    "nextRunDate" to it.nextRunDate?.toString(),
                    "isActive" to it.isActive
                )
            }
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = CUSTOM_REPORTS_COLLECTION_ID,
            documentId = io.appwrite.ID.unique(),
            data = data
        )
        
        // Schedule report if needed
        report.schedule?.let {
            scheduleReport(document.id, it)
        }
        
        documentToCustomReport(document)
    }
    
    suspend fun getCustomReports(userId: String): Result<List<CustomReport>> = safeApiCall {
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = CUSTOM_REPORTS_COLLECTION_ID,
            queries = listOf(Query.equal("createdBy", userId))
        )
        
        response.documents.map { documentToCustomReport(it) }
    }
    
    suspend fun generateReport(reportId: String): Result<ByteArray> = safeApiCall {
        val report = getCustomReportById(reportId).getOrNull()
            ?: throw Exception("Report not found")
        
        // Generate report data based on configuration
        val reportData = collectReportData(report)
        
        // Format report based on selected format
        return@safeApiCall when (report.schedule?.format ?: ReportFormat.PDF) {
            ReportFormat.PDF -> generatePdfReport(report, reportData)
            ReportFormat.EXCEL -> generateExcelReport(report, reportData)
            ReportFormat.CSV -> generateCsvReport(report, reportData)
            ReportFormat.HTML -> generateHtmlReport(report, reportData)
            ReportFormat.JSON -> generateJsonReport(report, reportData)
        }
    }
    
    // Helper functions
    
    private fun getDateRangeForPeriod(period: AnalyticsPeriod): Pair<LocalDate, LocalDate> {
        val endDate = LocalDate.now()
        val startDate = when (period) {
            AnalyticsPeriod.DAILY -> endDate.minusDays(1)
            AnalyticsPeriod.WEEKLY -> endDate.minusDays(7)
            AnalyticsPeriod.MONTHLY -> endDate.minusMonths(1)
            AnalyticsPeriod.QUARTERLY -> endDate.minusMonths(3)
            AnalyticsPeriod.YEARLY -> endDate.minusYears(1)
            AnalyticsPeriod.CUSTOM -> endDate.minusMonths(1) // Default to month
        }
        return startDate to endDate
    }
    
    private fun calculateWeightTrend(weights: List<WeightEntry>): StatisticsTrendDirection {
        if (weights.size < 2) return StatisticsTrendDirection.STABLE
        
        val recentWeights = weights.takeLast(5)
        val slope = calculateLinearRegression(
            recentWeights.map { ChronoUnit.DAYS.between(recentWeights.first().timestamp, it.timestamp).toDouble() },
            recentWeights.map { it.weight }
        )
        
        return when {
            slope > 0.01 -> StatisticsTrendDirection.INCREASING
            slope < -0.01 -> StatisticsTrendDirection.DECREASING
            else -> StatisticsTrendDirection.STABLE
        }
    }
    
    private fun calculateLinearRegression(x: List<Double>, y: List<Double>): Double {
        val n = x.size
        if (n < 2) return 0.0
        
        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.zip(y).sumOf { it.first * it.second }
        val sumX2 = x.sumOf { it * it }
        
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
    }
    
    private fun calculateStandardDeviation(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        
        val mean = values.average()
        val variance = values.map { (it - mean).pow(2) }.average()
        return sqrt(variance)
    }
    
    private fun projectWeight(weights: List<WeightEntry>, daysAhead: Int): Double {
        if (weights.size < 2) return weights.lastOrNull()?.weight ?: 0.0
        
        val x = weights.map { ChronoUnit.DAYS.between(weights.first().timestamp, it.timestamp).toDouble() }
        val y = weights.map { it.weight }
        
        val slope = calculateLinearRegression(x, y)
        val intercept = y.average() - slope * x.average()
        
        val projectedDay = x.last() + daysAhead
        return slope * projectedDay + intercept
    }
    
    private suspend fun getDogById(dogId: String): Dog? {
        return dogRepository.getDogById(dogId).getOrNull()
    }
    
    private fun calculateIdealWeight(dog: Dog?): Double {
        // This would calculate based on breed, age, and body condition
        return dog?.targetWeight ?: 0.0
    }
    
    private fun calculateBodyConditionScore(currentWeight: Double, idealWeight: Double): Double {
        if (idealWeight <= 0) return 5.0
        
        val ratio = currentWeight / idealWeight
        return when {
            ratio < 0.8 -> 3.0
            ratio < 0.9 -> 4.0
            ratio < 1.1 -> 5.0
            ratio < 1.2 -> 6.0
            ratio < 1.3 -> 7.0
            else -> 8.0
        }
    }
    
    private fun estimateMuscleCondition(dog: Dog?, weights: List<WeightEntry>): MuscleCondition {
        // This would analyze weight changes and activity levels
        return MuscleCondition.NORMAL
    }
    
    private fun calculateRecommendedCalories(dog: Dog): Double {
        // RER (Resting Energy Requirement) = 70 * (weight in kg)^0.75
        val rer = 70 * dog.weight.pow(0.75)
        
        // Activity factor based on age and activity level
        val activityFactor = 1.6 // Average adult dog
        
        return rer * activityFactor
    }
    
    private fun calculateMacronutrientBreakdown(intakes: List<FoodIntake>): MacronutrientBreakdown {
        // This would analyze the nutritional content of consumed foods
        return MacronutrientBreakdown(
            proteinPercent = 25.0,
            fatPercent = 15.0,
            carbPercent = 50.0,
            fiberPercent = 5.0
        )
    }
    
    private fun analyzeMicronutrients(intakes: List<FoodIntake>): MicronutrientAnalysis {
        // This would analyze vitamin and mineral content
        return MicronutrientAnalysis()
    }
    
    private fun evaluateProteinQuality(intakes: List<FoodIntake>): Double {
        // This would evaluate amino acid profiles and digestibility
        return 75.0
    }
    
    private fun calculateDietDiversity(intakes: List<FoodIntake>): Double {
        val uniqueFoods = intakes.map { it.foodId }.distinct().size
        val totalMeals = intakes.size
        
        return if (totalMeals > 0) {
            (uniqueFoods.toDouble() / totalMeals * 100).coerceIn(0.0, 100.0)
        } else 0.0
    }
    
    private fun calculateMealRegularity(intakes: List<FoodIntake>): Double {
        // This would analyze meal timing consistency
        return 80.0
    }
    
    private fun calculateTreatPercentage(intakes: List<FoodIntake>): Double {
        // TODO: Add mealType to FoodIntake model to differentiate treats/snacks
        val treatCalories = 0.0 // Currently unable to distinguish treats without mealType
        
        val totalCalories = intakes.sumOf { it.calories ?: 0 }
        
        return if (totalCalories > 0) {
            (treatCalories / totalCalories * 100)
        } else 0.0
    }
    
    private fun estimateHydration(weight: Double, intakes: List<FoodIntake>): Double {
        // Dogs need approximately 50-60 ml of water per kg of body weight
        val recommendedWater = weight * 55 // ml per day
        
        // Estimate water from food
        val waterFromFood = intakes.sumOf { it.amountGram * 0.1 } // Assume 10% moisture
        
        return waterFromFood / weight
    }
    
    private fun calculateNutritionalCompleteness(analysis: MicronutrientAnalysis): Double {
        // This would check if all essential nutrients are at optimal levels
        return 85.0
    }
    
    private fun identifyNutrientDeficiencies(
        analysis: MicronutrientAnalysis,
        dog: Dog
    ): List<NutrientDeficiency> {
        // This would compare nutrient levels to requirements
        return emptyList()
    }
    
    private fun identifyNutrientExcesses(
        analysis: MicronutrientAnalysis,
        dog: Dog
    ): List<NutrientExcess> {
        // This would identify nutrients above safe levels
        return emptyList()
    }
    
    private fun calculateFeedingScheduleAdherence(intakes: List<FoodIntake>): Double {
        // This would analyze how well feeding times match schedule
        return 90.0
    }
    
    private fun calculatePortionControlScore(
        intakes: List<FoodIntake>,
        recommendedCalories: Double
    ): Double {
        val dailyCalories = intakes
            .groupBy { it.timestamp.toLocalDate() }
            .mapValues { entry -> entry.value.sumOf { it.calories ?: 0 } }
        
        val deviations = dailyCalories.values.map { 
            abs(it - recommendedCalories) / recommendedCalories 
        }
        
        val averageDeviation = deviations.average()
        
        return (100 * (1 - averageDeviation)).coerceIn(0.0, 100.0)
    }
    
    private fun calculateOverallHealthScore(
        entries: List<DogHealthEntry>,
        medications: List<DogMedication>,
        allergies: List<DogAllergy>
    ): Double {
        var score = 100.0
        
        // Deduct for health issues
        score -= entries.size * 2
        
        // Deduct for chronic medications
        score -= medications.filter { it.isActive }.size * 5
        
        // Deduct for allergies based on severity
        allergies.forEach { allergy ->
            score -= when (allergy.severity) {
                DogAllergySeverity.MILD -> 2
                DogAllergySeverity.MODERATE -> 5
                DogAllergySeverity.SEVERE -> 10
                DogAllergySeverity.CRITICAL -> 15
            }
        }
        
        return score.coerceIn(0.0, 100.0)
    }
    
    private fun analyzeHealthTrend(entries: List<DogHealthEntry>): StatisticsTrendDirection {
        if (entries.size < 2) return StatisticsTrendDirection.STABLE
        
        val recentEntries = entries.takeLast(10)
        val firstHalf = recentEntries.take(recentEntries.size / 2)
        val secondHalf = recentEntries.drop(recentEntries.size / 2)
        
        val firstHalfSeverity = firstHalf.count { it.veterinaryVisit }
        val secondHalfSeverity = secondHalf.count { it.veterinaryVisit }
        
        return when {
            secondHalfSeverity > firstHalfSeverity * 1.5 -> StatisticsTrendDirection.DECREASING
            secondHalfSeverity < firstHalfSeverity * 0.7 -> StatisticsTrendDirection.INCREASING
            else -> StatisticsTrendDirection.STABLE
        }
    }
    
    private fun calculateMedicationAdherence(
        medications: List<DogMedication>,
        entries: List<DogHealthEntry>
    ): Double {
        // This would check medication logs against prescriptions
        return 95.0
    }
    
    private suspend fun getVaccineStatus(dogId: String): VaccineStatus {
        // This would fetch vaccination records
        return VaccineStatus(
            upToDate = true,
            overdue = emptyList(),
            upcoming = listOf("Rabies (${LocalDate.now().plusMonths(6)})")
        )
    }
    
    private fun calculatePreventiveCareScore(
        entries: List<DogHealthEntry>,
        vaccineStatus: VaccineStatus
    ): Double {
        var score = 100.0
        
        if (!vaccineStatus.upToDate) score -= 30
        if (vaccineStatus.overdue.isNotEmpty()) score -= (10 * vaccineStatus.overdue.size)
        
        // Check for regular checkups
        val checkups = entries.count { it.notes.contains("checkup", ignoreCase = true) }
        if (checkups < 2) score -= 20 // Less than 2 checkups per year
        
        return score.coerceIn(0.0, 100.0)
    }
    
    private fun identifyHealthRiskFactors(
        dogId: String,
        entries: List<DogHealthEntry>,
        allergies: List<DogAllergy>
    ): List<HealthRiskFactor> {
        val riskFactors = mutableListOf<HealthRiskFactor>()
        
        // Add allergy-related risks
        allergies.forEach { allergy ->
            riskFactors.add(
                HealthRiskFactor(
                    factor = "Allergie: ${allergy.allergen}",
                    riskLevel = when (allergy.severity) {
                        DogAllergySeverity.MILD -> RiskLevel.LOW
                        DogAllergySeverity.MODERATE -> RiskLevel.MEDIUM
                        DogAllergySeverity.SEVERE -> RiskLevel.HIGH
                        DogAllergySeverity.CRITICAL -> RiskLevel.HIGH
                    },
                    description = "Schweregrad: ${allergy.severity.name}. ${if (allergy.severity == DogAllergySeverity.SEVERE) "Überwachung erforderlich" else "Regelmäßige Kontrolle"}",
                    recommendations = listOf("Allergen vermeiden", "Medikamente bereithalten")
                )
            )
        }
        
        return riskFactors
    }
    
    private fun calculateAllergyManagementScore(
        allergies: List<DogAllergy>,
        entries: List<DogHealthEntry>
    ): Double {
        if (allergies.isEmpty()) return 100.0
        
        val recentReactions = entries.count { entry ->
            entry.symptoms.any { symptom ->
                symptom == HealthSymptom.ITCHING || 
                symptom == HealthSymptom.RASH ||
                symptom == HealthSymptom.HOT_SPOTS
            }
        }
        
        val baseScore = 100.0
        val deduction = recentReactions * 10.0
        
        return (baseScore - deduction).coerceIn(0.0, 100.0)
    }
    
    private fun analyzeChronicConditions(
        entries: List<DogHealthEntry>,
        medications: List<DogMedication>
    ): Map<String, Double> {
        // This would identify and track chronic conditions
        // Returns condition name to management score (0-100)
        return emptyMap()
    }
    
    private fun calculateEmergencyReadinessScore(dogId: String): Double {
        // This would check for emergency contacts, first aid knowledge, etc.
        return 75.0
    }
    
    private fun createHealthEventTimeline(entries: List<DogHealthEntry>): List<HealthEvent> {
        return entries.map { entry ->
            HealthEvent(
                date = entry.entryDate.toLocalDate(),
                eventType = if (entry.veterinaryVisit) {
                    HealthEventType.ROUTINE_CHECKUP
                } else {
                    HealthEventType.ILLNESS
                },
                description = entry.symptoms.joinToString(", "),
                severity = if (entry.veterinaryVisit) {
                    SeverityLevel.MODERATE
                } else {
                    SeverityLevel.MILD
                },
                outcome = entry.notes,
                treatmentGiven = if (entry.veterinaryVisit) "Tierärztliche Behandlung" else null
            )
        }
    }
    
    private fun predictHealthIssues(
        dogId: String,
        timeline: List<HealthEvent>,
        riskFactors: List<HealthRiskFactor>
    ): List<PredictedHealthIssue> {
        // This would use ML models to predict health issues
        return emptyList()
    }
    
    private suspend fun getBreedInfo(breed: String): BreedInfo? {
        // This would fetch breed information
        return null
    }
    
    private fun calculateRecommendedActivity(
        breed: BreedInfo?,
        age: Int,
        weight: Double
    ): Double {
        // Base activity recommendation
        var minutes = 60.0
        
        // Adjust for breed
        breed?.let {
            minutes *= when (it.energyLevel) {
                1, 2 -> 0.5
                3 -> 0.75
                4 -> 1.0
                5 -> 1.25
                else -> 1.0
            }
        }
        
        // Adjust for age
        minutes *= when {
            age < 1 -> 0.5  // Puppies need less sustained exercise
            age < 2 -> 0.75
            age < 7 -> 1.0
            age < 10 -> 0.75
            else -> 0.5
        }
        
        return minutes
    }
    
    private fun estimateCurrentActivity(dog: Dog, breed: BreedInfo?): Double {
        // This would use activity tracking data
        return 45.0 // Mock data
    }
    
    private fun estimateWalkFrequency(breed: BreedInfo?, age: Int): Int {
        return when {
            age < 1 -> 21 // 3 times daily for puppies
            age > 10 -> 7 // Once daily for seniors
            else -> 14 // Twice daily for adults
        }
    }
    
    private fun estimateWalkDuration(breed: BreedInfo?, weight: Double): Double {
        return when {
            weight < 10 -> 20.0
            weight < 25 -> 30.0
            weight < 40 -> 45.0
            else -> 30.0
        }.coerceIn(15.0, 60.0)
    }
    
    private fun estimatePlayTime(age: Int, breed: BreedInfo?): Double {
        return when {
            age < 2 -> 30.0
            age < 7 -> 20.0
            else -> 10.0
        }
    }
    
    private fun estimateTrainingFrequency(age: Int): Double {
        return when {
            age < 1 -> 7.0 // Daily for puppies
            age < 3 -> 3.5 // Every other day for young dogs
            else -> 2.0 // Twice a week for adults
        }
    }
    
    private fun calculateEnergyExpenditure(
        weight: Double,
        activityMinutes: Double,
        level: ActivityLevel
    ): Double {
        // MET (Metabolic Equivalent) values for dogs
        val met = when (level) {
            ActivityLevel.VERY_LOW -> 1.2
            ActivityLevel.LOW -> 1.5
            ActivityLevel.NORMAL -> 2.0
            ActivityLevel.HIGH -> 3.0
            ActivityLevel.VERY_HIGH -> 4.0
        }
        
        // Calories = MET * weight(kg) * time(hours)
        return met * weight * (activityMinutes / 60)
    }
    
    private fun calculateRestQuality(level: ActivityLevel): Double {
        return when (level) {
            ActivityLevel.VERY_LOW -> 60.0
            ActivityLevel.LOW -> 70.0
            ActivityLevel.NORMAL -> 85.0
            ActivityLevel.HIGH -> 80.0
            ActivityLevel.VERY_HIGH -> 70.0
        }
    }
    
    private fun estimateFoodCost(intakes: List<FoodIntake>): Double {
        // Estimate based on average food prices
        val totalKg = intakes.sumOf { it.amountGram } / 1000
        val pricePerKg = 5.0 // Average price
        return totalKg * pricePerKg / 30 // Monthly cost
    }
    
    private fun estimateTreatCost(intakes: List<FoodIntake>): Double {
        // TODO: Add mealType to FoodIntake model to filter treats
        val treatIntakes = emptyList<FoodIntake>() // Currently unable to filter treats
        val totalKg = treatIntakes.sumOf { it.amountGram } / 1000
        val pricePerKg = 10.0 // Treats are more expensive
        return totalKg * pricePerKg / 30
    }
    
    private fun estimateSupplementCost(dog: Dog): Double {
        // Basic supplement cost estimate
        return 20.0 // Monthly average
    }
    
    private suspend fun estimateMedicationCost(dogId: String): Double {
        // This would calculate based on active medications
        return 30.0 // Monthly average
    }
    
    private suspend fun estimateVetCost(
        dogId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double {
        // Annual vet costs divided by 12
        return 600.0 / 12 // €50/month average
    }
    
    private fun estimateGroomingCost(breed: String): Double {
        // Based on breed grooming requirements
        return 40.0 // Monthly average
    }
    
    private fun estimateAccessoriesCost(): Double {
        // Toys, beds, leashes, etc. amortized monthly
        return 15.0
    }
    
    private fun analyzeCostTrend(currentCost: Double): StatisticsTrendDirection {
        // This would compare to historical costs
        return StatisticsTrendDirection.STABLE
    }
    
    private fun identifyCostOptimizations(
        breakdown: CostBreakdown,
        intakes: List<FoodIntake>
    ): List<CostOptimization> {
        val optimizations = mutableListOf<CostOptimization>()
        
        // Check if bulk buying could save money
        if (breakdown.food > 50) {
            optimizations.add(
                CostOptimization(
                    category = "Futter",
                    currentCost = breakdown.food,
                    optimizedCost = breakdown.food * 0.85,
                    savingsAmount = breakdown.food * 0.15,
                    savingsPercent = 15.0,
                    recommendation = "Größere Mengen kaufen für 15% Rabatt",
                    implementationDifficulty = DifficultyLevel.EASY
                )
            )
        }
        
        return optimizations
    }
    
    private fun analyzePriceVariations(intakes: List<FoodIntake>): Map<String, PriceVariation> {
        // This would track price changes over time
        return emptyMap()
    }
    
    private fun calculateBulkPurchaseSavings(intakes: List<FoodIntake>): Double {
        // Estimate savings from buying in bulk
        val monthlyConsumption = intakes.sumOf { it.amountGram } / 1000
        return if (monthlyConsumption > 10) {
            monthlyConsumption * 0.5 // €0.50 per kg saved
        } else 0.0
    }
    
    private fun calculateBrandLoyaltyCost(intakes: List<FoodIntake>): Double {
        // Premium brand cost vs generic
        return 10.0 // €10/month extra for premium brands
    }
    
    private fun analyzeEatingBehavior(intakes: List<FoodIntake>): Double {
        // Score based on consistency, portion control, etc.
        return 75.0
    }
    
    private fun analyzeFoodMotivation(intakes: List<FoodIntake>): FoodMotivation {
        // Based on eating patterns and treat response
        return FoodMotivation.NORMAL
    }
    
    private fun analyzeTreatResponse(intakes: List<FoodIntake>): TreatResponse {
        // TODO: Add mealType to FoodIntake model to calculate treat percentage
        val treatPercentage = 0.0 // Currently unable to distinguish treats
        
        return when {
            treatPercentage < 5 -> TreatResponse.MINIMAL
            treatPercentage < 15 -> TreatResponse.MODERATE
            treatPercentage < 25 -> TreatResponse.HIGH
            else -> TreatResponse.OBSESSIVE
        }
    }
    
    private fun analyzeMealTimeConsistency(intakes: List<FoodIntake>): Double {
        // Analyze how consistent meal times are
        return 80.0
    }
    
    private fun analyzePickiness(intakes: List<FoodIntake>): Double {
        // Check variety acceptance
        val uniqueFoods = intakes.map { it.foodId }.distinct().size
        return if (uniqueFoods < 3) 80.0 else 20.0
    }
    
    private fun identifyFoodPreferences(intakes: List<FoodIntake>): List<FoodPreference> {
        // Analyze which foods are consumed most eagerly
        return emptyList()
    }
    
    private fun analyzeEatingSpeed(intakes: List<FoodIntake>): EatingSpeed {
        // This would need feeding duration data
        return EatingSpeed.NORMAL
    }
    
    private fun identifyStressEatingPatterns(intakes: List<FoodIntake>): List<StressIndicator> {
        // Look for irregular eating patterns
        return emptyList()
    }
    
    private fun predictWeight(analytics: WeightAnalytics): WeightPrediction {
        return WeightPrediction(
            predictedWeight30Days = analytics.projectedWeight,
            predictedWeight90Days = projectWeightLongTerm(analytics, 90),
            confidenceLevel = calculateConfidence(analytics.consistencyScore).toFloat(),
            assumptions = listOf(
                "Aktuelle Fütterung bleibt gleich",
                "Aktivitätslevel bleibt konstant"
            ),
            scenarioAnalysis = mapOf(
                "10% weniger Futter" to analytics.projectedWeight * 0.97,
                "10% mehr Aktivität" to analytics.projectedWeight * 0.98,
                "Beides kombiniert" to analytics.projectedWeight * 0.95
            )
        )
    }
    
    private fun projectWeightLongTerm(analytics: WeightAnalytics, days: Int): Double {
        val currentWeight = analytics.currentWeight
        val velocity = analytics.weightVelocity * (days / 7.0)
        
        return (currentWeight + velocity).coerceIn(
            currentWeight * 0.8,
            currentWeight * 1.2
        )
    }
    
    private fun calculateConfidence(consistencyScore: Double): Double {
        return consistencyScore * 0.9 // 90% of consistency score
    }
    
    private fun predictNutritionalIssues(analytics: NutritionAnalytics): List<PredictedHealthIssue> {
        val predictions = mutableListOf<PredictedHealthIssue>()
        
        // Check for developing deficiencies
        analytics.nutrientDeficiencies.forEach { deficiency ->
            predictions.add(
                PredictedHealthIssue(
                    condition = "Nährstoffmangel: $deficiency",
                    probability = 0.7,
                    timeframe = "30-60 Tage",
                    preventiveMeasures = listOf(
                        "Nahrungsergänzung hinzufügen",
                        "Ernährung anpassen",
                        "Tierarzt konsultieren"
                    ),
                    riskFactors = listOf("Unausgewogene Ernährung")
                )
            )
        }
        
        return predictions
    }
    
    
    private suspend fun predictCosts(dogId: String): CostPrediction {
        // Simple linear projection for now
        val currentMonthlyCost = 200.0 // Would get from cost analytics
        
        return CostPrediction(
            next30DaysCost = currentMonthlyCost,
            next90DaysCost = currentMonthlyCost * 3,
            annualProjection = currentMonthlyCost * 12 * 1.05, // 5% inflation
            inflationAdjustedCost = currentMonthlyCost * 12 * 1.1, // 10% inflation worst case
            budgetAlerts = emptyList()
        )
    }
    
    private suspend fun predictBehavioralChanges(dogId: String): List<BehaviorPrediction> {
        // This would use behavioral patterns to predict changes
        return emptyList()
    }
    
    private fun assessOverallRisk(
        weight: WeightAnalytics,
        nutrition: NutritionAnalytics,
        health: HealthAnalytics
    ): RiskAssessment {
        val weightRisk = assessWeightRisk(weight)
        val nutritionRisk = assessNutritionRisk(nutrition)
        val healthRisk = health.healthScore.let { 100 - it }
        
        val overallRisk = (weightRisk + nutritionRisk + healthRisk) / 3
        
        val highestRisks = mutableListOf<Risk>()
        
        if (weightRisk > 50) {
            highestRisks.add(
                Risk(
                    name = "Gewichtsproblem",
                    category = RiskCategory.NUTRITION,
                    severity = if (weightRisk > 70) RiskLevel.HIGH else RiskLevel.MEDIUM,
                    likelihood = weightRisk,
                    impact = "Gesundheitliche Komplikationen",
                    timeframe = "3-6 Monate"
                )
            )
        }
        
        return RiskAssessment(
            dogId = "", // Will be set from context
            assessmentDate = LocalDateTime.now(),
            overallRiskScore = overallRisk,
            riskCategories = mapOf(
                RiskCategory.WEIGHT to if (weightRisk > 70) StatisticsRiskLevel.HIGH else if (weightRisk > 40) StatisticsRiskLevel.MODERATE else StatisticsRiskLevel.LOW,
                RiskCategory.NUTRITION to if (nutritionRisk > 70) StatisticsRiskLevel.HIGH else if (nutritionRisk > 40) StatisticsRiskLevel.MODERATE else StatisticsRiskLevel.LOW,
                RiskCategory.HEALTH to if (healthRisk > 70) StatisticsRiskLevel.HIGH else if (healthRisk > 40) StatisticsRiskLevel.MODERATE else StatisticsRiskLevel.LOW
            ),
            recommendations = highestRisks.map { "Addressiere ${it.name}" },
            timelinessScore = 80.0
        )
    }
    
    private fun assessWeightRisk(analytics: WeightAnalytics): Double {
        val bcsRisk = when {
            analytics.bodyConditionScore < 4 || analytics.bodyConditionScore > 6 -> 50.0
            analytics.bodyConditionScore < 4.5 || analytics.bodyConditionScore > 5.5 -> 25.0
            else -> 0.0
        }
        
        val trendRisk = when (analytics.weightTrend) {
            StatisticsTrendDirection.INCREASING, StatisticsTrendDirection.DECREASING -> 25.0
            StatisticsTrendDirection.STABLE -> 0.0
        }
        
        return (bcsRisk + trendRisk).coerceIn(0.0, 100.0)
    }
    
    private fun assessNutritionRisk(analytics: NutritionAnalytics): Double {
        var risk = 0.0
        
        if (analytics.calorieBalance != CalorieBalance.BALANCED) risk += 25.0
        if (analytics.treatPercentage > 20) risk += 25.0
        if (analytics.micronutrientAnalysis.adequacyScore < 80) risk += 25.0
        if (analytics.nutrientDeficiencies.isNotEmpty()) risk += 25.0
        
        return risk.coerceIn(0.0, 100.0)
    }
    
    private fun createMitigationPlan(risks: List<Risk>): List<MitigationStep> {
        return risks.map { risk ->
            MitigationStep(
                action = "Risiko '${risk.name}' addressieren",
                priority = when (risk.severity) {
                    RiskLevel.CRITICAL -> PriorityLevel.CRITICAL
                    RiskLevel.HIGH -> PriorityLevel.HIGH
                    RiskLevel.MEDIUM -> PriorityLevel.MEDIUM
                    RiskLevel.LOW -> PriorityLevel.LOW
                },
                timeline = risk.timeframe,
                resources = listOf("Tierarzt konsultieren", "Ernährung anpassen"),
                expectedOutcome = "Risiko um 50% reduzieren"
            )
        }
    }
    
    private fun recommendInterventions(
        risk: RiskAssessment,
        weight: WeightAnalytics,
        nutrition: NutritionAnalytics,
        health: HealthAnalytics
    ): List<PredictedHealthIssue> {
        val interventions = mutableListOf<PredictedHealthIssue>()
        
        // Weight interventions
        if (abs(weight.bodyConditionScore - 5.0) > 1) {
            interventions.add(
                PredictedHealthIssue(
                    condition = "Gewichtsmanagement erforderlich",
                    probability = 0.8,
                    timeframe = "3-6 Monate",
                    preventiveMeasures = listOf(
                        "Tägliche Futtermenge um 10% reduzieren",
                        "Tägliche Bewegung um 15 Minuten erhöhen",
                        "Wöchentlich wiegen"
                    ),
                    riskFactors = if (abs(weight.bodyConditionScore - 5.0) > 2) {
                        listOf("Stark erhöhtes Gewicht", "Gesundheitsrisiko")
                    } else {
                        listOf("Leichtes Übergewicht")
                    }
                )
            )
        }
        
        return interventions
    }
    
    private suspend fun predictLifestageTransition(dogId: String): PredictedHealthIssue? {
        val dog = dogRepository.getDogById(dogId).getOrNull() ?: return null
        val ageInYears = ChronoUnit.YEARS.between(dog.birthDate, LocalDate.now()).toInt()
        
        val (currentStage, nextStage, transitionAge) = when {
            ageInYears < 1 -> Triple("Welpe", "Heranwachsend", 1)
            ageInYears < 2 -> Triple("Heranwachsend", "Erwachsen", 2)
            ageInYears < 7 -> Triple("Erwachsen", "Senior", 7)
            ageInYears < 12 -> Triple("Senior", "Hochbetagt", 12)
            else -> return null
        }
        
        val monthsToTransition = (transitionAge - ageInYears) * 12
        
        return if (monthsToTransition <= 6) {
            PredictedHealthIssue(
                condition = "Übergang von $currentStage zu $nextStage",
                probability = 1.0,
                timeframe = "$monthsToTransition Monate",
                preventiveMeasures = getLifestagePreparationSteps(nextStage),
                riskFactors = listOf("Altersbedingter Übergang")
            )
        } else null
    }
    
    private fun getLifestagePreparationSteps(stage: String): List<String> {
        return when (stage) {
            "Senior" -> listOf(
                "Tierarzt-Check-up vereinbaren",
                "Auf Senior-Futter umstellen",
                "Gelenkgesundheit überwachen",
                "Aktivität anpassen"
            )
            "Erwachsen" -> listOf(
                "Von Welpen- auf Erwachsenenfutter umstellen",
                "Portionen anpassen"
            )
            else -> emptyList()
        }
    }
    
    private suspend fun compareToBreedAverages(dog: Dog): BreedComparison {
        // This would fetch breed-specific data
        return BreedComparison()
    }
    
    private suspend fun compareToAgeGroup(dog: Dog): AgeGroupComparison {
        // This would compare to dogs of similar age
        return AgeGroupComparison()
    }
    
    private suspend fun compareToSimilarDogs(dog: Dog): SimilarDogsComparison {
        // This would find and compare to similar dogs
        return SimilarDogsComparison()
    }
    
    private suspend fun compareToHistoricalData(
        dogId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        // This would compare current period to previous period
        // Currently not implemented
    }
    
    private suspend fun compareToGoals(dogId: String) {
        // This would compare progress to set goals
        // Currently not implemented
    }
    
    private suspend fun cacheStatistics(statistics: AdvancedStatistics) {
        // Cache for performance
        val data = mapOf(
            "dogId" to statistics.dogId,
            "period" to statistics.period.name,
            "generatedAt" to statistics.generatedAt.toString(),
            "data" to statistics // Would serialize to JSON
        )
        
        appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = ANALYTICS_CACHE_COLLECTION_ID,
            documentId = io.appwrite.ID.unique(),
            data = data
        )
    }
    
    private suspend fun scheduleReport(reportId: String, schedule: ReportSchedule) {
        val data = mapOf(
            "reportId" to reportId,
            "frequency" to schedule.frequency,
            "dayOfWeek" to schedule.dayOfWeek,
            "dayOfMonth" to schedule.dayOfMonth,
            "time" to schedule.time,
            "nextRunDate" to calculateNextRunDate(schedule).toString(),
            "isActive" to schedule.isActive
        )
        
        appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = REPORT_SCHEDULES_COLLECTION_ID,
            documentId = io.appwrite.ID.unique(),
            data = data
        )
    }
    
    private fun calculateNextRunDate(schedule: ReportSchedule): LocalDateTime {
        val now = LocalDateTime.now()
        return when (schedule.frequency.uppercase()) {
            "DAILY" -> now.plusDays(1)
            "WEEKLY" -> now.plusWeeks(1)
            "MONTHLY" -> now.plusMonths(1)
            "QUARTERLY" -> now.plusMonths(3)
            "YEARLY" -> now.plusYears(1)
            else -> now.plusDays(1) // Default to daily
        }
    }
    
    private suspend fun getCustomReportById(reportId: String): Result<CustomReport> = safeApiCall {
        val document = appwriteService.databases.getDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = CUSTOM_REPORTS_COLLECTION_ID,
            documentId = reportId
        )
        documentToCustomReport(document)
    }
    
    private suspend fun collectReportData(report: CustomReport): Map<String, Any> {
        // Collect data based on report configuration
        return emptyMap()
    }
    
    private fun generatePdfReport(report: CustomReport, data: Map<String, Any>): ByteArray {
        // Generate PDF using a library like iText
        return ByteArray(0)
    }
    
    private fun generateExcelReport(report: CustomReport, data: Map<String, Any>): ByteArray {
        // Generate Excel using Apache POI
        return ByteArray(0)
    }
    
    private fun generateCsvReport(report: CustomReport, data: Map<String, Any>): ByteArray {
        // Generate CSV
        return ByteArray(0)
    }
    
    private fun generateHtmlReport(report: CustomReport, data: Map<String, Any>): ByteArray {
        // Generate HTML
        return ByteArray(0)
    }
    
    private fun generateJsonReport(report: CustomReport, data: Map<String, Any>): ByteArray {
        // Generate JSON
        return ByteArray(0)
    }
    
    private fun documentToCustomReport(document: Document<Map<String, Any>>): CustomReport {
        val sectionsData = document.data["sections"] as List<Map<String, Any>>
        val filtersData = document.data["filters"] as Map<String, Any>
        val scheduleData = document.data["schedule"] as? Map<String, Any>
        
        return CustomReport(
            id = document.id,
            name = document.data["name"] as String,
            description = document.data["description"] as String,
            createdBy = document.data["createdBy"] as String,
            createdAt = LocalDateTime.parse(document.data["createdAt"] as String),
            reportType = ReportType.valueOf(document.data["reportType"] as String),
            sections = sectionsData.map { sectionData ->
                val customizationData = sectionData["customization"] as Map<String, Any>
                ReportSection(
                    id = sectionData["id"] as String,
                    title = sectionData["title"] as String,
                    type = SectionType.valueOf(sectionData["type"] as String),
                    dataSource = DataSource.valueOf(sectionData["dataSource"] as String),
                    visualization = VisualizationType.valueOf(sectionData["visualization"] as String),
                    metrics = (sectionData["metrics"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    customization = ReportCustomization(
                        colors = (customizationData["colors"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        fontSize = (customizationData["fontSize"] as? Number)?.toInt() ?: 12,
                        showLegend = customizationData["showLegend"] as? Boolean ?: true,
                        showDataLabels = customizationData["showDataLabels"] as? Boolean ?: false
                    )
                )
            },
            filters = ReportFilters(
                dateRange = filtersData["dateRange"] as String,
                dogs = (filtersData["dogs"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                categories = (filtersData["categories"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                metrics = (filtersData["metrics"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                comparison = filtersData["comparison"] as? String
            ),
            schedule = scheduleData?.let {
                ReportSchedule(
                    frequency = it["frequency"] as String,
                    dayOfWeek = (it["dayOfWeek"] as? Number)?.toInt(),
                    dayOfMonth = (it["dayOfMonth"] as? Number)?.toInt(),
                    time = it["time"] as String,
                    nextRunDate = (it["nextRunDate"] as? String)?.let { LocalDate.parse(it) },
                    isActive = it["isActive"] as Boolean,
                    recipients = emptyList(),
                    format = ReportFormat.PDF
                )
            }
        )
    }
}