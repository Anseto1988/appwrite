package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.FoodIntake
import com.example.snacktrack.data.model.WeightEntry
import com.example.snacktrack.data.repository.FoodIntakeRepository
import com.example.snacktrack.data.repository.WeightRepository
import io.appwrite.Query
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val dailyAverageCalories: Int = 0,
    val weeklyAverageCalories: Int = 0,
    val monthlyAverageCalories: Int = 0,
    val weightEntries: List<WeightEntry> = emptyList(),
    val mostFrequentFoods: List<FoodFrequency> = emptyList(),
    val dailyCalorieData: List<DailyCalories> = emptyList(),
    val feedingTimeDistribution: Map<Int, Int> = emptyMap(), // Hour -> Count
    val totalFoodIntakes: Int = 0,
    val averageDailyIntakes: Double = 0.0,
    val periodDays: Int = 7, // Number of days in the statistics period
    val errorMessage: String? = null
)

data class FoodFrequency(
    val foodName: String,
    val count: Int,
    val totalCalories: Int
)

data class DailyCalories(
    val date: LocalDate,
    val calories: Int
)

class StatisticsViewModel(
    private val context: Context,
    private val dogId: String
) : ViewModel() {
    
    private val foodIntakeRepository = FoodIntakeRepository(context)
    private val weightRepository = WeightRepository(context)
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Load weight history
                weightRepository.getWeightHistory(dogId).collect { weightEntries ->
                    _uiState.update { it.copy(weightEntries = weightEntries) }
                }
                
                // Load food intake statistics
                loadFoodIntakeStatistics()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Fehler beim Laden der Statistiken: ${e.message}"
                    )
                }
            }
        }
    }
    
    private suspend fun loadFoodIntakeStatistics() {
        val endDate = LocalDate.now()
        val startDate30Days = endDate.minusDays(30)
        val startDate7Days = endDate.minusDays(7)
        
        val allIntakes = mutableListOf<FoodIntake>()
        val dailyCaloriesMap = mutableMapOf<LocalDate, Int>()
        val foodFrequencyMap = mutableMapOf<String, FoodFrequency>()
        val feedingHoursMap = mutableMapOf<Int, Int>()
        
        // Collect data for the last 30 days
        var currentDate = startDate30Days
        while (!currentDate.isAfter(endDate)) {
            foodIntakeRepository.getFoodIntakesForDog(dogId, currentDate).first().forEach { intake ->
                allIntakes.add(intake)
                
                // Aggregate daily calories
                dailyCaloriesMap[currentDate] = dailyCaloriesMap.getOrDefault(currentDate, 0) + intake.calories
                
                // Track food frequency
                val existing = foodFrequencyMap[intake.foodName]
                foodFrequencyMap[intake.foodName] = FoodFrequency(
                    foodName = intake.foodName,
                    count = (existing?.count ?: 0) + 1,
                    totalCalories = (existing?.totalCalories ?: 0) + intake.calories
                )
                
                // Track feeding times
                val hour = intake.timestamp.hour
                feedingHoursMap[hour] = feedingHoursMap.getOrDefault(hour, 0) + 1
            }
            currentDate = currentDate.plusDays(1)
        }
        
        // Calculate averages
        val last30DaysCalories = allIntakes.sumOf { it.calories }
        val last7DaysCalories = allIntakes.filter { 
            !it.timestamp.toLocalDate().isBefore(startDate7Days) 
        }.sumOf { it.calories }
        val todayCalories = allIntakes.filter { 
            it.timestamp.toLocalDate() == endDate 
        }.sumOf { it.calories }
        
        val monthlyAverage = if (dailyCaloriesMap.isNotEmpty()) last30DaysCalories / dailyCaloriesMap.size else 0
        val weeklyAverage = if (dailyCaloriesMap.filter { it.key.isAfter(startDate7Days.minusDays(1)) }.isNotEmpty()) {
            last7DaysCalories / dailyCaloriesMap.filter { it.key.isAfter(startDate7Days.minusDays(1)) }.size
        } else 0
        
        // Prepare daily calories data for chart
        val dailyCaloriesList = mutableListOf<DailyCalories>()
        currentDate = startDate30Days
        while (!currentDate.isAfter(endDate)) {
            dailyCaloriesList.add(
                DailyCalories(
                    date = currentDate,
                    calories = dailyCaloriesMap.getOrDefault(currentDate, 0)
                )
            )
            currentDate = currentDate.plusDays(1)
        }
        
        // Get top 5 most frequent foods
        val topFoods = foodFrequencyMap.values
            .sortedByDescending { it.count }
            .take(5)
        
        // Calculate the number of days with data
        val daysWithData = dailyCaloriesMap.keys.size
        val periodDays = if (daysWithData > 0) {
            ChronoUnit.DAYS.between(dailyCaloriesMap.keys.minOrNull(), dailyCaloriesMap.keys.maxOrNull()).toInt() + 1
        } else {
            7 // Default to 7 days if no data
        }
        
        _uiState.update {
            it.copy(
                isLoading = false,
                dailyAverageCalories = todayCalories,
                weeklyAverageCalories = weeklyAverage,
                monthlyAverageCalories = monthlyAverage,
                mostFrequentFoods = topFoods,
                dailyCalorieData = dailyCaloriesList,
                feedingTimeDistribution = feedingHoursMap.toSortedMap(),
                totalFoodIntakes = allIntakes.size,
                averageDailyIntakes = if (dailyCaloriesMap.isNotEmpty()) 
                    allIntakes.size.toDouble() / dailyCaloriesMap.size else 0.0,
                periodDays = periodDays
            )
        }
    }
    
    fun refreshStatistics() {
        loadStatistics()
    }
}

class StatisticsViewModelFactory(
    private val context: Context,
    private val dogId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(context, dogId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}