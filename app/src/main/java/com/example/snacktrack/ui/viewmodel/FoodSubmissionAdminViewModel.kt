package com.example.snacktrack.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.FoodSubmission
import com.example.snacktrack.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FoodSubmissionAdminViewModel(context: Context) : ViewModel() {
    
    private val foodRepository = FoodRepository(context)
    
    private val _submissions = MutableStateFlow<List<FoodSubmission>>(emptyList())
    val submissions: StateFlow<List<FoodSubmission>> = _submissions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadSubmissions()
    }
    
    fun loadSubmissions() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                foodRepository.getAllFoodSubmissions().collectLatest { submissionsList ->
                    _submissions.value = submissionsList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("FoodSubmissionAdminVM", "Error loading submissions: ${e.message}", e)
                _errorMessage.value = "Fehler beim Laden der Beiträge: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun approveSubmission(submission: FoodSubmission) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            foodRepository.reviewFoodSubmission(submission, true)
                .onSuccess { 
                    loadSubmissions() // Neuladen der Liste
                }
                .onFailure { e ->
                    Log.e("FoodSubmissionAdminVM", "Error approving submission: ${e.message}", e)
                    _errorMessage.value = "Fehler beim Genehmigen des Beitrags: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun rejectSubmission(submission: FoodSubmission) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            foodRepository.reviewFoodSubmission(submission, false)
                .onSuccess { 
                    loadSubmissions() // Neuladen der Liste
                }
                .onFailure { e ->
                    Log.e("FoodSubmissionAdminVM", "Error rejecting submission: ${e.message}", e)
                    _errorMessage.value = "Fehler beim Ablehnen des Beitrags: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun deleteSubmission(submission: FoodSubmission) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            foodRepository.deleteFoodSubmission(submission.id)
                .onSuccess {
                    loadSubmissions() // Neuladen der Liste
                }
                .onFailure { e ->
                    Log.e("FoodSubmissionAdminVM", "Error deleting submission: ${e.message}", e)
                    _errorMessage.value = "Fehler beim Löschen des Beitrags: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
