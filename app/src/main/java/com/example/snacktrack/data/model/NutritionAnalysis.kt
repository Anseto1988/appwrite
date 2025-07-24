package com.example.snacktrack.data.model

import java.time.LocalDate

/**
 * Represents daily nutrition analysis for a dog
 */
data class NutritionAnalysis(
    val id: String = "",
    val dogId: String = "",
    val date: LocalDate = LocalDate.now(),
    
    // Consumed nutrients (calculated from food intake)
    val totalCalories: Int = 0,
    val totalProtein: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFiber: Double = 0.0,
    val totalMoisture: Double = 0.0,
    
    // Recommended daily amounts
    val recommendedCalories: Int = 0,
    val recommendedProtein: Double = 0.0,
    val recommendedFat: Double = 0.0,
    val recommendedCarbs: Double = 0.0,
    
    // Treat budget
    val treatCaloriesConsumed: Int = 0,
    val treatCaloriesLimit: Int = 0,
    
    // BARF specific data
    val meatPercentage: Double = 0.0,
    val bonePercentage: Double = 0.0,
    val organPercentage: Double = 0.0,
    val vegetablePercentage: Double = 0.0,
    val supplementPercentage: Double = 0.0
) {
    // Calculate percentage of recommended intake
    val caloriePercentage: Double
        get() = if (recommendedCalories > 0) (totalCalories.toDouble() / recommendedCalories) * 100 else 0.0
    
    val proteinPercentage: Double
        get() = if (recommendedProtein > 0) (totalProtein / recommendedProtein) * 100 else 0.0
    
    val fatPercentage: Double
        get() = if (recommendedFat > 0) (totalFat / recommendedFat) * 100 else 0.0
    
    val carbPercentage: Double
        get() = if (recommendedCarbs > 0) (totalCarbs / recommendedCarbs) * 100 else 0.0
    
    val treatBudgetUsed: Double
        get() = if (treatCaloriesLimit > 0) (treatCaloriesConsumed.toDouble() / treatCaloriesLimit) * 100 else 0.0
    
    // Nutrition balance score (0-100)
    fun getNutritionScore(): Int {
        val scores = listOf(
            // Calories: Best at 95-105%
            when {
                caloriePercentage in 95.0..105.0 -> 100
                caloriePercentage in 90.0..110.0 -> 80
                caloriePercentage in 80.0..120.0 -> 60
                else -> 40
            },
            // Protein: Best at 90-110%
            when {
                proteinPercentage in 90.0..110.0 -> 100
                proteinPercentage in 80.0..120.0 -> 80
                else -> 60
            },
            // Fat: Best at 90-110%
            when {
                fatPercentage in 90.0..110.0 -> 100
                fatPercentage in 80.0..120.0 -> 80
                else -> 60
            },
            // Treats: Lower is better, max 10%
            when {
                treatBudgetUsed <= 10 -> 100
                treatBudgetUsed <= 15 -> 80
                treatBudgetUsed <= 20 -> 60
                else -> 40
            }
        )
        
        return scores.average().toInt()
    }
    
    fun getRecommendations(): List<NutritionRecommendation> {
        val recommendations = mutableListOf<NutritionRecommendation>()
        
        // Calorie recommendations
        when {
            caloriePercentage < 80 -> recommendations.add(
                NutritionRecommendation(
                    type = RecommendationType.INCREASE_CALORIES,
                    message = "Ihr Hund erhält zu wenig Kalorien. Erhöhen Sie die Futtermenge.",
                    severity = RecommendationSeverity.HIGH
                )
            )
            caloriePercentage > 120 -> recommendations.add(
                NutritionRecommendation(
                    type = RecommendationType.DECREASE_CALORIES,
                    message = "Ihr Hund erhält zu viele Kalorien. Reduzieren Sie die Futtermenge.",
                    severity = RecommendationSeverity.HIGH
                )
            )
        }
        
        // Protein recommendations
        when {
            proteinPercentage < 80 -> recommendations.add(
                NutritionRecommendation(
                    type = RecommendationType.INCREASE_PROTEIN,
                    message = "Proteinzufuhr ist zu niedrig. Wählen Sie proteinreicheres Futter.",
                    severity = RecommendationSeverity.MEDIUM
                )
            )
            proteinPercentage > 150 -> recommendations.add(
                NutritionRecommendation(
                    type = RecommendationType.DECREASE_PROTEIN,
                    message = "Sehr hohe Proteinzufuhr. Bei Nierenproblemen reduzieren.",
                    severity = RecommendationSeverity.LOW
                )
            )
        }
        
        // Treat recommendations
        if (treatBudgetUsed > 20) {
            recommendations.add(
                NutritionRecommendation(
                    type = RecommendationType.REDUCE_TREATS,
                    message = "Zu viele Leckerlis! Maximal 10% der Tageskalorien sollten aus Leckerlis stammen.",
                    severity = RecommendationSeverity.MEDIUM
                )
            )
        }
        
        return recommendations
    }
}

