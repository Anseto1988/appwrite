package com.example.snacktrack.data.model

/**
 * Prevention-related enums to avoid redeclaration conflicts
 */

// HealthEntryType moved to DogHealthEntry.kt to avoid duplication

enum class PreventionCategory {
    NUTRITION,
    EXERCISE,
    MEDICAL,
    DENTAL,
    GROOMING,
    BEHAVIOR,
    ENVIRONMENTAL,
    EMERGENCY_PREP
}

// RecommendationPriority moved to HealthModels.kt to avoid duplication

// Additional prevention-related enums
enum class PreventionTask {
    WEIGHT_MONITORING,
    DENTAL_CARE,
    VACCINATION_REMINDER,
    EXERCISE_TRACKING,
    NUTRITION_ADJUSTMENT,
    HEALTH_SCREENING
}

enum class PreventionActivity {
    DAILY_WALK,
    TEETH_BRUSHING,
    WEIGHT_CHECK,
    MEDICATION_ADMIN,
    GROOMING_SESSION,
    TRAINING_SESSION
}

// UI State for prevention dashboard
data class PreventionUiState(
    val isLoading: Boolean = false,
    val tasks: List<PreventionTask> = emptyList(),
    val recommendations: List<PreventionRecommendation> = emptyList(),
    val error: String? = null
)