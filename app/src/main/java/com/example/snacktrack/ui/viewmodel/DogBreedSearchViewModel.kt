package com.example.snacktrack.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.DogBreed
import com.example.snacktrack.data.repository.DogBreedRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel für die Hunderassen-Suche
 */
class DogBreedSearchViewModel(context: Context) : ViewModel() {
    
    private val repository = DogBreedRepository(context)
    
    // State für Suchergebnisse
    private val _suggestions = MutableStateFlow<List<DogBreed>>(emptyList())
    val suggestions: StateFlow<List<DogBreed>> = _suggestions.asStateFlow()
    
    // State für Loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // State für Sichtbarkeit der Vorschläge
    private val _showSuggestions = MutableStateFlow(false)
    val showSuggestions: StateFlow<Boolean> = _showSuggestions.asStateFlow()
    
    // Job für Debouncing
    private var searchJob: Job? = null
    
    /**
     * Sucht nach Hunderassen basierend auf dem eingegebenen Text
     * Implementiert Debouncing um unnötige API-Calls zu vermeiden
     */
    fun searchBreeds(query: String) {
        // Vorherigen Job abbrechen
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _suggestions.value = emptyList()
            _showSuggestions.value = false
            _isLoading.value = false
            return
        }
        
        // Zu kurze Queries nicht suchen
        if (query.length < 2) {
            _suggestions.value = emptyList()
            _showSuggestions.value = false
            _isLoading.value = false
            return
        }
        
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            
            // Debouncing: Warte 300ms bevor die Suche gestartet wird
            delay(300)
            
            try {
                val result = repository.searchBreeds(query, limit = 8)
                
                result.fold(
                    onSuccess = { breeds ->
                        _suggestions.value = breeds
                        _showSuggestions.value = breeds.isNotEmpty()
                        _isLoading.value = false
                    },
                    onFailure = { error ->
                        android.util.Log.e("DogBreedSearchViewModel", "Fehler bei der Suche: ${error.message}", error)
                        _suggestions.value = emptyList()
                        _showSuggestions.value = false
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DogBreedSearchViewModel", "Unerwarteter Fehler bei der Suche: ${e.message}", e)
                _suggestions.value = emptyList()
                _showSuggestions.value = false
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Versteckt die Vorschläge
     */
    fun hideSuggestions() {
        _showSuggestions.value = false
    }
    
    /**
     * Zeigt die Vorschläge wieder an, wenn welche vorhanden sind
     */
    fun showSuggestions() {
        if (_suggestions.value.isNotEmpty()) {
            _showSuggestions.value = true
        }
    }
    
    /**
     * Lädt beliebte Hunderassen für den initialen Zustand
     */
    fun loadPopularBreeds() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val result = repository.getAllBreeds(limit = 20)
                
                result.fold(
                    onSuccess = { breeds ->
                        // Hier könnten wir die beliebtesten Rassen filtern
                        // Für jetzt nehmen wir einfach die ersten 8
                        _suggestions.value = breeds.take(8)
                        _isLoading.value = false
                    },
                    onFailure = { error ->
                        android.util.Log.e("DogBreedSearchViewModel", "Fehler beim Laden der Rassen: ${error.message}", error)
                        _suggestions.value = emptyList()
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DogBreedSearchViewModel", "Unerwarteter Fehler beim Laden: ${e.message}", e)
                _suggestions.value = emptyList()
                _isLoading.value = false
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
