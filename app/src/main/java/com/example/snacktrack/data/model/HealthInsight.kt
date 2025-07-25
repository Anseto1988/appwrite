package com.example.snacktrack.data.model

import java.time.LocalDateTime

data class HealthInsight(
    val id: String,
    val type: HealthInsightType,
    val title: String,
    val description: String,
    val severity: HealthInsightSeverity,
    val timestamp: LocalDateTime,
    val recommendedAction: String? = null,
    val relatedData: Map<String, Any> = emptyMap()
)

enum class HealthInsightType {
    WEIGHT_TREND,
    NUTRITION_BALANCE,
    ACTIVITY_PATTERN,
    HEALTH_RISK,
    MEDICATION_REMINDER,
    VETERINARY_CHECKUP,
    BEHAVIORAL_CHANGE,
    DIETARY_ALERT
}

enum class HealthInsightSeverity {
    INFO,
    WARNING,
    CRITICAL
}