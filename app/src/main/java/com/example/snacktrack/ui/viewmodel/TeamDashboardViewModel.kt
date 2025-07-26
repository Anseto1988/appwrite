package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeamDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TeamStatistics(
    val totalDogs: Int = 0,
    val activeTasks: Int = 0,
    val completedTasks: Int = 0,
    val memberCount: Int = 0
)

data class TeamTask(
    val id: String,
    val title: String,
    val assignedTo: String,
    val dueDate: String,
    val isCompleted: Boolean = false
)

data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: String,
    val isPurchased: Boolean = false
)

data class ConsumptionPrediction(
    val dogName: String,
    val foodName: String,
    val daysRemaining: Int,
    val recommendedPurchaseDate: String
)

data class TeamActivity(
    val id: String,
    val type: String,
    val description: String,
    val timestamp: String,
    val userId: String
)

class TeamDashboardViewModel(
    private val context: Context,
    private val teamId: String
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TeamDashboardUiState())
    val uiState: StateFlow<TeamDashboardUiState> = _uiState.asStateFlow()
    
    private val _statistics = MutableStateFlow(TeamStatistics())
    val statistics: StateFlow<TeamStatistics> = _statistics.asStateFlow()
    
    private val _upcomingTasks = MutableStateFlow<List<TeamTask>>(emptyList())
    val upcomingTasks: StateFlow<List<TeamTask>> = _upcomingTasks.asStateFlow()
    
    private val _shoppingItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems.asStateFlow()
    
    private val _consumptionPredictions = MutableStateFlow<List<ConsumptionPrediction>>(emptyList())
    val consumptionPredictions: StateFlow<List<ConsumptionPrediction>> = _consumptionPredictions.asStateFlow()
    
    private val _recentActivities = MutableStateFlow<List<TeamActivity>>(emptyList())
    val recentActivities: StateFlow<List<TeamActivity>> = _recentActivities.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadTeamData()
    }
    
    fun loadTeamData() {
        viewModelScope.launch {
            _isLoading.value = true
            // TODO: Load team data from repository
            _statistics.value = TeamStatistics(
                totalDogs = 0,
                activeTasks = 0,
                completedTasks = 0,
                memberCount = 0
            )
            _upcomingTasks.value = emptyList()
            _shoppingItems.value = emptyList()
            _consumptionPredictions.value = emptyList()
            _recentActivities.value = emptyList()
            _isLoading.value = false
        }
    }
    
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            // TODO: Mark task as completed
        }
    }
    
    fun markItemAsPurchased(itemId: String) {
        viewModelScope.launch {
            // TODO: Mark shopping item as purchased
        }
    }
    
    fun addPredictionToShoppingList(prediction: ConsumptionPrediction) {
        viewModelScope.launch {
            // TODO: Add prediction to shopping list
        }
    }
    
    fun loadMoreActivities() {
        viewModelScope.launch {
            // TODO: Load more activities
        }
    }
}