package com.example.snacktrack.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Task assignment for team members
 */
data class FeedingTask(
    val id: String = "",
    val teamId: String = "",
    val dogId: String = "",
    val assignedToUserId: String? = null,
    val taskType: TaskType = TaskType.FEEDING,
    val scheduledDate: LocalDate = LocalDate.now(),
    val scheduledTime: LocalTime? = null,
    val status: TaskStatus = TaskStatus.PENDING,
    val completedByUserId: String? = null,
    val completedAt: LocalDateTime? = null,
    val notes: String? = null,
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 30,
    val recurrenceRule: RecurrenceRule? = null
)

enum class TaskType(val displayName: String, val icon: String) {
    FEEDING("Fütterung", "🍖"),
    MEDICATION("Medikament", "💊"),
    WALK("Gassi gehen", "🚶"),
    VET_APPOINTMENT("Tierarzttermin", "🏥"),
    GROOMING("Pflege", "✂️"),
    TRAINING("Training", "🎾"),
    WEIGHT_CHECK("Gewichtskontrolle", "⚖️"),
    OTHER("Sonstiges", "📋")
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE,
    CANCELLED
}

data class RecurrenceRule(
    val frequency: RecurrenceFrequency = RecurrenceFrequency.DAILY,
    val interval: Int = 1, // Every N days/weeks/months
    val daysOfWeek: List<DayOfWeek> = emptyList(), // For weekly
    val endDate: LocalDate? = null,
    val occurrences: Int? = null // Max number of occurrences
)

enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

enum class DayOfWeek(val displayName: String) {
    MONDAY("Montag"),
    TUESDAY("Dienstag"),
    WEDNESDAY("Mittwoch"),
    THURSDAY("Donnerstag"),
    FRIDAY("Freitag"),
    SATURDAY("Samstag"),
    SUNDAY("Sonntag")
}

/**
 * Shared shopping list for team
 */
data class TeamShoppingList(
    val id: String = "",
    val teamId: String = "",
    val name: String = "Team Einkaufsliste",
    val items: List<ShoppingItem> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true
)

data class ShoppingItem(
    val id: String = "",
    val productName: String = "",
    val brand: String? = null,
    val quantity: Int = 1,
    val unit: String = "Stück",
    val category: ShoppingCategory = ShoppingCategory.FOOD,
    val addedByUserId: String = "",
    val addedAt: LocalDateTime = LocalDateTime.now(),
    val purchasedByUserId: String? = null,
    val purchasedAt: LocalDateTime? = null,
    val isPurchased: Boolean = false,
    val isUrgent: Boolean = false,
    val notes: String? = null,
    val estimatedPrice: Double? = null,
    val linkedFoodId: String? = null // Link to food database
)

enum class ShoppingCategory(val displayName: String, val emoji: String) {
    FOOD("Futter", "🥩"),
    TREATS("Leckerlis", "🦴"),
    MEDICATION("Medikamente", "💊"),
    SUPPLEMENTS("Nahrungsergänzung", "💚"),
    TOYS("Spielzeug", "🎾"),
    GROOMING("Pflege", "🧼"),
    ACCESSORIES("Zubehör", "🎀"),
    OTHER("Sonstiges", "📦")
}

/**
 * Team activity feed
 */
data class TeamActivity(
    val id: String = "",
    val teamId: String = "",
    val userId: String = "",
    val dogId: String? = null,
    val activityType: ActivityType = ActivityType.FEEDING,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val description: String = "",
    val details: Map<String, Any> = emptyMap(),
    val isImportant: Boolean = false
)

