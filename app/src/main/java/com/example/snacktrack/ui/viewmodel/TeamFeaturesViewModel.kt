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

data class TeamFeaturesUiState(
    val isLoading: Boolean = false,
    val teamId: String = "",
    val upcomingTasks: List<FeedingTask> = emptyList(),
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val consumptionPredictions: List<ConsumptionPrediction> = emptyList(),
    val recentActivities: List<TeamActivity> = emptyList(),
    val statistics: TeamStatistics? = null,
    val currentUserId: String = "",
    val error: String? = null
)

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
                        upcomingTasks = tasks,
                        shoppingItems = shoppingItems,
                        consumptionPredictions = predictions,
                        recentActivities = activities,
                        statistics = stats,
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
                                    task.copy(
                                        status = TaskStatus.COMPLETED,
                                        completedByUserId = state.currentUserId,
                                        completedAt = java.time.LocalDateTime.now()
                                    )
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
    
    fun addShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                val result = teamFeaturesRepository.addItemToShoppingList(
                    _uiState.value.teamId,
                    item,
                    _uiState.value.currentUserId
                )
                
                result.getOrNull()?.let { newItem ->
                    _uiState.update { state ->
                        state.copy(
                            shoppingItems = state.shoppingItems + newItem
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
                                if (item.id == itemId) updatedItem else item
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
    
    fun addPredictionToShoppingList(prediction: ConsumptionPrediction) {
        val shoppingItem = ShoppingItem(
            productName = prediction.foodName,
            brand = prediction.brand,
            quantity = prediction.recommendedOrderQuantity,
            unit = "Packung",
            category = ShoppingCategory.FOOD,
            isUrgent = prediction.daysUntilEmpty <= 3,
            linkedFoodId = prediction.foodId
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
                            recentActivities = state.recentActivities + moreActivities
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