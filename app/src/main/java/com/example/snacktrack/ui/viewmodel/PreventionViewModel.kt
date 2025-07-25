package com.example.snacktrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.PreventionRepository
import com.example.snacktrack.data.service.AppwriteService
// These imports are already covered by the wildcard import above: com.example.snacktrack.data.model.*
// PreventionActivity, PreventionTask, and WeightEntry are in the data.model package
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

data class PreventionUiState(
    val isLoading: Boolean = false,
    val dogName: String = "",
    val riskAssessment: RiskAssessment? = null,
    val weightGoals: List<WeightGoal> = emptyList(),
    val weightHistory: List<WeightEntry> = emptyList(),
    val allergyPrevention: AllergyPrevention? = null,
    val healthScreenings: List<HealthScreening> = emptyList(),
    val vaccinationSchedule: VaccinationSchedule? = null,
    val dentalCare: DentalCare? = null,
    val seasonalCare: SeasonalCare? = null,
    val analytics: PreventionAnalytics? = null,
    val upcomingTasks: List<PreventionTask> = emptyList(),
    val recentActivities: List<PreventionActivity> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class PreventionViewModel(
    private val appwriteService: AppwriteService,
    private val dogId: String
) : ViewModel() {
    private val preventionRepository = PreventionRepository(appwriteService)
    
    private val _uiState = MutableStateFlow(PreventionUiState())
    val uiState: StateFlow<PreventionUiState> = _uiState.asStateFlow()
    
    init {
        loadPreventionData()
    }
    
    fun loadPreventionData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load all prevention data concurrently
                val riskAssessmentResult = preventionRepository.getRiskAssessments(dogId)
                val weightGoalsResult = preventionRepository.getWeightGoals(dogId)
                val allergyPreventionResult = preventionRepository.getAllergyPrevention(dogId)
                val healthScreeningsResult = preventionRepository.getHealthScreenings(dogId)
                val vaccinationScheduleResult = preventionRepository.getVaccinationSchedule(dogId)
                val dentalCareResult = preventionRepository.getDentalCare(dogId)
                val seasonalCareResult = preventionRepository.getSeasonalCare(dogId, getCurrentSeason())
                val analyticsResult = preventionRepository.getPreventionAnalytics(dogId)
                
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        dogName = "Ihr Hund", // Would get from dog data
                        riskAssessment = riskAssessmentResult.getOrNull()?.firstOrNull(),
                        weightGoals = weightGoalsResult.getOrNull() ?: emptyList(),
                        allergyPrevention = allergyPreventionResult.getOrNull(),
                        healthScreenings = healthScreeningsResult.getOrNull() ?: emptyList(),
                        vaccinationSchedule = vaccinationScheduleResult.getOrNull(),
                        dentalCare = dentalCareResult.getOrNull(),
                        seasonalCare = seasonalCareResult.getOrNull(),
                        analytics = analyticsResult.getOrNull(),
                        upcomingTasks = generateUpcomingTasks(),
                        recentActivities = generateRecentActivities(),
                        weightHistory = generateWeightHistory()
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
    
    // Risk Assessment
    
    fun generateRiskAssessment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = preventionRepository.generateRiskAssessment(dogId)
                result.getOrNull()?.let { assessment ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            riskAssessment = assessment,
                            successMessage = "Risikobewertung erfolgreich erstellt"
                        )
                    }
                } ?: run {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Fehler beim Erstellen der Risikobewertung"
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
    
    // Weight Management
    
    fun createWeightGoal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Create a default weight goal - in real app this would open a dialog
                val weightGoal = WeightGoal(
                    dogId = dogId,
                    targetWeight = 25.0, // Example target
                    currentWeight = 27.0, // Example current
                    startWeight = 27.0,
                    targetDate = LocalDate.now().plusMonths(3),
                    goalType = WeightGoalType.LOSE_WEIGHT,
                    status = GoalStatus.ACTIVE
                )
                
                val result = preventionRepository.createWeightGoal(weightGoal)
                result.getOrNull()?.let { createdGoal ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            weightGoals = currentState.weightGoals + createdGoal,
                            successMessage = "Gewichtsziel erfolgreich erstellt"
                        )
                    }
                } ?: run {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Fehler beim Erstellen des Gewichtsziels"
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
    
    fun updateWeightGoal(goalId: String, newWeight: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = preventionRepository.updateWeightGoalProgress(goalId, newWeight)
                result.getOrNull()?.let { updatedGoal ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            weightGoals = currentState.weightGoals.map { goal ->
                                if (goal.id == goalId) updatedGoal else goal
                            },
                            successMessage = "Gewicht erfolgreich aktualisiert"
                        )
                    }
                    
                    // Add to weight history
                    val newEntry = WeightEntry(
                        date = LocalDate.now(),
                        weight = newWeight
                    )
                    _uiState.update { currentState ->
                        currentState.copy(
                            weightHistory = (currentState.weightHistory + newEntry)
                                .sortedByDescending { it.date }
                                .take(50) // Keep last 50 entries
                        )
                    }
                } ?: run {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Fehler beim Aktualisieren des Gewichts"
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
    
    // Allergy Prevention
    
    fun addKnownAllergen() {
        viewModelScope.launch {
            try {
                // In real app, this would open a dialog to input allergen details
                val allergen = KnownAllergen(
                    allergen = "Chicken", // Example
                    severity = AllergySeverity.MODERATE,
                    reactions = listOf(
                        AllergyReaction(
                            type = ReactionType.SKIN,
                            symptoms = listOf("Itching", "Redness"),
                            onsetTime = "2-4 hours"
                        )
                    )
                )
                
                val result = preventionRepository.addKnownAllergen(dogId, allergen)
                result.getOrNull()?.let {
                    // Refresh allergy prevention data
                    loadAllergyPrevention()
                    _uiState.update { 
                        it.copy(successMessage = "Allergen erfolgreich hinzugefügt")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun addSuspectedAllergen() {
        viewModelScope.launch {
            try {
                // In real app, this would open a dialog
                // For now, just show success message
                _uiState.update { 
                    it.copy(successMessage = "Funktion zum Hinzufügen verdächtiger Allergene öffnet Dialog")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun confirmAllergen(allergenName: String) {
        viewModelScope.launch {
            try {
                // Convert suspected to known allergen
                val knownAllergen = KnownAllergen(
                    allergen = allergenName,
                    severity = AllergySeverity.MODERATE // Default, would be configurable
                )
                
                val result = preventionRepository.addKnownAllergen(dogId, knownAllergen)
                result.getOrNull()?.let {
                    loadAllergyPrevention()
                    _uiState.update { 
                        it.copy(successMessage = "Allergen bestätigt und zu bekannten Allergenen hinzugefügt")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun createEmergencyPlan() {
        viewModelScope.launch {
            try {
                // In real app, this would open a comprehensive emergency plan setup
                _uiState.update { 
                    it.copy(successMessage = "Notfallplan-Setup öffnet detailliertes Formular")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    // Health Screening
    
    fun scheduleRoutineScreening() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val screening = HealthScreening(
                    dogId = dogId,
                    screeningType = ScreeningType.ROUTINE,
                    scheduledDate = LocalDate.now().plusWeeks(2),
                    status = ScreeningStatus.SCHEDULED,
                    tests = listOf(
                        ScreeningTest(
                            name = "Blutbild",
                            category = TestCategory.BLOOD_WORK,
                            purpose = "Allgemeine Gesundheitsüberprüfung"
                        ),
                        ScreeningTest(
                            name = "Urinanalyse",
                            category = TestCategory.URINE_ANALYSIS,
                            purpose = "Nieren- und Blasenfunktion"
                        )
                    )
                )
                
                val result = preventionRepository.createHealthScreening(screening)
                result.getOrNull()?.let { createdScreening ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            healthScreenings = currentState.healthScreenings + createdScreening,
                            successMessage = "Routineuntersuchung erfolgreich geplant"
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
    
    fun scheduleBreedSpecificScreening() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val screening = HealthScreening(
                    dogId = dogId,
                    screeningType = ScreeningType.BREED_SPECIFIC,
                    scheduledDate = LocalDate.now().plusWeeks(3),
                    status = ScreeningStatus.SCHEDULED,
                    tests = listOf(
                        ScreeningTest(
                            name = "Hüftdysplasie-Screening",
                            category = TestCategory.IMAGING,
                            purpose = "Rassenspezifische Gelenkprobleme"
                        ),
                        ScreeningTest(
                            name = "Herzuntersuchung",
                            category = TestCategory.CARDIAC,
                            purpose = "Rassespezifische Herzprobleme"
                        )
                    )
                )
                
                val result = preventionRepository.createHealthScreening(screening)
                result.getOrNull()?.let { createdScreening ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            healthScreenings = currentState.healthScreenings + createdScreening,
                            successMessage = "Rassenspezifische Untersuchung erfolgreich geplant"
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
    
    fun scheduleSeniorScreening() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val screening = HealthScreening(
                    dogId = dogId,
                    screeningType = ScreeningType.SENIOR_PANEL,
                    scheduledDate = LocalDate.now().plusWeeks(1),
                    status = ScreeningStatus.SCHEDULED,
                    tests = listOf(
                        ScreeningTest(
                            name = "Erweiterte Blutuntersuchung",
                            category = TestCategory.BLOOD_WORK,
                            purpose = "Organfunktionen bei Senioren"
                        ),
                        ScreeningTest(
                            name = "Röntgen Thorax",
                            category = TestCategory.IMAGING,
                            purpose = "Herz und Lunge bei Senioren"
                        ),
                        ScreeningTest(
                            name = "Neurologische Untersuchung",
                            category = TestCategory.NEUROLOGICAL,
                            purpose = "Kognitive Funktion"
                        )
                    )
                )
                
                val result = preventionRepository.createHealthScreening(screening)
                result.getOrNull()?.let { createdScreening ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            healthScreenings = currentState.healthScreenings + createdScreening,
                            successMessage = "Senior-Untersuchung erfolgreich geplant"
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
    
    fun completeScreening(screeningId: String) {
        viewModelScope.launch {
            try {
                // In real app, this would open a form to enter results
                val results = ScreeningResults(
                    overallStatus = HealthStatus.NORMAL,
                    testResults = mapOf(
                        "blood_work" to TestResult(
                            value = "Normal",
                            isNormal = true,
                            interpretation = "Alle Werte im Normalbereich"
                        )
                    ),
                    recommendedActions = listOf("Nächste Untersuchung in 6 Monaten")
                )
                
                val result = preventionRepository.updateScreeningResults(screeningId, results)
                result.getOrNull()?.let {
                    _uiState.update { currentState ->
                        currentState.copy(
                            healthScreenings = currentState.healthScreenings.map { screening ->
                                if (screening.id == screeningId) {
                                    screening.copy(
                                        status = ScreeningStatus.COMPLETED,
                                        completedDate = LocalDate.now(),
                                        results = results
                                    )
                                } else screening
                            },
                            successMessage = "Untersuchung erfolgreich abgeschlossen"
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
    
    // Vaccination Management
    
    fun recordVaccination(vaccineId: String, doseNumber: Int) {
        viewModelScope.launch {
            try {
                val result = preventionRepository.recordVaccination(
                    dogId = dogId,
                    vaccineName = vaccineId,
                    doseNumber = doseNumber,
                    administeredDate = LocalDate.now()
                )
                
                result.getOrNull()?.let {
                    // Refresh vaccination schedule
                    loadVaccinationSchedule()
                    _uiState.update { 
                        it.copy(successMessage = "Impfung erfolgreich eingetragen")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    // Dental Care
    
    fun logDentalCare() {
        viewModelScope.launch {
            try {
                val careLog = HomeCareLog(
                    date = LocalDate.now(),
                    careType = HomeCareType.BRUSHING,
                    completed = true,
                    notes = "Tägliche Zahnpflege durchgeführt"
                )
                
                val result = preventionRepository.logHomeCare(dogId, careLog)
                result.getOrNull()?.let {
                    // Refresh dental care data
                    loadDentalCare()
                    _uiState.update { 
                        it.copy(successMessage = "Zahnpflege erfolgreich eingetragen")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun scheduleProfessionalCleaning() {
        viewModelScope.launch {
            try {
                // In real app, this would integrate with calendar/booking system
                _uiState.update { 
                    it.copy(successMessage = "Professionelle Zahnreinigung - Terminbuchung öffnet Kalender")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    // Helper methods for loading specific data
    
    private suspend fun loadAllergyPrevention() {
        try {
            val result = preventionRepository.getAllergyPrevention(dogId)
            result.getOrNull()?.let { allergyPrevention ->
                _uiState.update { currentState ->
                    currentState.copy(allergyPrevention = allergyPrevention)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    private suspend fun loadVaccinationSchedule() {
        try {
            val result = preventionRepository.getVaccinationSchedule(dogId)
            result.getOrNull()?.let { schedule ->
                _uiState.update { currentState ->
                    currentState.copy(vaccinationSchedule = schedule)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    private suspend fun loadDentalCare() {
        try {
            val result = preventionRepository.getDentalCare(dogId)
            result.getOrNull()?.let { dentalCare ->
                _uiState.update { currentState ->
                    currentState.copy(dentalCare = dentalCare)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    // Helper methods for generating demo data
    
    private fun generateUpcomingTasks(): List<PreventionTask> {
        return listOf(
            PreventionTask(
                id = "1",
                title = "Zahnreinigung beim Tierarzt",
                category = PreventionCategory.DENTAL,
                priority = RecommendationPriority.HIGH,
                dueDate = LocalDate.now().plusWeeks(2)
            ),
            PreventionTask(
                id = "2",
                title = "Jährliche Routineuntersuchung",
                category = PreventionCategory.MEDICAL,
                priority = RecommendationPriority.MEDIUM,
                dueDate = LocalDate.now().plusMonths(1)
            ),
            PreventionTask(
                id = "3",
                title = "Gewichtskontrolle",
                category = PreventionCategory.NUTRITION,
                priority = RecommendationPriority.MEDIUM,
                dueDate = LocalDate.now().plusDays(7)
            ),
            PreventionTask(
                id = "4",
                title = "Impfauffrischung",
                category = PreventionCategory.MEDICAL,
                priority = RecommendationPriority.HIGH,
                dueDate = LocalDate.now().plusWeeks(3)
            ),
            PreventionTask(
                id = "5",
                title = "Zeckenschutz erneuern",
                category = PreventionCategory.GENERAL,
                priority = RecommendationPriority.MEDIUM,
                dueDate = LocalDate.now().plusDays(14)
            )
        )
    }
    
    private fun generateRecentActivities(): List<PreventionActivity> {
        return listOf(
            PreventionActivity(
                id = "1",
                type = "dental_care",
                title = "Zähne geputzt",
                date = LocalDateTime.now().minusHours(2),
                notes = "5 Minuten Zahnpflege"
            ),
            PreventionActivity(
                id = "2",
                type = "weight_check",
                title = "Gewicht gemessen",
                date = LocalDateTime.now().minusDays(1),
                notes = "26.8 kg - leichte Verbesserung"
            ),
            PreventionActivity(
                id = "3",
                type = "vaccination",
                title = "Tollwutimpfung",
                date = LocalDateTime.now().minusWeeks(2),
                notes = "Auffrischung erfolgreich"
            ),
            PreventionActivity(
                id = "4",
                type = "screening",
                title = "Blutuntersuchung",
                date = LocalDateTime.now().minusMonths(1),
                notes = "Alle Werte normal"
            ),
            PreventionActivity(
                id = "5",
                type = "dental_care",
                title = "Kaustange gegeben",
                date = LocalDateTime.now().minusDays(2),
                notes = "Dentalcare-Stick"
            )
        )
    }
    
    private fun generateWeightHistory(): List<WeightEntry> {
        val entries = mutableListOf<WeightEntry>()
        var currentWeight = 28.0
        
        for (i in 30 downTo 0) {
            val date = LocalDate.now().minusDays(i.toLong())
            
            // Simulate gradual weight loss with some variation
            if (i % 7 == 0) { // Weekly measurements
                currentWeight += Random.nextDouble(-0.3, 0.1)
                currentWeight = currentWeight.coerceIn(25.0, 30.0)
                
                entries.add(
                    WeightEntry(
                        date = date,
                        weight = currentWeight
                    )
                )
            }
        }
        
        return entries.sortedByDescending { it.date }
    }
    
    private fun getCurrentSeason(): Season {
        val month = LocalDate.now().monthValue
        return when (month) {
            3, 4, 5 -> Season.SPRING
            6, 7, 8 -> Season.SUMMER
            9, 10, 11 -> Season.FALL
            else -> Season.WINTER
        }
    }
    
    // Error handling
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

class PreventionViewModelFactory(
    private val appwriteService: AppwriteService,
    private val dogId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreventionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PreventionViewModel(appwriteService, dogId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}