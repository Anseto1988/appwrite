package com.example.snacktrack.data.repository

import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime

class NutritionRepository(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : BaseRepository() {
    
    companion object {
        const val NUTRITION_ANALYSIS_COLLECTION_ID = "nutrition_analysis"
        const val TREAT_BUDGET_COLLECTION_ID = "treat_budgets"
    }
    
    private val foodIntakeRepository = FoodIntakeRepository(context, appwriteService)
    private val dogRepository = DogRepository(context, appwriteService)
    
    /**
     * Calculate and save daily nutrition analysis
     */
    suspend fun analyzeDailyNutrition(dogId: String, date: LocalDate): Result<NutritionAnalysis> = safeApiCall {
        // Get dog data for calorie calculations
        val dogResult = dogRepository.getDogById(dogId)
        val dog = dogResult.getOrNull() ?: throw Exception("Dog not found")
        
        // Get all food intakes for the day
        val intakesFlow = foodIntakeRepository.getFoodIntakesForDog(dogId, date)
        val intakes = kotlinx.coroutines.flow.first(intakesFlow)
        
        // Calculate totals
        var totalCalories = 0
        var totalProtein = 0.0
        var totalFat = 0.0
        var totalCarbs = 0.0
        var totalFiber = 0.0
        var totalMoisture = 0.0
        var treatCalories = 0
        
        // Categorize food types for BARF analysis
        var meatGrams = 0.0
        var boneGrams = 0.0
        var organGrams = 0.0
        var vegetableGrams = 0.0
        
        intakes.forEach { intake ->
            totalCalories += intake.calories
            
            // If we have detailed nutrition info
            intake.protein?.let { totalProtein += it * (intake.amountGram / 100) }
            intake.fat?.let { totalFat += it * (intake.amountGram / 100) }
            intake.carbs?.let { totalCarbs += it * (intake.amountGram / 100) }
            
            // Categorize as treat if it's a small portion or marked as treat
            if (intake.amountGram < 50 || intake.note?.contains("Leckerli", ignoreCase = true) == true) {
                treatCalories += intake.calories
            }
            
            // BARF categorization (simplified - would need food category info)
            when {
                intake.foodName.contains("Fleisch", ignoreCase = true) || 
                intake.foodName.contains("Huhn", ignoreCase = true) ||
                intake.foodName.contains("Rind", ignoreCase = true) -> meatGrams += intake.amountGram
                
                intake.foodName.contains("Knochen", ignoreCase = true) -> boneGrams += intake.amountGram
                
                intake.foodName.contains("Leber", ignoreCase = true) ||
                intake.foodName.contains("Niere", ignoreCase = true) -> organGrams += intake.amountGram
                
                intake.foodName.contains("Gemüse", ignoreCase = true) ||
                intake.foodName.contains("Obst", ignoreCase = true) -> vegetableGrams += intake.amountGram
            }
        }
        
        // Calculate recommended values
        val recommendedCalories = dog.calculateDailyCalorieNeed()
        val recommendedProtein = dog.weight * 2.5 // 2.5g per kg body weight
        val recommendedFat = dog.weight * 1.0 // 1g per kg body weight
        val recommendedCarbs = (recommendedCalories * 0.45) / 4 // 45% of calories from carbs, 4 cal/g
        
        // Calculate BARF percentages
        val totalBARFGrams = meatGrams + boneGrams + organGrams + vegetableGrams
        val meatPercentage = if (totalBARFGrams > 0) (meatGrams / totalBARFGrams) * 100 else 0.0
        val bonePercentage = if (totalBARFGrams > 0) (boneGrams / totalBARFGrams) * 100 else 0.0
        val organPercentage = if (totalBARFGrams > 0) (organGrams / totalBARFGrams) * 100 else 0.0
        val vegetablePercentage = if (totalBARFGrams > 0) (vegetableGrams / totalBARFGrams) * 100 else 0.0
        
        val analysis = NutritionAnalysis(
            dogId = dogId,
            date = date,
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalFat = totalFat,
            totalCarbs = totalCarbs,
            totalFiber = totalFiber,
            totalMoisture = totalMoisture,
            recommendedCalories = recommendedCalories,
            recommendedProtein = recommendedProtein,
            recommendedFat = recommendedFat,
            recommendedCarbs = recommendedCarbs,
            treatCaloriesConsumed = treatCalories,
            treatCaloriesLimit = (recommendedCalories * 0.1).toInt(), // 10% for treats
            meatPercentage = meatPercentage,
            bonePercentage = bonePercentage,
            organPercentage = organPercentage,
            vegetablePercentage = vegetablePercentage
        )
        
        // Save to database
        saveNutritionAnalysis(analysis)
    }
    
    /**
     * Save nutrition analysis to database
     */
    private suspend fun saveNutritionAnalysis(analysis: NutritionAnalysis): NutritionAnalysis {
        val data = mapOf(
            "dogId" to analysis.dogId,
            "date" to analysis.date.toString(),
            "totalCalories" to analysis.totalCalories,
            "totalProtein" to analysis.totalProtein,
            "totalFat" to analysis.totalFat,
            "totalCarbs" to analysis.totalCarbs,
            "totalFiber" to analysis.totalFiber,
            "totalMoisture" to analysis.totalMoisture,
            "recommendedCalories" to analysis.recommendedCalories,
            "recommendedProtein" to analysis.recommendedProtein,
            "recommendedFat" to analysis.recommendedFat,
            "recommendedCarbs" to analysis.recommendedCarbs,
            "treatCaloriesConsumed" to analysis.treatCaloriesConsumed,
            "treatCaloriesLimit" to analysis.treatCaloriesLimit,
            "meatPercentage" to analysis.meatPercentage,
            "bonePercentage" to analysis.bonePercentage,
            "organPercentage" to analysis.organPercentage,
            "vegetablePercentage" to analysis.vegetablePercentage
        )
        
        // Check if analysis for this date already exists
        val existing = getNutritionAnalysis(analysis.dogId, analysis.date)
        
        val document = if (existing.isSuccess && existing.getOrNull() != null) {
            // Update existing
            appwriteService.databases.updateDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = NUTRITION_ANALYSIS_COLLECTION_ID,
                documentId = existing.getOrNull()!!.id,
                data = data
            )
        } else {
            // Create new
            appwriteService.databases.createDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = NUTRITION_ANALYSIS_COLLECTION_ID,
                documentId = ID.unique(),
                data = data
            )
        }
        
        return documentToNutritionAnalysis(document)
    }
    
    /**
     * Get nutrition analysis for a specific date
     */
    suspend fun getNutritionAnalysis(dogId: String, date: LocalDate): Result<NutritionAnalysis?> = safeApiCall {
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = NUTRITION_ANALYSIS_COLLECTION_ID,
            queries = listOf(
                Query.equal("dogId", dogId),
                Query.equal("date", date.toString())
            )
        )
        
        response.documents.firstOrNull()?.let { documentToNutritionAnalysis(it) }
    }
    
    /**
     * Get nutrition analysis for date range
     */
    suspend fun getNutritionAnalysisRange(
        dogId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<NutritionAnalysis>> = safeApiCall {
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = NUTRITION_ANALYSIS_COLLECTION_ID,
            queries = listOf(
                Query.equal("dogId", dogId),
                Query.greaterThanEqual("date", startDate.toString()),
                Query.lessThanEqual("date", endDate.toString())
            )
        )
        
        response.documents.map { documentToNutritionAnalysis(it) }
    }
    
    /**
     * Calculate BARF portions for a dog
     */
    suspend fun calculateBARFPortions(dogId: String): Result<BARFCalculation> = safeApiCall {
        val dogResult = dogRepository.getDogById(dogId)
        val dog = dogResult.getOrNull() ?: throw Exception("Dog not found")
        
        val age = when {
            dog.birthDate == null -> DogAge.ADULT
            dog.birthDate.plusYears(1).isAfter(LocalDate.now()) -> DogAge.PUPPY
            dog.birthDate.plusYears(2).isAfter(LocalDate.now()) -> DogAge.JUNIOR
            dog.birthDate.plusYears(7).isAfter(LocalDate.now()) -> DogAge.ADULT
            else -> DogAge.SENIOR
        }
        
        BARFCalculation(
            dogWeight = dog.weight,
            activityLevel = dog.activityLevel,
            age = age
        )
    }
    
    /**
     * Get or create treat budget for today
     */
    suspend fun getTreatBudget(dogId: String, date: LocalDate = LocalDate.now()): Result<TreatBudget> = safeApiCall {
        // Get dog's daily calorie need
        val dogResult = dogRepository.getDogById(dogId)
        val dog = dogResult.getOrNull() ?: throw Exception("Dog not found")
        val dailyCalories = dog.calculateDailyCalorieNeed()
        val treatLimit = (dailyCalories * 0.1).toInt() // 10% for treats
        
        // Get existing budget or create new
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = TREAT_BUDGET_COLLECTION_ID,
            queries = listOf(
                Query.equal("dogId", dogId),
                Query.equal("date", date.toString())
            )
        )
        
        if (response.documents.isNotEmpty()) {
            documentToTreatBudget(response.documents.first())
        } else {
            // Create new budget
            val budget = TreatBudget(
                dogId = dogId,
                date = date,
                dailyCalorieLimit = treatLimit
            )
            saveTreatBudget(budget)
        }
    }
    
    /**
     * Add treat to budget
     */
    suspend fun addTreatToBudget(
        dogId: String,
        treat: TreatEntry,
        date: LocalDate = LocalDate.now()
    ): Result<TreatBudget> = safeApiCall {
        val budgetResult = getTreatBudget(dogId, date)
        val budget = budgetResult.getOrNull() ?: throw Exception("Could not get treat budget")
        
        if (!budget.canAddTreat(treat.calories)) {
            throw Exception("Treat würde das Tagesbudget überschreiten!")
        }
        
        val updatedBudget = budget.copy(
            treats = budget.treats + treat
        )
        
        saveTreatBudget(updatedBudget)
    }
    
    private suspend fun saveTreatBudget(budget: TreatBudget): TreatBudget {
        val treatData = budget.treats.map { treat ->
            mapOf(
                "name" to treat.name,
                "calories" to treat.calories,
                "timeGiven" to treat.timeGiven,
                "reason" to treat.reason.name
            )
        }
        
        val data = mapOf(
            "dogId" to budget.dogId,
            "date" to budget.date.toString(),
            "dailyCalorieLimit" to budget.dailyCalorieLimit,
            "treats" to treatData
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = TREAT_BUDGET_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
        
        return documentToTreatBudget(document)
    }
    
    // Helper functions for document conversion
    private fun documentToNutritionAnalysis(document: Document<Map<String, Any>>): NutritionAnalysis {
        return NutritionAnalysis(
            id = document.id,
            dogId = document.data["dogId"] as String,
            date = LocalDate.parse(document.data["date"] as String),
            totalCalories = (document.data["totalCalories"] as Number).toInt(),
            totalProtein = (document.data["totalProtein"] as Number).toDouble(),
            totalFat = (document.data["totalFat"] as Number).toDouble(),
            totalCarbs = (document.data["totalCarbs"] as Number).toDouble(),
            totalFiber = (document.data["totalFiber"] as Number).toDouble(),
            totalMoisture = (document.data["totalMoisture"] as Number).toDouble(),
            recommendedCalories = (document.data["recommendedCalories"] as Number).toInt(),
            recommendedProtein = (document.data["recommendedProtein"] as Number).toDouble(),
            recommendedFat = (document.data["recommendedFat"] as Number).toDouble(),
            recommendedCarbs = (document.data["recommendedCarbs"] as Number).toDouble(),
            treatCaloriesConsumed = (document.data["treatCaloriesConsumed"] as Number).toInt(),
            treatCaloriesLimit = (document.data["treatCaloriesLimit"] as Number).toInt(),
            meatPercentage = (document.data["meatPercentage"] as Number).toDouble(),
            bonePercentage = (document.data["bonePercentage"] as Number).toDouble(),
            organPercentage = (document.data["organPercentage"] as Number).toDouble(),
            vegetablePercentage = (document.data["vegetablePercentage"] as Number).toDouble()
        )
    }
    
    private fun documentToTreatBudget(document: Document<Map<String, Any>>): TreatBudget {
        val treatsData = document.data["treats"] as? List<Map<String, Any>> ?: emptyList()
        val treats = treatsData.map { treatMap ->
            TreatEntry(
                name = treatMap["name"] as String,
                calories = (treatMap["calories"] as Number).toInt(),
                timeGiven = treatMap["timeGiven"] as String,
                reason = TreatReason.valueOf(treatMap["reason"] as String)
            )
        }
        
        return TreatBudget(
            dogId = document.data["dogId"] as String,
            date = LocalDate.parse(document.data["date"] as String),
            dailyCalorieLimit = (document.data["dailyCalorieLimit"] as Number).toInt(),
            treats = treats
        )
    }
}