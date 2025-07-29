package com.example.snacktrack.data.repository

import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.Query
import io.appwrite.ID
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.*

class AIRepository(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : BaseRepository() {
    
    companion object {
        const val RECOMMENDATIONS_COLLECTION_ID = "ai_recommendations"
        const val PREDICTIONS_COLLECTION_ID = "ai_predictions"
        const val ANOMALIES_COLLECTION_ID = "ai_anomalies"
        const val TRAINING_DATA_COLLECTION_ID = "ai_training_data"
        const val RISK_ASSESSMENTS_COLLECTION_ID = "ai_risk_assessments"
    }
    
    private val dogRepository = DogRepository(context)
    private val foodIntakeRepository = FoodIntakeRepository(context)
    private val weightRepository = WeightRepository(context)
    private val healthRepository = HealthRepository(context, appwriteService)
    
    /**
     * Generate AI-based food recommendations
     */
    suspend fun generateFoodRecommendations(
        dogId: String,
        type: AIRecommendationType = AIRecommendationType.DAILY
    ): Result<FoodRecommendation> = safeApiCall {
        // Gather all necessary data
        val dog = dogRepository.getDogById(dogId).getOrThrow()
        
        val allergies = healthRepository.getAllergiesForDog(dogId).getOrNull() ?: emptyList()
        val recentIntakes = mutableListOf<FoodIntake>()
        for (i in 0..30) {
            val date = LocalDate.now().minusDays(i.toLong())
            val intakes = foodIntakeRepository.getFoodIntakesForDog(dogId, date).first()
            recentIntakes.addAll(intakes)
        }
        
        val weightHistory = weightRepository.getWeightHistory(dogId).first()
        
        // Calculate factors
        val age = dog.birthDate?.let { birthDate ->
            val now = LocalDate.now()
            val years = now.year - birthDate.year
            val dayDiff = now.dayOfMonth - birthDate.dayOfMonth
            years + dayDiff / 365.0
        } ?: 2.0
        
        val factors = RecommendationFactors(
            breed = dog.breed,
            age = age,
            weight = dog.weight,
            activityLevel = dog.activityLevel,
            healthConditions = emptyList(), // Would need health condition tracking
            allergies = allergies.map { it.allergen },
            preferences = analyzePreferences(recentIntakes)
        )
        
        // Generate recommendations based on type
        val recommendations = when (type) {
            AIRecommendationType.DAILY -> generateDailyRecommendations(dog, factors)
            AIRecommendationType.WEIGHT_LOSS -> generateWeightLossRecommendations(dog, factors, weightHistory)
            AIRecommendationType.WEIGHT_GAIN -> generateWeightGainRecommendations(dog, factors)
            else -> generateDailyRecommendations(dog, factors)
        }
        
        val recommendation = FoodRecommendation(
            dogId = dogId,
            recommendationType = type,
            recommendations = recommendations,
            reasoning = generateReasoningText(factors, recommendations),
            confidenceScore = calculateConfidenceScore(factors, recentIntakes.size),
            factors = factors
        )
        
        // Save recommendation
        saveRecommendation(recommendation)
    }
    
    /**
     * Predict future weight based on historical data and current habits
     */
    suspend fun predictWeight(
        dogId: String,
        daysToPredict: Int = 90
    ): Result<AIWeightPrediction> = safeApiCall {
        val dog = dogRepository.getDogById(dogId).getOrThrow()
        
        val weightHistory = weightRepository.getWeightHistory(dogId).first()
        
        if (weightHistory.size < 3) {
            throw Exception("Nicht genügend Gewichtsdaten für Vorhersage")
        }
        
        val recentIntakes = mutableListOf<FoodIntake>()
        for (i in 0..30) {
            val date = LocalDate.now().minusDays(i.toLong())
            val intakes = foodIntakeRepository.getFoodIntakesForDog(dogId, date).first()
            recentIntakes.addAll(intakes)
        }
        
        val avgDailyCalories = if (recentIntakes.isNotEmpty()) {
            recentIntakes.groupBy { it.timestamp.toLocalDate() }
                .map { it.value.sumOf { intake -> intake.calories } }
                .average().toInt()
        } else {
            dog.calculateDailyCalorieNeed()
        }
        
        // Simple linear regression for weight prediction
        val predictions = predictWeightTrend(
            weightHistory,
            avgDailyCalories,
            dog.calculateDailyCalorieNeed(),
            daysToPredict
        )
        
        val factors = WeightPredictionFactors(
            historicalWeights = weightHistory,
            averageDailyCalories = avgDailyCalories,
            activityLevel = dog.activityLevel,
            breed = dog.breed,
            age = dog.birthDate?.let { birthDate ->
                val now = LocalDate.now()
                val years = now.year - birthDate.year
                val dayDiff = now.dayOfMonth - birthDate.dayOfMonth
                years + dayDiff / 365.0
            } ?: 2.0,
            isNeutered = false, // Would need this data
            healthConditions = emptyList()
        )
        
        val prediction = AIWeightPrediction(
            dogId = dogId,
            currentWeight = dog.weight,
            predictions = predictions,
            confidenceInterval = calculateConfidenceInterval(predictions),
            factors = factors
        )
        
        // Save prediction
        savePrediction(prediction)
    }
    
    /**
     * Detect anomalies in eating patterns
     */
    suspend fun detectEatingAnomalies(dogId: String): Result<List<EatingAnomaly>> = safeApiCall {
        val recentIntakes = mutableListOf<FoodIntake>()
        for (i in 0..6) {
            val date = LocalDate.now().minusDays(i.toLong())
            val intakes = foodIntakeRepository.getFoodIntakesForDog(dogId, date).first()
            recentIntakes.addAll(intakes)
        }
        
        val historicalIntakes = mutableListOf<FoodIntake>()
        for (i in 8..30) {
            val date = LocalDate.now().minusDays(i.toLong())
            val intakes = foodIntakeRepository.getFoodIntakesForDog(dogId, date).first()
            historicalIntakes.addAll(intakes)
        }
        
        if (historicalIntakes.isEmpty()) {
            return@safeApiCall emptyList<EatingAnomaly>()
        }
        
        // Establish baseline
        val baseline = establishBaseline(historicalIntakes)
        
        // Detect various anomalies
        val anomalies = mutableListOf<EatingAnomaly>()
        
        // Check for unusual amounts
        val dailyCalories = recentIntakes.groupBy { it.timestamp.toLocalDate() }
            .mapValues { (_, intakeList) -> intakeList.sumOf { intake -> intake.calories } }
        
        dailyCalories.forEach { (date, calories) ->
            val deviation = kotlin.math.abs(calories - baseline.averageDailyCalories) / baseline.averageDailyCalories.toDouble()
            if (deviation > 0.3) { // 30% deviation
                anomalies.add(
                    createAnomaly(
                        dogId,
                        AnomalyType.UNUSUAL_AMOUNT,
                        if (deviation > 0.5) AnomalySeverity.HIGH else AnomalySeverity.MEDIUM,
                        "Ungewöhnliche Kalorienzufuhr: $calories kcal (Normal: ${baseline.averageDailyCalories} kcal)",
                        recentIntakes.filter { intake -> intake.timestamp.toLocalDate() == date },
                        baseline
                    )
                )
            }
        }
        
        // Check for skipped meals
        val expectedMealsPerDay = baseline.normalMealTimes.size
        val daysWithSkippedMeals = (0..6).map { LocalDate.now().minusDays(it.toLong()) }
            .filter { date ->
                recentIntakes.count { intake -> intake.timestamp.toLocalDate() == date } < expectedMealsPerDay - 1
            }
        
        if (daysWithSkippedMeals.size >= 2) {
            anomalies.add(
                createAnomaly(
                    dogId,
                    AnomalyType.SKIPPED_MEALS,
                    AnomalySeverity.MEDIUM,
                    "Mehrere übersprungene Mahlzeiten in den letzten 7 Tagen",
                    emptyList(),
                    baseline
                )
            )
        }
        
        // Save anomalies
        anomalies.forEach { saveAnomaly(it) }
        
        anomalies
    }
    
    /**
     * Perform health risk assessment
     */
    suspend fun assessHealthRisk(dogId: String): Result<HealthRiskAssessment> = safeApiCall {
        val dog = dogRepository.getDogById(dogId).getOrThrow()
        
        val weightHistory = weightRepository.getWeightHistory(dogId).first()
        val recentIntakes = mutableListOf<FoodIntake>()
        for (i in 0..30) {
            val date = LocalDate.now().minusDays(i.toLong())
            val intakes = foodIntakeRepository.getFoodIntakesForDog(dogId, date).first()
            recentIntakes.addAll(intakes)
        }
        
        val allergies = healthRepository.getAllergiesForDog(dogId).getOrNull() ?: emptyList()
        val healthEntries = healthRepository.getHealthEntries(
            dogId,
            LocalDateTime.now().minusDays(30)
        ).getOrNull() ?: emptyList()
        
        val riskFactors = mutableListOf<RiskFactor>()
        
        // Assess weight risk
        val weightRisk = assessWeightRisk(dog, weightHistory)
        if (weightRisk.severity > 0.3f) {
            riskFactors.add(weightRisk)
        }
        
        // Assess nutrition risk
        val nutritionRisk = assessNutritionRisk(recentIntakes, dog)
        if (nutritionRisk.severity > 0.3f) {
            riskFactors.add(nutritionRisk)
        }
        
        // Assess activity risk
        val activityRisk = RiskFactor(
            factor = "Aktivitätslevel",
            category = AIRiskCategory.ACTIVITY,
            severity = when (dog.activityLevel) {
                ActivityLevel.VERY_LOW -> 0.7f
                ActivityLevel.LOW -> 0.5f
                ActivityLevel.NORMAL -> 0.2f
                ActivityLevel.HIGH -> 0.1f
                ActivityLevel.VERY_HIGH -> 0.1f
            },
            description = "Aktivitätslevel: ${dog.activityLevel.displayName}",
            improvementPlan = if (dog.activityLevel.factor < 1.6) 
                "Erhöhen Sie die tägliche Bewegung schrittweise" else ""
        )
        
        if (activityRisk.severity > 0.3f) {
            riskFactors.add(activityRisk)
        }
        
        // Calculate overall risk score
        val overallRisk = if (riskFactors.isNotEmpty()) {
            riskFactors.map { it.severity }.average().toFloat()
        } else 0f
        
        // Generate recommendations
        val recommendations = generateHealthRecommendations(riskFactors, dog)
        
        val assessment = HealthRiskAssessment(
            dogId = dogId,
            overallRiskScore = overallRisk,
            riskFactors = riskFactors,
            recommendations = recommendations
        )
        
        // Save assessment
        saveHealthAssessment(assessment)
    }
    
    // Helper functions for AI calculations
    
    private fun analyzePreferences(intakes: List<FoodIntake>): List<String> {
        return intakes.groupBy { it.foodName }
            .entries
            .sortedByDescending { it.value.size }
            .take(5)
            .map { it.key }
    }
    
    private fun generateDailyRecommendations(
        dog: Dog,
        factors: RecommendationFactors
    ): List<FoodRecommendationItem> {
        val dailyCalories = dog.calculateDailyCalorieNeed()
        
        // Simple recommendation logic - would be ML model in production
        return listOf(
            FoodRecommendationItem(
                foodName = "Premium Adult Hundefutter",
                brand = "Recommended Brand",
                recommendedAmount = dailyCalories / 3.5, // Assuming 350 kcal/100g
                frequency = FeedingFrequency.TWICE_DAILY,
                reason = "Ausgewogene Ernährung für ${factors.breed} im Alter von ${factors.age.toInt()} Jahren",
                nutritionMatch = 0.85f,
                allergenSafe = !factors.allergies.any { it.contains("Huhn", ignoreCase = true) }
            )
        )
    }
    
    private fun generateWeightLossRecommendations(
        dog: Dog,
        factors: RecommendationFactors,
        weightHistory: List<WeightEntry>
    ): List<FoodRecommendationItem> {
        val targetCalories = (dog.calculateDailyCalorieNeed() * 0.8).toInt() // 20% reduction
        
        return listOf(
            FoodRecommendationItem(
                foodName = "Light & Fit Hundefutter",
                brand = "Diet Brand",
                recommendedAmount = targetCalories / 3.0, // Lower calorie density
                frequency = FeedingFrequency.THREE_TIMES_DAILY, // More frequent, smaller meals
                reason = "Kalorienreduzierte Kost für gesunde Gewichtsabnahme",
                nutritionMatch = 0.90f,
                allergenSafe = true
            )
        )
    }
    
    private fun generateWeightGainRecommendations(
        dog: Dog,
        factors: RecommendationFactors
    ): List<FoodRecommendationItem> {
        val targetCalories = (dog.calculateDailyCalorieNeed() * 1.2).toInt() // 20% increase
        
        return listOf(
            FoodRecommendationItem(
                foodName = "High Energy Hundefutter",
                brand = "Performance Brand",
                recommendedAmount = targetCalories / 4.5, // Higher calorie density
                frequency = FeedingFrequency.THREE_TIMES_DAILY,
                reason = "Kalorienreiche Kost für gesunde Gewichtszunahme",
                nutritionMatch = 0.88f,
                allergenSafe = true
            )
        )
    }
    
    private fun generateReasoningText(
        factors: RecommendationFactors,
        recommendations: List<FoodRecommendationItem>
    ): String {
        return "Basierend auf Rasse (${factors.breed}), Alter (${factors.age.toInt()} Jahre), " +
               "Gewicht (${factors.weight} kg) und Aktivitätslevel (${factors.activityLevel.displayName}) " +
               "wurden diese Empfehlungen erstellt."
    }
    
    private fun calculateConfidenceScore(factors: RecommendationFactors, dataPoints: Int): Float {
        val baseConfidence = 0.7f
        val dataBonus = minOf(dataPoints / 100f, 0.2f) // Up to 0.2 bonus for lots of data
        val factorCompleteness = listOf(
            factors.breed.isNotEmpty(),
            factors.age > 0,
            factors.weight > 0
        ).count { it } / 3f * 0.1f
        
        return minOf(baseConfidence + dataBonus + factorCompleteness, 0.95f)
    }
    
    private fun predictWeightTrend(
        history: List<WeightEntry>,
        currentCalories: Int,
        targetCalories: Int,
        days: Int
    ): List<WeightPredictionPoint> {
        // Simple linear regression
        val weights = history.map { it.weight }
        val avgWeight = weights.average()
        
        // Calculate trend (kg per day)
        val trend = if (history.size >= 2) {
            (history.last().weight - history.first().weight) / 
            java.time.temporal.ChronoUnit.DAYS.between(history.first().timestamp.toLocalDate(), history.last().timestamp.toLocalDate()).toDouble()
        } else 0.0
        
        // Adjust trend based on calorie differential
        val calorieDiff = currentCalories - targetCalories
        val adjustedTrend = trend + (calorieDiff.toDouble() / 3500.0) // 3500 kcal ≈ 0.45 kg
        
        return (1..days).map { day ->
            WeightPredictionPoint(
                date = LocalDate.now().plusDays(day.toLong()),
                predictedWeight = history.last().weight + (adjustedTrend * day),
                confidenceLevel = maxOf(0.9f - (day / 365f), 0.3f) // Confidence decreases over time
            )
        }
    }
    
    private fun calculateConfidenceInterval(
        predictions: List<WeightPredictionPoint>
    ): ConfidenceInterval {
        val margin = predictions.map { it.predictedWeight * (1 - it.confidenceLevel) * 0.1 }
        return ConfidenceInterval(
            lower = predictions.mapIndexed { i, p -> p.predictedWeight - margin[i] },
            upper = predictions.mapIndexed { i, p -> p.predictedWeight + margin[i] }
        )
    }
    
    private fun establishBaseline(intakes: List<FoodIntake>): BaselineEatingPattern {
        val dailyCalories = intakes.groupBy { it.timestamp.toLocalDate() }
            .mapValues { (_, intakeList) -> intakeList.sumOf { intake -> intake.calories } }
        
        val mealTimes = intakes.map { it.timestamp.hour }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { String.format("%02d:00", it.key) }
        
        return BaselineEatingPattern(
            averageDailyCalories = dailyCalories.values.average().toInt(),
            averageMealSize = intakes.map { it.amountGram }.average(),
            normalMealTimes = mealTimes,
            normalEatingDuration = 15, // Default assumption
            preferredFoods = intakes.groupBy { it.foodName }
                .entries
                .sortedByDescending { it.value.size }
                .take(5)
                .map { it.key }
        )
    }
    
    private fun createAnomaly(
        dogId: String,
        type: AnomalyType,
        severity: AnomalySeverity,
        description: String,
        affectedMeals: List<FoodIntake>,
        baseline: BaselineEatingPattern
    ): EatingAnomaly {
        val recommendation = when (type) {
            AnomalyType.UNUSUAL_AMOUNT -> when (severity) {
                AnomalySeverity.HIGH -> "Tierarzt konsultieren wenn das Verhalten anhält"
                AnomalySeverity.MEDIUM -> "Beobachten Sie das Fressverhalten genau"
                else -> "Leichte Abweichung, weiter beobachten"
            }
            AnomalyType.SKIPPED_MEALS -> "Stellen Sie sicher, dass Ihr Hund Zugang zu Futter hat"
            else -> "Beobachten Sie das Verhalten weiter"
        }
        
        return EatingAnomaly(
            dogId = dogId,
            anomalyType = type,
            severity = severity,
            description = description,
            affectedMeals = affectedMeals,
            baselineData = baseline,
            recommendation = recommendation,
            requiresVetAttention = severity == AnomalySeverity.CRITICAL ||
                                  (severity == AnomalySeverity.HIGH && type == AnomalyType.FOOD_REJECTION)
        )
    }
    
    private fun assessWeightRisk(dog: Dog, history: List<WeightEntry>): RiskFactor {
        val idealWeight = dog.targetWeight ?: dog.weight
        val currentDeviation = abs(dog.weight - idealWeight) / idealWeight
        
        val trend = if (history.size >= 2) {
            val recentTrend = history.takeLast(5).let { recent ->
                if (recent.size >= 2) {
                    (recent.last().weight - recent.first().weight) / recent.first().weight
                } else 0.0
            }
            recentTrend
        } else 0.0
        
        val severity = when {
            currentDeviation > 0.2 -> 0.8f
            currentDeviation > 0.15 -> 0.6f
            currentDeviation > 0.1 -> 0.4f
            else -> 0.2f
        } + if (abs(trend) > 0.05) 0.2f else 0f
        
        return RiskFactor(
            factor = "Gewicht",
            category = AIRiskCategory.WEIGHT,
            severity = minOf(severity, 1f),
            description = "Aktuelles Gewicht: ${dog.weight}kg (Ideal: ${idealWeight}kg)",
            improvementPlan = when {
                dog.weight > idealWeight * 1.1 -> "Kalorienreduzierte Diät empfohlen"
                dog.weight < idealWeight * 0.9 -> "Kalorienerhöhung empfohlen"
                else -> "Gewicht im gesunden Bereich halten"
            }
        )
    }
    
    private fun assessNutritionRisk(intakes: List<FoodIntake>, dog: Dog): RiskFactor {
        val dailyCalories = if (intakes.isNotEmpty()) {
            intakes.groupBy { it.timestamp.toLocalDate() }
                .mapValues { (_, intakeList) -> intakeList.sumOf { intake -> intake.calories } }
                .values.average()
        } else 0.0
        
        val targetCalories = dog.calculateDailyCalorieNeed()
        val deviation = kotlin.math.abs(dailyCalories - targetCalories) / targetCalories
        
        val severity = when {
            deviation > 0.3 -> 0.7f
            deviation > 0.2 -> 0.5f
            deviation > 0.1 -> 0.3f
            else -> 0.1f
        }
        
        return RiskFactor(
            factor = "Ernährung",
            category = AIRiskCategory.NUTRITION,
            severity = severity,
            description = "Durchschnittliche Kalorienzufuhr: ${dailyCalories.toInt()} kcal/Tag",
            improvementPlan = when {
                dailyCalories > targetCalories * 1.2 -> "Reduzieren Sie die Futtermenge"
                dailyCalories < targetCalories * 0.8 -> "Erhöhen Sie die Futtermenge"
                else -> "Ernährung ist ausgewogen"
            }
        )
    }
    
    private fun generateHealthRecommendations(
        riskFactors: List<RiskFactor>,
        dog: Dog
    ): List<HealthRecommendation> {
        return riskFactors.sortedByDescending { it.severity }
            .mapIndexed { index, risk ->
                HealthRecommendation(
                    priority = index + 1,
                    category = risk.category.name,
                    action = risk.improvementPlan,
                    expectedBenefit = "Reduzierung des ${risk.factor}-Risikos",
                    timeframe = when (risk.severity) {
                        in 0.7f..1f -> "Sofort"
                        in 0.5f..0.7f -> "Innerhalb einer Woche"
                        else -> "Innerhalb eines Monats"
                    }
                )
            }
    }
    
    // Save functions for persistence
    
    private suspend fun saveRecommendation(recommendation: FoodRecommendation): FoodRecommendation {
        val data = mapOf(
            "dogId" to recommendation.dogId,
            "generatedAt" to recommendation.generatedAt.toString(),
            "recommendationType" to recommendation.recommendationType.name,
            "recommendations" to recommendation.recommendations.map { item ->
                mapOf(
                    "foodId" to item.foodId,
                    "foodName" to item.foodName,
                    "brand" to item.brand,
                    "recommendedAmount" to item.recommendedAmount,
                    "frequency" to item.frequency.name,
                    "reason" to item.reason,
                    "nutritionMatch" to item.nutritionMatch,
                    "allergenSafe" to item.allergenSafe
                )
            },
            "reasoning" to recommendation.reasoning,
            "confidenceScore" to recommendation.confidenceScore,
            "factors" to mapOf(
                "breed" to recommendation.factors.breed,
                "age" to recommendation.factors.age,
                "weight" to recommendation.factors.weight,
                "activityLevel" to recommendation.factors.activityLevel.name,
                "healthConditions" to recommendation.factors.healthConditions,
                "allergies" to recommendation.factors.allergies,
                "preferences" to recommendation.factors.preferences
            )
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = RECOMMENDATIONS_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
        
        return recommendation.copy(id = document.id)
    }
    
    private suspend fun savePrediction(prediction: AIWeightPrediction): AIWeightPrediction {
        val data = mapOf(
            "dogId" to prediction.dogId,
            "predictionDate" to prediction.predictionDate.toString(),
            "currentWeight" to prediction.currentWeight,
            "predictions" to prediction.predictions.map {
                mapOf(
                    "date" to it.date.toString(),
                    "predictedWeight" to it.predictedWeight,
                    "confidenceLevel" to it.confidenceLevel
                )
            },
            "modelVersion" to prediction.modelVersion
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = PREDICTIONS_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
        
        return prediction.copy(id = document.id)
    }
    
    private suspend fun saveAnomaly(anomaly: EatingAnomaly): EatingAnomaly {
        val data = mapOf(
            "dogId" to anomaly.dogId,
            "detectedAt" to anomaly.detectedAt.toString(),
            "anomalyType" to anomaly.anomalyType.name,
            "severity" to anomaly.severity.name,
            "description" to anomaly.description,
            "recommendation" to anomaly.recommendation,
            "requiresVetAttention" to anomaly.requiresVetAttention
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = ANOMALIES_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
        
        return anomaly.copy(id = document.id)
    }
    
    private suspend fun saveHealthAssessment(assessment: HealthRiskAssessment): HealthRiskAssessment {
        val data = mapOf(
            "dogId" to assessment.dogId,
            "assessmentDate" to assessment.assessmentDate.toString(),
            "overallRiskScore" to assessment.overallRiskScore,
            "riskFactors" to assessment.riskFactors.map {
                mapOf(
                    "factor" to it.factor,
                    "category" to it.category.name,
                    "severity" to it.severity,
                    "description" to it.description,
                    "improvementPlan" to it.improvementPlan
                )
            },
            "recommendations" to assessment.recommendations.map {
                mapOf(
                    "priority" to it.priority,
                    "category" to it.category,
                    "action" to it.action,
                    "expectedBenefit" to it.expectedBenefit,
                    "timeframe" to it.timeframe
                )
            },
            "nextAssessmentDate" to assessment.nextAssessmentDate.toString()
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = RISK_ASSESSMENTS_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
        
        return assessment.copy(id = document.id)
    }
}