/**
 * BARF (Biologisch Artgerechtes Rohes Futter) Calculator
 */
data class BARFCalculation(
    val dogWeight: Double, // in kg
    val activityLevel: ActivityLevel,
    val age: DogAge,
    val specialNeeds: List<SpecialNeed> = emptyList()
) {
    // Calculate daily food amount (2-3% of body weight, adjusted for activity)
    val dailyFoodAmount: Double
        get() {
            val basePercentage = when (age) {
                DogAge.PUPPY -> 5.0 // Puppies need 5-6% of body weight
                DogAge.JUNIOR -> 3.5
                DogAge.ADULT -> 2.5
                DogAge.SENIOR -> 2.0
            }
            
            val activityMultiplier = when (activityLevel) {
                ActivityLevel.VERY_LOW -> 0.8
                ActivityLevel.LOW -> 0.9
                ActivityLevel.NORMAL -> 1.0
                ActivityLevel.HIGH -> 1.2
                ActivityLevel.VERY_HIGH -> 1.4
            }
            
            return (dogWeight * (basePercentage / 100) * activityMultiplier) * 1000 // Convert to grams
        }
    
    // BARF component calculations (in grams)
    val meatAmount: Double
        get() = dailyFoodAmount * 0.70 // 70% muscle meat
    
    val boneAmount: Double
        get() = dailyFoodAmount * 0.10 // 10% raw meaty bones
    
    val organAmount: Double
        get() = dailyFoodAmount * 0.10 // 10% organs (5% liver, 5% other)
    
    val vegetableAmount: Double
        get() = dailyFoodAmount * 0.10 // 10% vegetables/fruits
    
    // Detailed organ breakdown
    val liverAmount: Double
        get() = organAmount * 0.5 // 50% of organs should be liver
    
    val otherOrganAmount: Double
        get() = organAmount * 0.5 // 50% other organs (kidney, spleen, etc.)
    
    // Weekly amounts (some items fed weekly rather than daily)
    val weeklyFishAmount: Double
        get() = dogWeight * 20 // 20g per kg body weight per week
    
    val weeklyEggAmount: Int
        get() = when {
            dogWeight < 10 -> 1
            dogWeight < 25 -> 2
            else -> 3
        }
}

enum class DogAge {
    PUPPY, // < 1 year
    JUNIOR, // 1-2 years
    ADULT,  // 2-7 years
    SENIOR  // > 7 years
}

enum class SpecialNeed {
    WEIGHT_LOSS,
    WEIGHT_GAIN,
    KIDNEY_DISEASE,
    LIVER_DISEASE,
    ALLERGIES,
    DIGESTIVE_ISSUES
}

data class NutritionRecommendation(
    val type: RecommendationType,
    val message: String,
    val severity: RecommendationSeverity
)

enum class RecommendationType {
    INCREASE_CALORIES,
    DECREASE_CALORIES,
    INCREASE_PROTEIN,
    DECREASE_PROTEIN,
    INCREASE_FAT,
    DECREASE_FAT,
    REDUCE_TREATS,
    ADD_SUPPLEMENTS,
    ADJUST_PORTIONS
}

enum class RecommendationSeverity {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Daily treat budget tracker
 */
data class TreatBudget(
    val dogId: String,
    val date: LocalDate = LocalDate.now(),
    val dailyCalorieLimit: Int, // Usually 10% of daily calories
    val treats: List<TreatEntry> = emptyList()
) {
    val totalTreatCalories: Int
        get() = treats.sumOf { it.calories }
    
    val remainingBudget: Int
        get() = dailyCalorieLimit - totalTreatCalories
    
    val budgetPercentageUsed: Double
        get() = if (dailyCalorieLimit > 0) (totalTreatCalories.toDouble() / dailyCalorieLimit) * 100 else 0.0
    
    fun canAddTreat(calories: Int): Boolean = remainingBudget >= calories
}

data class TreatEntry(
    val name: String,
    val calories: Int,
    val timeGiven: String, // HH:mm format
    val reason: TreatReason = TreatReason.TRAINING
)

enum class TreatReason(val displayName: String) {
    TRAINING("Training"),
    REWARD("Belohnung"),
    MEDICATION("Medikamentengabe"),
    DENTAL("Zahnpflege"),
    ENRICHMENT("Beschäftigung"),
    OTHER("Sonstiges")
}