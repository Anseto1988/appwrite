package com.example.snacktrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.AIRepository
import com.example.snacktrack.data.service.AppwriteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AIUiState(
    val isLoading: Boolean = false,
    val foodRecommendation: FoodRecommendation? = null,
    val weightPrediction: WeightPrediction? = null,
    val anomalies: List<EatingAnomaly> = emptyList(),
    val healthRiskAssessment: HealthRiskAssessment? = null,
    val error: String? = null
)

class AIViewModel(
    private val appwriteService: AppwriteService
) : ViewModel() {
    private val aiRepository = AIRepository(appwriteService)
    
    private val _uiState = MutableStateFlow(AIUiState())
    val uiState: StateFlow<AIUiState> = _uiState.asStateFlow()
    
    fun loadAIData(dogId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Generate food recommendations
                val recommendationResult = aiRepository.generateFoodRecommendations(
                    dogId, 
                    RecommendationType.DAILY
                )
                val recommendation = recommendationResult.getOrNull()
                
                // Predict weight
                val predictionResult = aiRepository.predictWeight(dogId)
                val prediction = predictionResult.getOrNull()
                
                // Detect anomalies
                val anomaliesResult = aiRepository.detectEatingAnomalies(dogId)
                val anomalies = anomaliesResult.getOrNull() ?: emptyList()
                
                // Assess health risk
                val assessmentResult = aiRepository.assessHealthRisk(dogId)
                val assessment = assessmentResult.getOrNull()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        foodRecommendation = recommendation,
                        weightPrediction = prediction,
                        anomalies = anomalies,
                        healthRiskAssessment = assessment,
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
    
    fun generateRecommendations(dogId: String, type: RecommendationType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = aiRepository.generateFoodRecommendations(dogId, type)
                result.getOrNull()?.let { recommendation ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            foodRecommendation = recommendation
                        )
                    }
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
    
    fun predictWeight(dogId: String, days: Int = 90) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = aiRepository.predictWeight(dogId, days)
                result.getOrNull()?.let { prediction ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            weightPrediction = prediction
                        )
                    }
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
    
    fun refreshAIAnalysis(dogId: String) {
        loadAIData(dogId)
    }
    
    fun provideFeedback(modelType: AIModelType, feedback: UserFeedback) {
        viewModelScope.launch {
            // Store feedback for model improvement
            // This would be implemented to save training data
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

class AIViewModelFactory(
    private val appwriteService: AppwriteService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AIViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AIViewModel(appwriteService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}