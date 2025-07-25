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
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TeamFeaturesRepository(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : BaseRepository() {
    
    companion object {
        const val FEEDING_TASKS_COLLECTION_ID = "feeding_tasks"
        const val SHOPPING_LISTS_COLLECTION_ID = "shopping_lists"
        const val SHOPPING_ITEMS_COLLECTION_ID = "shopping_items"
        const val TEAM_ACTIVITIES_COLLECTION_ID = "team_activities"
        const val TASK_TEMPLATES_COLLECTION_ID = "task_templates"
        const val TEAM_NOTIFICATIONS_COLLECTION_ID = "team_notifications"
        const val CONSUMPTION_PREDICTIONS_COLLECTION_ID = "consumption_predictions"
    }
    
    private val foodIntakeRepository = FoodIntakeRepository(context)
    
    // Task Management
    
    suspend fun createTask(task: FeedingTask): Result<FeedingTask> = safeApiCall {
        val data = mapOf(
            "teamId" to task.teamId,
            "dogId" to task.dogId,
            "assignedToUserId" to task.assignedToUserId,
            "taskType" to task.taskType.name,
            "scheduledDate" to task.scheduledDate.toString(),
            "scheduledTime" to task.scheduledTime?.toString(),
            "status" to task.status.name,
            "notes" to task.notes,
            "reminderEnabled" to task.reminderEnabled,
            "reminderMinutesBefore" to task.reminderMinutesBefore,
            "recurrenceRule" to task.recurrenceRule?.let { 
                mapOf(
                    "frequency" to it.frequency.name,
                    "interval" to it.interval,
                    "daysOfWeek" to it.daysOfWeek.map { day -> day.name },
                    "endDate" to it.endDate?.toString(),
                    "occurrences" to it.occurrences
                )
            }
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = FEEDING_TASKS_COLLECTION_ID,
            documentId = io.appwrite.ID.unique(),
            data = data
        )
        
        // Log activity
        logActivity(
            TeamActivity(
                teamId = task.teamId,
                userId = task.assignedToUserId ?: "",
                dogId = task.dogId,
                activityType = ActivityType.TASK_ASSIGNED,
                description = "Neue Aufgabe: ${task.taskType.displayName}",
                details = mapOf("taskId" to document.id)
            )
        )
        
        documentToFeedingTask(document)
    }
    
    suspend fun getTasksForTeam(
        teamId: String,
        date: LocalDate? = null,
        status: TaskStatus? = null
    ): Result<List<FeedingTask>> = safeApiCall {
        val queries = mutableListOf(Query.equal("teamId", teamId))
        
        date?.let {
            queries.add(Query.equal("scheduledDate", it.toString()))
        }
        
        status?.let {
            queries.add(Query.equal("status", it.name))
        }
        
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = FEEDING_TASKS_COLLECTION_ID,
            queries = queries
        )
        
        response.documents.map { documentToFeedingTask(it) }
    }
    
    suspend fun completeTask(
        taskId: String,
        userId: String,
        notes: String? = null
    ): Result<FeedingTask> = safeApiCall {
        val data = mapOf(
            "status" to TaskStatus.COMPLETED.name,
            "completedByUserId" to userId,
            "completedAt" to LocalDateTime.now().toString(),
            "notes" to notes
        )
        
        val document = appwriteService.databases.updateDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = FEEDING_TASKS_COLLECTION_ID,
            documentId = taskId,
            data = data
        )
        
        val task = documentToFeedingTask(document)
        
        // Log activity
        logActivity(
            TeamActivity(
                teamId = task.teamId,
                userId = userId,
                dogId = task.dogId,
                activityType = ActivityType.TASK_COMPLETED,
                description = "${task.taskType.displayName} erledigt",
                details = mapOf("taskId" to taskId)
            )
        )
        
        task
    }
    
    suspend fun getUpcomingTasks(
        teamId: String,
        days: Int = 7
    ): Result<List<FeedingTask>> = safeApiCall {
        val endDate = LocalDate.now().plusDays(days.toLong())
        
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = FEEDING_TASKS_COLLECTION_ID,
            queries = listOf(
                Query.equal("teamId", teamId),
                Query.greaterThanEqual("scheduledDate", LocalDate.now().toString()),
                Query.lessThanEqual("scheduledDate", endDate.toString()),
                Query.notEqual("status", TaskStatus.COMPLETED.name)
            )
        )
        
        response.documents.map { documentToFeedingTask(it) }
            .sortedBy { it.scheduledDate }
    }
    
    // Shopping List Management
    
    suspend fun getOrCreateShoppingList(teamId: String): Result<TeamShoppingList> = safeApiCall {
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = SHOPPING_LISTS_COLLECTION_ID,
            queries = listOf(
                Query.equal("teamId", teamId),
                Query.equal("isActive", true)
            )
        )
        
        if (response.documents.isNotEmpty()) {
            documentToShoppingList(response.documents.first())
        } else {
            createShoppingList(teamId)
        }
    }
    
    private suspend fun createShoppingList(teamId: String): TeamShoppingList {
        val data = mapOf(
            "teamId" to teamId,
            "name" to "Team Einkaufsliste",
            "createdAt" to LocalDateTime.now().toString(),
            "lastUpdated" to LocalDateTime.now().toString(),
            "isActive" to true
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = SHOPPING_LISTS_COLLECTION_ID,
            documentId = io.appwrite.ID.unique(),
            data = data
        )
        
        return documentToShoppingList(document)
    }
    
    suspend fun addItemToShoppingList(
        teamId: String,
        item: ShoppingItem,
        userId: String
    ): Result<ShoppingItem> = safeApiCall {
        val shoppingList = getOrCreateShoppingList(teamId).getOrNull()
            ?: throw Exception("Could not get shopping list")
        
        val data = mapOf(
            "shoppingListId" to shoppingList.id,
            "productName" to item.productName,
            "brand" to item.brand,
            "quantity" to item.quantity,
            "unit" to item.unit,
            "category" to item.category.name,
            "addedByUserId" to userId,
            "addedAt" to LocalDateTime.now().toString(),
            "isPurchased" to false,
            "isUrgent" to item.isUrgent,
            "notes" to item.notes,
            "estimatedPrice" to item.estimatedPrice,
            "linkedFoodId" to item.linkedFoodId
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = SHOPPING_ITEMS_COLLECTION_ID,
            documentId = io.appwrite.ID.unique(),
            data = data
        )
        
        // Update shopping list timestamp
        appwriteService.databases.updateDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = SHOPPING_LISTS_COLLECTION_ID,
            documentId = shoppingList.id,
            data = mapOf("lastUpdated" to LocalDateTime.now().toString())
        )
        
        // Log activity
        logActivity(
            TeamActivity(
                teamId = teamId,
                userId = userId,
                activityType = ActivityType.SHOPPING_ITEM_ADDED,
                description = "${item.productName} zur Einkaufsliste hinzugefügt",
                details = mapOf(
                    "itemId" to document.id,
                    "quantity" to item.quantity,
                    "category" to item.category.name
                )
            )
        )
        
        documentToShoppingItem(document)
    }
    
    suspend fun getShoppingItems(
        teamId: String,
        includesPurchased: Boolean = false
    ): Result<List<ShoppingItem>> = safeApiCall {
        val shoppingList = getOrCreateShoppingList(teamId).getOrNull()
            ?: return@safeApiCall emptyList()
        
        val queries = mutableListOf(
            Query.equal("shoppingListId", shoppingList.id)
        )
        
        if (!includesPurchased) {
            queries.add(Query.equal("isPurchased", false))
        }
        
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = SHOPPING_ITEMS_COLLECTION_ID,
            queries = queries
        )
        
        response.documents.map { documentToShoppingItem(it) }
            .sortedBy { it.category.ordinal }
    }
    
    suspend fun markItemAsPurchased(
        itemId: String,
        userId: String
    ): Result<ShoppingItem> = safeApiCall {
        val data = mapOf(
            "isPurchased" to true,
            "purchasedByUserId" to userId,
            "purchasedAt" to LocalDateTime.now().toString()
        )
        
        val document = appwriteService.databases.updateDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = SHOPPING_ITEMS_COLLECTION_ID,
            documentId = itemId,
            data = data
        )
        
        val item = documentToShoppingItem(document)
        
        // Log activity
        logActivity(
            TeamActivity(
                teamId = "", // Would need to fetch from shopping list
                userId = userId,
                activityType = ActivityType.SHOPPING_ITEM_PURCHASED,
                description = "${item.productName} eingekauft",
                details = mapOf("itemId" to itemId)
            )
        )
        
        item
    }
    
    // Smart Shopping List Generation
    
    suspend fun generateShoppingPredictions(teamId: String): Result<List<ConsumptionPrediction>> = safeApiCall {
        val dogs = getDogsByTeam(teamId)
        val predictions = mutableListOf<ConsumptionPrediction>()
        
        dogs.forEach { dog ->
            // Get food intakes for the last 30 days
            val intakes = mutableListOf<FoodIntake>()
            var currentDate = LocalDate.now().minusDays(30)
            val endDate = LocalDate.now()
            while (!currentDate.isAfter(endDate)) {
                val dailyIntakes = foodIntakeRepository.getFoodIntakesForDog(dog.id, currentDate).first()
                intakes.addAll(dailyIntakes)
                currentDate = currentDate.plusDays(1)
            }
            
            // Group by food and calculate average daily consumption
            val consumptionByFood = intakes.groupBy { it.foodId }
                .filter { it.key != null }
                .mapValues { entry ->
                    val totalGrams = entry.value.sumOf { it.amountGram.toDouble() }
                    val days = 30
                    totalGrams / days
                }
            
            consumptionByFood.forEach { (foodId, dailyConsumption) ->
                if (foodId != null && dailyConsumption > 0) {
                    // Get current stock (would need inventory tracking)
                    val currentStock = estimateCurrentStock(foodId, intakes)
                    val daysUntilEmpty = (currentStock / dailyConsumption).toInt()
                    
                    if (daysUntilEmpty <= 7) { // Order when less than a week left
                        predictions.add(
                            ConsumptionPrediction(
                                foodId = foodId,
                                foodName = intakes.first { it.foodId == foodId }.foodName,
                                brand = "", // Would need to fetch from food database
                                currentStock = currentStock,
                                dailyConsumption = dailyConsumption,
                                daysUntilEmpty = daysUntilEmpty,
                                recommendedOrderDate = LocalDate.now().plusDays(maxOf(0, daysUntilEmpty - 3).toLong()),
                                recommendedOrderQuantity = calculateOrderQuantity(dailyConsumption),
                                confidence = 0.8f
                            )
                        )
                    }
                }
            }
        }
        
        predictions.sortedBy { it.daysUntilEmpty }
    }
    
    private fun estimateCurrentStock(foodId: String, recentIntakes: List<FoodIntake>): Double {
        // Simplified estimation - would need proper inventory tracking
        val lastPurchaseDate = LocalDate.now().minusDays(14) // Assume bi-weekly shopping
        val consumedSinceLastPurchase = recentIntakes
            .filter { it.foodId == foodId && it.timestamp.toLocalDate().isAfter(lastPurchaseDate) }
            .sumOf { it.amountGram.toDouble() }
        
        val typicalPackageSize = 5000.0 // 5kg bag
        return maxOf(0.0, typicalPackageSize - consumedSinceLastPurchase)
    }
    
    private fun calculateOrderQuantity(dailyConsumption: Double): Int {
        val monthlyConsumption = dailyConsumption * 30
        return when {
            monthlyConsumption < 2000 -> 1 // 1 small bag
            monthlyConsumption < 5000 -> 1 // 1 large bag
            monthlyConsumption < 10000 -> 2 // 2 large bags
            else -> 3 // 3+ bags
        }
    }
    
    // Activity Feed
    
    suspend fun getTeamActivities(
        teamId: String,
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<TeamActivity>> = safeApiCall {
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = TEAM_ACTIVITIES_COLLECTION_ID,
            queries = listOf(
                Query.equal("teamId", teamId),
                Query.orderDesc("timestamp"),
                Query.limit(limit),
                Query.offset(offset)
            )
        )
        
        response.documents.map { documentToTeamActivity(it) }
    }
    
    suspend fun getActivityFeed(
        teamId: String,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        activityTypes: List<ActivityType>? = null
    ): Result<List<TeamActivity>> = safeApiCall {
        val queries = mutableListOf(Query.equal("teamId", teamId))
        
        startDate?.let {
            queries.add(Query.greaterThanEqual("timestamp", it.toString()))
        }
        
        endDate?.let {
            queries.add(Query.lessThanEqual("timestamp", it.toString()))
        }
        
        activityTypes?.let {
            queries.add(Query.equal("activityType", it.map { type -> type.name }))
        }
        
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = TEAM_ACTIVITIES_COLLECTION_ID,
            queries = queries
        )
        
        response.documents.map { documentToTeamActivity(it) }
            .sortedByDescending { it.timestamp }
    }
    
    private suspend fun logActivity(activity: TeamActivity) {
        try {
            val data = mapOf(
                "teamId" to activity.teamId,
                "userId" to activity.userId,
                "dogId" to activity.dogId,
                "activityType" to activity.activityType.name,
                "timestamp" to activity.timestamp.toString(),
                "description" to activity.description,
                "details" to activity.details,
                "isImportant" to activity.isImportant
            )
            
            appwriteService.databases.createDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = TEAM_ACTIVITIES_COLLECTION_ID,
                documentId = io.appwrite.ID.unique(),
                data = data
            )
        } catch (e: Exception) {
            // Log error but don't fail the main operation
        }
    }
    
    // Team Statistics
    
    suspend fun getTeamStatistics(
        teamId: String,
        period: StatisticsPeriod = StatisticsPeriod.WEEK
    ): Result<TeamStatistics> = safeApiCall {
        val (startDate, endDate) = getDateRangeForPeriod(period)
        
        val activities = getActivityFeed(
            teamId, 
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59)
        ).getOrNull() ?: emptyList()
        
        val tasks = getTasksForTeam(teamId).getOrNull() ?: emptyList()
        
        // Calculate member contributions
        val memberContributions = activities.groupBy { it.userId }
            .mapValues { (userId, userActivities) ->
                MemberContribution(
                    userId = userId,
                    tasksCompleted = userActivities.count { it.activityType == ActivityType.TASK_COMPLETED },
                    feedingsGiven = userActivities.count { it.activityType == ActivityType.FEEDING },
                    shoppingItemsPurchased = userActivities.count { it.activityType == ActivityType.SHOPPING_ITEM_PURCHASED },
                    activitiesLogged = userActivities.size,
                    contributionScore = calculateContributionScore(userActivities)
                )
            }
        
        // Calculate task completion rate
        val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
        val totalTasks = tasks.size
        val taskCompletionRate = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
        
        // Calculate most active times
        val mostActiveTimes = activities
            .map { it.timestamp.hour }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
        
        TeamStatistics(
            teamId = teamId,
            period = period,
            startDate = startDate,
            endDate = endDate,
            memberContributions = memberContributions,
            taskCompletionRate = taskCompletionRate,
            averageResponseTime = calculateAverageResponseTime(tasks),
            mostActiveTimes = mostActiveTimes,
            totalActivities = activities.size
        )
    }
    
    private fun getDateRangeForPeriod(period: StatisticsPeriod): Pair<LocalDate, LocalDate> {
        val endDate = LocalDate.now()
        val startDate = when (period) {
            StatisticsPeriod.DAY -> endDate
            StatisticsPeriod.WEEK -> endDate.minusDays(6)
            StatisticsPeriod.MONTH -> endDate.minusDays(29)
            StatisticsPeriod.QUARTER -> endDate.minusDays(89)
            StatisticsPeriod.YEAR -> endDate.minusDays(364)
        }
        return startDate to endDate
    }
    
    private fun calculateContributionScore(activities: List<TeamActivity>): Float {
        // Simple scoring: different activities have different weights
        val weights = mapOf(
            ActivityType.FEEDING to 3,
            ActivityType.MEDICATION_GIVEN to 5,
            ActivityType.VET_VISIT to 10,
            ActivityType.TASK_COMPLETED to 2,
            ActivityType.SHOPPING_ITEM_PURCHASED to 2,
            ActivityType.WEIGHT_ENTRY to 1
        )
        
        val totalScore = activities.sumOf { activity ->
            weights[activity.activityType] ?: 1
        }
        
        // Normalize to 0-100
        return minOf(100f, totalScore.toFloat() * 2)
    }
    
    private fun calculateAverageResponseTime(tasks: List<FeedingTask>): Int {
        val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED && it.completedAt != null }
        
        if (completedTasks.isEmpty()) return 0
        
        val responseTimes = completedTasks.mapNotNull { task ->
            task.completedAt?.let { completed ->
                val scheduled = task.scheduledDate.atTime(task.scheduledTime ?: LocalTime.NOON)
                val minutesDiff = java.time.Duration.between(scheduled, completed).toMinutes()
                if (minutesDiff >= 0) minutesDiff.toInt() else null
            }
        }
        
        return if (responseTimes.isNotEmpty()) responseTimes.average().toInt() else 0
    }
    
    // Helper functions
    
    private suspend fun getDogsByTeam(teamId: String): List<Dog> {
        // This would need to be implemented in DogRepository
        return emptyList()
    }
    
    private fun documentToFeedingTask(document: Document<Map<String, Any>>): FeedingTask {
        val recurrenceData = document.data["recurrenceRule"] as? Map<String, Any>
        
        return FeedingTask(
            id = document.id,
            teamId = document.data["teamId"] as String,
            dogId = document.data["dogId"] as String,
            assignedToUserId = document.data["assignedToUserId"] as? String,
            taskType = TaskType.valueOf(document.data["taskType"] as String),
            scheduledDate = LocalDate.parse(document.data["scheduledDate"] as String),
            scheduledTime = (document.data["scheduledTime"] as? String)?.let { LocalTime.parse(it) },
            status = TaskStatus.valueOf(document.data["status"] as String),
            completedByUserId = document.data["completedByUserId"] as? String,
            completedAt = (document.data["completedAt"] as? String)?.let { LocalDateTime.parse(it) },
            notes = document.data["notes"] as? String,
            reminderEnabled = document.data["reminderEnabled"] as Boolean,
            reminderMinutesBefore = (document.data["reminderMinutesBefore"] as Number).toInt(),
            recurrenceRule = recurrenceData?.let { data ->
                RecurrenceRule(
                    frequency = RecurrenceFrequency.valueOf(data["frequency"] as String),
                    interval = (data["interval"] as Number).toInt(),
                    daysOfWeek = (data["daysOfWeek"] as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.map { DayOfWeek.valueOf(it) } ?: emptyList(),
                    endDate = (data["endDate"] as? String)?.let { LocalDate.parse(it) },
                    occurrences = (data["occurrences"] as? Number)?.toInt()
                )
            }
        )
    }
    
    private fun documentToShoppingList(document: Document<Map<String, Any>>): TeamShoppingList {
        return TeamShoppingList(
            id = document.id,
            teamId = document.data["teamId"] as String,
            name = document.data["name"] as String,
            createdAt = LocalDateTime.parse(document.data["createdAt"] as String),
            lastUpdated = LocalDateTime.parse(document.data["lastUpdated"] as String),
            isActive = document.data["isActive"] as Boolean
        )
    }
    
    private fun documentToShoppingItem(document: Document<Map<String, Any>>): ShoppingItem {
        return ShoppingItem(
            id = document.id,
            productName = document.data["productName"] as String,
            brand = document.data["brand"] as? String,
            quantity = (document.data["quantity"] as Number).toInt(),
            unit = document.data["unit"] as String,
            category = ShoppingCategory.valueOf(document.data["category"] as String),
            addedByUserId = document.data["addedByUserId"] as String,
            addedAt = LocalDateTime.parse(document.data["addedAt"] as String),
            purchasedByUserId = document.data["purchasedByUserId"] as? String,
            purchasedAt = (document.data["purchasedAt"] as? String)?.let { LocalDateTime.parse(it) },
            isPurchased = document.data["isPurchased"] as Boolean,
            isUrgent = document.data["isUrgent"] as Boolean,
            notes = document.data["notes"] as? String,
            estimatedPrice = (document.data["estimatedPrice"] as? Number)?.toDouble(),
            linkedFoodId = document.data["linkedFoodId"] as? String
        )
    }
    
    private fun documentToTeamActivity(document: Document<Map<String, Any>>): TeamActivity {
        return TeamActivity(
            id = document.id,
            teamId = document.data["teamId"] as String,
            userId = document.data["userId"] as String,
            dogId = document.data["dogId"] as? String,
            activityType = ActivityType.valueOf(document.data["activityType"] as String),
            timestamp = LocalDateTime.parse(document.data["timestamp"] as String),
            description = document.data["description"] as String,
            details = (document.data["details"] as? Map<String, Any>) ?: emptyMap(),
            isImportant = document.data["isImportant"] as Boolean
        )
    }
}