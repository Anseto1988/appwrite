package com.example.snacktrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.NutritionRepository
import com.example.snacktrack.data.service.AppwriteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class NutritionUiState(
    val isLoading: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val nutritionAnalysis: NutritionAnalysis? = null,
    val treatBudget: TreatBudget? = null,
    val barfCalculation: BARFCalculation? = null,
    val weeklyAnalysis: List<NutritionAnalysis> = emptyList(),
    val error: String? = null
)

class NutritionViewModel(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : ViewModel() {
    private val nutritionRepository = NutritionRepository(context, appwriteService)
    
    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()
    
    fun loadNutritionData(dogId: String, date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedDate = date) }
            
            try {
                // Analyze nutrition for the selected date
                nutritionRepository.analyzeDailyNutrition(dogId, date)
                
                // Load nutrition analysis
                val analysisResult = nutritionRepository.getNutritionAnalysis(dogId, date)
                val analysis = analysisResult.getOrNull()
                
                // Load treat budget
                val budgetResult = nutritionRepository.getTreatBudget(dogId, date)
                val budget = budgetResult.getOrNull()
                
                // Calculate BARF portions
                val barfResult = nutritionRepository.calculateBARFPortions(dogId)
                val barf = barfResult.getOrNull()
                
                // Load weekly data for trends
                val weekStart = date.minusDays(6)
                val weeklyResult = nutritionRepository.getNutritionAnalysisRange(
                    dogId, weekStart, date
                )
                val weeklyData = weeklyResult.getOrNull() ?: emptyList()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nutritionAnalysis = analysis,
                        treatBudget = budget,
                        barfCalculation = barf,
                        weeklyAnalysis = weeklyData,
                        error = null
                    )
                }
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
    
    fun addTreatEntry(dogId: String, treatEntry: TreatEntry) {
        viewModelScope.launch {
            try {
                val result = nutritionRepository.addTreatToBudget(
                    dogId = dogId,
                    treat = treatEntry,
                    date = _uiState.value.selectedDate
                )
                
                result.getOrNull()?.let { updatedBudget ->
                    _uiState.update {
                        it.copy(treatBudget = updatedBudget)
                    }
                    
                    // Refresh nutrition analysis to update treat calories
                    loadNutritionData(dogId, _uiState.value.selectedDate)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun getWeeklyTrend(): NutritionTrend {
        val weeklyData = _uiState.value.weeklyAnalysis
        
        if (weeklyData.isEmpty()) {
            return NutritionTrend(
                averageScore = 0,
                trend = TrendDirection.STABLE,
                message = "Nicht genügend Daten für Trend"
            )
        }
        
        val scores = weeklyData.map { it.getNutritionScore() }
        val averageScore = scores.average().toInt()
        
        // Calculate trend
        val firstHalf = scores.take(scores.size / 2).average()
        val secondHalf = scores.takeLast(scores.size / 2).average()
        
        val trend = when {
            secondHalf > firstHalf + 5 -> TrendDirection.IMPROVING
            secondHalf < firstHalf - 5 -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
        
        val message = when (trend) {
            TrendDirection.IMPROVING -> "Die Ernährung verbessert sich!"
            TrendDirection.DECLINING -> "Die Ernährungsqualität nimmt ab."
            TrendDirection.STABLE -> "Die Ernährung ist konstant."
        }
        
        return NutritionTrend(averageScore, trend, message)
    }
    
    fun getCalorieDistribution(): CalorieDistribution? {
        val analysis = _uiState.value.nutritionAnalysis ?: return null
        
        val proteinCalories = analysis.totalProtein * 4
        val fatCalories = analysis.totalFat * 9
        val carbCalories = analysis.totalCarbs * 4
        
        val totalMacroCalories = proteinCalories + fatCalories + carbCalories
        
        if (totalMacroCalories == 0.0) return null
        
        return CalorieDistribution(
            proteinPercentage = (proteinCalories / totalMacroCalories * 100).toInt(),
            fatPercentage = (fatCalories / totalMacroCalories * 100).toInt(),
            carbPercentage = (carbCalories / totalMacroCalories * 100).toInt()
        )
    }
}

data class NutritionTrend(
    val averageScore: Int,
    val trend: TrendDirection,
    val message: String
)

enum class TrendDirection {
    IMPROVING, STABLE, DECLINING
}

data class CalorieDistribution(
    val proteinPercentage: Int,
    val fatPercentage: Int,
    val carbPercentage: Int
)

class NutritionViewModelFactory(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutritionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NutritionViewModel(context, appwriteService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}