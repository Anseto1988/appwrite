package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.service.AppwriteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddDogUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class AddDogViewModel(
    private val context: Context,
    private val appwriteService: AppwriteService
) : ViewModel() {
    
    private val dogRepository = DogRepository(context)
    
    private val _uiState = MutableStateFlow(AddDogUiState())
    val uiState: StateFlow<AddDogUiState> = _uiState.asStateFlow()
    
    fun saveDog(dog: Dog) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            
            try {
                val result = dogRepository.saveDog(dog)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = result.exceptionOrNull()?.message ?: "Unbekannter Fehler"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Unbekannter Fehler"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}