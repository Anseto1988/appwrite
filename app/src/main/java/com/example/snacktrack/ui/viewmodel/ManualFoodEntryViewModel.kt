package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.Food
import com.example.snacktrack.data.repository.FoodRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// Use direct repository methods instead of imported function

class ManualFoodEntryViewModel(
    private val context: Context
) : ViewModel() {

    private val foodRepository = FoodRepository(context)
    
    private val _foodSuggestions = MutableStateFlow<List<Food>>(emptyList())
    val foodSuggestions: StateFlow<List<Food>> = _foodSuggestions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private var searchJob: Job? = null
    
    @OptIn(FlowPreview::class)
    fun searchFoodByName(query: String) {
        // Cancel previous job if it's still running
        searchJob?.cancel()
        
        if (query.length < 2) {
            _foodSuggestions.value = emptyList()
            return
        }
        
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            // Add a small delay to prevent API calls on every keystroke
            delay(300)
            
            // Use enhanced food search with product name search
            foodRepository.searchFood(query)
                .onSuccess { foods ->
                    _foodSuggestions.value = foods
                    _isLoading.value = false
                    if (foods.isNotEmpty()) {
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "Keine passenden Futterarten gefunden"
                    }
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler bei der Suche: ${e.message}"
                    _foodSuggestions.value = emptyList()
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Sucht speziell nach Marken
     */
    @OptIn(FlowPreview::class)
    fun searchFoodByBrand(brand: String) {
        // Cancel previous job if it's still running
        searchJob?.cancel()
        
        if (brand.length < 2) {
            _foodSuggestions.value = emptyList()
            return
        }
        
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            // Add a small delay to prevent API calls on every keystroke
            delay(300)
            
            // Use enhanced brand search
            foodRepository.searchFoodByBrand(brand)
                .onSuccess { foods ->
                    _foodSuggestions.value = foods
                    _isLoading.value = false
                    
                    if (foods.isNotEmpty()) {
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "Keine passenden Marken gefunden"
                    }
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler bei der Markensuche: ${e.message}"
                    _foodSuggestions.value = emptyList()
                    _isLoading.value = false
                }
        }
    }
    
    fun clearSuggestions() {
        _foodSuggestions.value = emptyList()
    }
    
    // Calculate calories from a food's nutrients using the food model's calculation
    fun calculateCaloriesFromGrams(food: Food, grams: Double): Int {
        val caloriesPer100g = food.kcalPer100g
        return (caloriesPer100g * grams / 100.0).toInt()
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ManualFoodEntryViewModel::class.java)) {
                return ManualFoodEntryViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
