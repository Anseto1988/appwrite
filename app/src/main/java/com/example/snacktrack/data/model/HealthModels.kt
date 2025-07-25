package com.example.snacktrack.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Gesundheitseintrag für einen Hund
 */
data class HealthEntry(
    val id: String = "",
    val dogId: String,
    val type: HealthEntryType,
    val date: LocalDate,
    val title: String,
    val description: String = "",
    val value: Double? = null,  // z.B. Gewicht, Temperatur
    val unit: String? = null,   // z.B. kg, °C
    val veterinarianId: String? = null,
    val attachments: List<String> = emptyList(),
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Präventionsaufgabe
 */
data class PreventionTask(
    val id: String = "",
    val dogId: String? = null,
    val title: String,
    val description: String = "",
    val category: PreventionCategory,
    val priority: RecommendationPriority,
    val dueDate: LocalDate? = null,
    val isCompleted: Boolean = false,
    val completedDate: LocalDate? = null,
    val isRecurring: Boolean = false,
    val recurrenceInterval: SyncInterval? = null,
    val reminders: List<LocalDateTime> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Präventionsaktivität
 */
data class PreventionActivity(
    val id: String = "",
    val dogId: String? = null,
    val type: String,
    val title: String,
    val description: String = "",
    val date: LocalDateTime,
    val duration: Int? = null, // in Minuten
    val notes: String? = null,
    val taskId: String? = null, // Verknüpfung zu PreventionTask
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * UI State für Prevention Screen
 */
data class PreventionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val upcomingTasks: List<PreventionTask> = emptyList(),
    val recentActivities: List<PreventionActivity> = emptyList(),
    val overallRiskScore: Double = 0.0,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val recommendations: List<PreventionRecommendation> = emptyList()
)

/**
 * Präventionsempfehlung
 */
data class PreventionRecommendation(
    val id: String = "",
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val actionItems: List<String> = emptyList(),
    val relatedTasks: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Gewichtseintrag
 */
data class WeightEntry(
    val date: LocalDate,
    val weight: Double
)

/**
 * Datumsbereich für Analysen
 */
data class DateRange(
    val start: LocalDate,
    val end: LocalDate
) {
    fun contains(date: LocalDate): Boolean {
        return !date.isBefore(start) && !date.isAfter(end)
    }
    
    fun getDays(): Long {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end)
    }
}

/**
 * Hunderasse
 */
data class Breed(
    val id: String,
    val name: String,
    val nameGerman: String,
    val category: String,
    val size: DogSize,
    val averageWeight: Double,
    val averageLifespan: Int,
    val exerciseNeeds: ActivityLevel,
    val groomingNeeds: String,
    val healthRisks: List<String> = emptyList(),
    val temperament: List<String> = emptyList()
)

/**
 * Gesundheitstrend
 */
data class HealthTrend(
    val metric: String,
    val trend: TrendDirection,
    val changePercent: Double,
    val periodDays: Int
)