enum class ActivityType(val displayName: String, val icon: String) {
    FEEDING("Hat gefüttert", "🍖"),
    WEIGHT_ENTRY("Gewicht eingetragen", "⚖️"),
    MEDICATION_GIVEN("Medikament verabreicht", "💊"),
    VET_VISIT("Tierarztbesuch", "🏥"),
    WALK_COMPLETED("Gassi gegangen", "🚶"),
    TASK_COMPLETED("Aufgabe erledigt", "✅"),
    TASK_ASSIGNED("Aufgabe zugewiesen", "📋"),
    SHOPPING_ITEM_ADDED("Einkaufsliste ergänzt", "🛒"),
    SHOPPING_ITEM_PURCHASED("Eingekauft", "✓"),
    DOG_ADDED("Hund hinzugefügt", "🐕"),
    TEAM_MEMBER_JOINED("Team beigetreten", "👥"),
    HEALTH_ISSUE_REPORTED("Gesundheitsproblem gemeldet", "⚠️"),
    MILESTONE_REACHED("Meilenstein erreicht", "🎉")
}

/**
 * Team statistics and insights
 */
data class TeamStatistics(
    val teamId: String = "",
    val period: StatisticsPeriod = StatisticsPeriod.WEEK,
    val startDate: LocalDate = LocalDate.now().minusDays(7),
    val endDate: LocalDate = LocalDate.now(),
    val memberContributions: Map<String, MemberContribution> = emptyMap(),
    val taskCompletionRate: Float = 0f,
    val averageResponseTime: Int = 0, // minutes
    val mostActiveTimes: List<Int> = emptyList(), // hours of day
    val totalActivities: Int = 0
)

data class MemberContribution(
    val userId: String = "",
    val tasksCompleted: Int = 0,
    val feedingsGiven: Int = 0,
    val shoppingItemsPurchased: Int = 0,
    val activitiesLogged: Int = 0,
    val contributionScore: Float = 0f // 0-100
)

enum class StatisticsPeriod {
    DAY,
    WEEK,
    MONTH,
    QUARTER,
    YEAR
}

/**
 * Smart shopping list generation based on consumption
 */
data class ConsumptionPrediction(
    val foodId: String = "",
    val foodName: String = "",
    val brand: String = "",
    val currentStock: Double = 0.0, // in grams
    val dailyConsumption: Double = 0.0, // average grams per day
    val daysUntilEmpty: Int = 0,
    val recommendedOrderDate: LocalDate = LocalDate.now(),
    val recommendedOrderQuantity: Int = 1,
    val confidence: Float = 0f // 0-1
)

/**
 * Team notification preferences
 */
data class TeamNotificationSettings(
    val userId: String = "",
    val teamId: String = "",
    val taskReminders: Boolean = true,
    val taskAssignments: Boolean = true,
    val taskCompletions: Boolean = false,
    val shoppingListUpdates: Boolean = true,
    val urgentAlerts: Boolean = true,
    val dailySummary: Boolean = false,
    val weeklySummary: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val quietHoursStart: LocalTime = LocalTime.of(22, 0),
    val quietHoursEnd: LocalTime = LocalTime.of(7, 0)
)

/**
 * Task templates for common activities
 */
data class TaskTemplate(
    val id: String = "",
    val teamId: String = "",
    val name: String = "",
    val taskType: TaskType = TaskType.FEEDING,
    val defaultTime: LocalTime? = null,
    val defaultDuration: Int? = null, // minutes
    val defaultNotes: String? = null,
    val recurrenceRule: RecurrenceRule? = null,
    val isActive: Boolean = true
)

/**
 * Team roles and permissions
 */
data class TeamMemberRole(
    val userId: String = "",
    val teamId: String = "",
    val role: TeamRole = TeamRole.VIEWER,
    val permissions: Set<TeamPermission> = emptySet(),
    val joinedAt: LocalDateTime = LocalDateTime.now()
)

// TeamRole is defined in Team.kt as an alias to BasicTeamRole

enum class TeamPermission {
    MANAGE_TEAM,
    MANAGE_DOGS,
    ASSIGN_TASKS,
    COMPLETE_TASKS,
    MANAGE_SHOPPING_LIST,
    VIEW_STATISTICS,
    MANAGE_NOTIFICATIONS,
    INVITE_MEMBERS,
    REMOVE_MEMBERS
}