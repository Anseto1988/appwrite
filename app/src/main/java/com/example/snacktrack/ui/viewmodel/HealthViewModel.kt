package com.example.snacktrack.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.HealthRepository
import com.example.snacktrack.data.repository.MedicationReminder
import com.example.snacktrack.data.service.AppwriteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class HealthUiState(
    val isLoading: Boolean = false,
    val allergies: List<DogAllergy> = emptyList(),
    val medications: List<DogMedication> = emptyList(),
    val recentEntries: List<DogHealthEntry> = emptyList(),
    val todayReminders: List<MedicationReminder> = emptyList(),
    val insights: List<HealthInsight> = emptyList(),
    val error: String? = null
)

data class HealthInsight(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val priority: InsightPriority = InsightPriority.INFO
)

enum class InsightPriority {
    INFO, WARNING, CRITICAL
}

class HealthViewModel(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : ViewModel() {
    private val healthRepository = HealthRepository(context, appwriteService)
    
    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()
    
    fun loadHealthData(dogId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load allergies
                val allergiesResult = healthRepository.getAllergiesForDog(dogId)
                val allergies = allergiesResult.getOrNull() ?: emptyList()
                
                // Load medications
                val medicationsResult = healthRepository.getActiveMedications(dogId)
                val medications = medicationsResult.getOrNull() ?: emptyList()
                
                // Load today's reminders
                val remindersResult = healthRepository.getMedicationReminders(dogId, LocalDate.now())
                val reminders = remindersResult.getOrNull() ?: emptyList()
                
                // Load recent health entries
                val entriesResult = healthRepository.getHealthEntries(
                    dogId,
                    LocalDateTime.now().minusDays(30)
                )
                val entries = entriesResult.getOrNull() ?: emptyList()
                
                // Generate insights
                val insights = generateHealthInsights(allergies, medications, entries)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allergies = allergies,
                        medications = medications,
                        todayReminders = reminders,
                        recentEntries = entries.sortedByDescending { it.entryDate },
                        insights = insights,
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
    
    fun addAllergy(allergy: DogAllergy) {
        viewModelScope.launch {
            try {
                healthRepository.addAllergy(allergy)
                loadHealthData(allergy.dogId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun addMedication(medication: DogMedication) {
        viewModelScope.launch {
            try {
                healthRepository.addMedication(medication)
                loadHealthData(medication.dogId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun addHealthEntry(entry: DogHealthEntry) {
        viewModelScope.launch {
            try {
                healthRepository.addHealthEntry(entry)
                loadHealthData(entry.dogId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun checkFoodForAllergens(dogId: String, ingredients: List<String>, onResult: (List<DogAllergy>) -> Unit) {
        viewModelScope.launch {
            try {
                val result = healthRepository.checkFoodForAllergens(dogId, ingredients)
                result.getOrNull()?.let { allergens ->
                    onResult(allergens)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    private fun generateHealthInsights(
        allergies: List<DogAllergy>,
        medications: List<DogMedication>,
        entries: List<DogHealthEntry>
    ): List<HealthInsight> {
        val insights = mutableListOf<HealthInsight>()
        
        // Critical allergies warning
        val criticalAllergies = allergies.filter { it.severity == DogAllergySeverity.CRITICAL }
        if (criticalAllergies.isNotEmpty()) {
            insights.add(
                HealthInsight(
                    title = "Kritische Allergien",
                    description = "Ihr Hund hat ${criticalAllergies.size} kritische Allergien. Bitte beim Füttern besonders vorsichtig sein!",
                    icon = Icons.Default.Error,
                    priority = InsightPriority.CRITICAL
                )
            )
        }
        
        // Medication reminders
        val medicationsWithFoodReq = medications.filter { it.foodInteraction != FoodInteraction.NONE }
        if (medicationsWithFoodReq.isNotEmpty()) {
            insights.add(
                HealthInsight(
                    title = "Medikamente mit Futter-Anforderungen",
                    description = "${medicationsWithFoodReq.size} Medikamente müssen mit speziellen Fütterungsanforderungen verabreicht werden.",
                    icon = Icons.Default.Restaurant,
                    priority = InsightPriority.WARNING
                )
            )
        }
        
        // Recent symptoms trend
        val recentSymptomEntries = entries
            .filter { it.symptoms.isNotEmpty() }
            .take(10)
        
        if (recentSymptomEntries.size >= 3) {
            val mostCommonSymptom = recentSymptomEntries
                .flatMap { it.symptoms }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
            
            mostCommonSymptom?.let {
                insights.add(
                    HealthInsight(
                        title = "Häufiges Symptom",
                        description = "${it.key.displayName} wurde in den letzten Einträgen ${it.value} mal beobachtet.",
                        icon = Icons.Default.TrendingUp,
                        priority = InsightPriority.WARNING
                    )
                )
            }
        }
        
        // Positive trend
        val lastWeekEntries = entries.filter { 
            it.entryDate.isAfter(LocalDateTime.now().minusDays(7)) 
        }
        if (lastWeekEntries.isEmpty() || lastWeekEntries.none { it.symptoms.isNotEmpty() }) {
            insights.add(
                HealthInsight(
                    title = "Gute Gesundheit",
                    description = "Keine gesundheitlichen Probleme in der letzten Woche!",
                    icon = Icons.Default.Favorite,
                    priority = InsightPriority.INFO
                )
            )
        }
        
        return insights.sortedByDescending { it.priority }
    }
}

class HealthViewModelFactory(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealthViewModel(context, appwriteService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}