package com.example.snacktrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.TeamFeaturesRepository
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

// Import the UI data classes from TeamDashboardViewModel
typealias UiTeamStatistics = com.example.snacktrack.ui.viewmodel.TeamStatistics
typealias UiTeamTask = com.example.snacktrack.ui.viewmodel.TeamTask
typealias UiShoppingItem = com.example.snacktrack.ui.viewmodel.ShoppingItem
typealias UiConsumptionPrediction = com.example.snacktrack.ui.viewmodel.ConsumptionPrediction
typealias UiTeamActivity = com.example.snacktrack.ui.viewmodel.TeamActivity

data class TeamFeaturesUiState(
    val isLoading: Boolean = false,
    val teamId: String = "",
    val upcomingTasks: List<UiTeamTask> = emptyList(),
    val shoppingItems: List<UiShoppingItem> = emptyList(),
    val consumptionPredictions: List<UiConsumptionPrediction> = emptyList(),
    val recentActivities: List<UiTeamActivity> = emptyList(),
    val statistics: UiTeamStatistics? = null,
    val currentUserId: String = "",
    val error: String? = null
)

// Mapping functions to convert between model and UI classes
private fun convertToUiShoppingItems(items: List<com.example.snacktrack.data.model.ShoppingItem>): List<UiShoppingItem> {
    return items.map { item ->
        UiShoppingItem(
            id = item.id,
            name = item.productName,
            quantity = "${item.quantity} ${item.unit}",
            isPurchased = item.isPurchased
        )
    }
}

private fun convertToUiConsumptionPredictions(predictions: List<com.example.snacktrack.data.model.ConsumptionPrediction>): List<UiConsumptionPrediction> {
    return predictions.map { prediction ->
        UiConsumptionPrediction(
            dogName = "Dog", // TODO: Get dog name from dogId
            foodName = prediction.foodName,
            daysRemaining = prediction.daysUntilEmpty,
            recommendedPurchaseDate = prediction.recommendedOrderDate.toString()
        )
    }
}

private fun convertToUiTeamActivities(activities: List<com.example.snacktrack.data.model.TeamActivity>): List<UiTeamActivity> {
    return activities.map { activity ->
        UiTeamActivity(
            id = activity.id,
            type = activity.activityType.name,
            description = activity.description,
            timestamp = activity.timestamp.toString(),
            userId = activity.userId
        )
    }
}

private fun convertToUiTeamStatistics(stats: com.example.snacktrack.data.model.TeamStatistics?): UiTeamStatistics? {
    return stats?.let {
        UiTeamStatistics(
            totalDogs = 0, // TODO: Calculate from team dogs
            activeTasks = 0, // TODO: Calculate from active tasks
            completedTasks = 0, // TODO: Calculate from completed tasks
            memberCount = it.memberContributions.size
        )
    }
}

private fun convertFromUiShoppingItem(item: UiShoppingItem): com.example.snacktrack.data.model.ShoppingItem {
    return com.example.snacktrack.data.model.ShoppingItem(
        id = item.id,
        productName = item.name,
        quantity = item.quantity.split(" ").firstOrNull()?.toIntOrNull() ?: 1,
        unit = item.quantity.split(" ").getOrNull(1) ?: "Stück",
        isPurchased = item.isPurchased
    )
}

class TeamFeaturesViewModel(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val teamFeaturesRepository = TeamFeaturesRepository(context, appwriteService)
    
    private val _uiState = MutableStateFlow(TeamFeaturesUiState())
    val uiState: StateFlow<TeamFeaturesUiState> = _uiState.asStateFlow()
    
    private var activityOffset = 0
    private val activityLimit = 20
    
    fun loadTeamData(teamId: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    teamId = teamId,
                    currentUserId = appwriteService.getCurrentUserId() ?: ""
                ) 
            }
            
            try {
                // Load all team data in parallel
                val tasksResult = teamFeaturesRepository.getUpcomingTasks(teamId)
                val tasks = tasksResult.getOrNull() ?: emptyList()
                
                val shoppingResult = teamFeaturesRepository.getShoppingItems(teamId)
                val shoppingItems = shoppingResult.getOrNull() ?: emptyList()
                
                val predictionsResult = teamFeaturesRepository.generateShoppingPredictions(teamId)
                val predictions = predictionsResult.getOrNull() ?: emptyList()
                
                val activitiesResult = teamFeaturesRepository.getTeamActivities(
                    teamId, 
                    limit = activityLimit
                )
                val activities = activitiesResult.getOrNull() ?: emptyList()
                
                val statsResult = teamFeaturesRepository.getTeamStatistics(
                    teamId,
                    StatisticsPeriod.WEEK
                )
                val stats = statsResult.getOrNull()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        upcomingTasks = emptyList(), // TODO: Convert FeedingTask to UiTeamTask
                        shoppingItems = convertToUiShoppingItems(shoppingItems),
                        consumptionPredictions = convertToUiConsumptionPredictions(predictions),
                        recentActivities = convertToUiTeamActivities(activities),
                        statistics = convertToUiTeamStatistics(stats),
                        error = null
                    )
                }
                
                activityOffset = activities.size
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun createTask(task: FeedingTask) {
        viewModelScope.launch {
            try {
                val result = teamFeaturesRepository.createTask(task)
                result.getOrNull()?.let {
                    loadTeamData(_uiState.value.teamId) // Reload to update UI
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun completeTask(taskId: String, notes: String?) {
        viewModelScope.launch {
            try {
                val result = teamFeaturesRepository.completeTask(
                    taskId,
                    _uiState.value.currentUserId,
                    notes
                )
                
                result.getOrNull()?.let {
                    // Update local state
                    _uiState.update { state ->
                        state.copy(
                            upcomingTasks = state.upcomingTasks.map { task ->
                                if (task.id == taskId) {
                                    task.copy(isCompleted = true)
                                } else task
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun addShoppingItem(item: UiShoppingItem) {
        viewModelScope.launch {
            try {
                val result = teamFeaturesRepository.addItemToShoppingList(
                    _uiState.value.teamId,
                    convertFromUiShoppingItem(item),
                    _uiState.value.currentUserId
                )
                
                result.getOrNull()?.let { newItem ->
                    _uiState.update { state ->
                        state.copy(
                            shoppingItems = state.shoppingItems + convertToUiShoppingItems(listOf(newItem))
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun markItemAsPurchased(itemId: String) {
        viewModelScope.launch {
            try {
                val result = teamFeaturesRepository.markItemAsPurchased(
                    itemId,
                    _uiState.value.currentUserId
                )
                
                result.getOrNull()?.let { updatedItem ->
                    _uiState.update { state ->
                        state.copy(
                            shoppingItems = state.shoppingItems.map { item ->
                                if (item.id == itemId) convertToUiShoppingItems(listOf(updatedItem)).first() else item
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun addPredictionToShoppingList(prediction: UiConsumptionPrediction) {
        val shoppingItem = UiShoppingItem(
            id = "temp_${System.currentTimeMillis()}",
            name = prediction.foodName,
            quantity = "1 Packung",
            isPurchased = false
        )
        
        addShoppingItem(shoppingItem)
    }
    
    fun loadMoreActivities() {
        viewModelScope.launch {
            try {
                val result = teamFeaturesRepository.getTeamActivities(
                    _uiState.value.teamId,
                    limit = activityLimit,
                    offset = activityOffset
                )
                
                result.getOrNull()?.let { moreActivities ->
                    _uiState.update { state ->
                        state.copy(
                            recentActivities = state.recentActivities + convertToUiTeamActivities(moreActivities)
                        )
                    }
                    activityOffset += moreActivities.size
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun refreshData() {
        loadTeamData(_uiState.value.teamId)
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

class TeamFeaturesViewModelFactory(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamFeaturesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamFeaturesViewModel(context, appwriteService, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}