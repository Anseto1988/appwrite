package com.example.snacktrack.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalDate
import com.example.snacktrack.data.model.Team
import com.example.snacktrack.data.model.TeamMember
import com.example.snacktrack.data.model.BasicTeamRole
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.FeedingTask
import com.example.snacktrack.data.model.TeamActivity
import com.example.snacktrack.data.model.TeamStatistics
import com.example.snacktrack.data.model.ShoppingItem
import com.example.snacktrack.data.model.ConsumptionPrediction
import com.example.snacktrack.data.model.TaskType
import com.example.snacktrack.data.model.TaskStatus
import com.example.snacktrack.data.model.ActivityType
import com.example.snacktrack.data.repository.TeamRepository
import com.example.snacktrack.data.repository.DogRepository

data class TeamUiState(
    val isLoading: Boolean = false,
    val statistics: TeamStatistics? = null,
    val upcomingTasks: List<FeedingTask> = emptyList(),
    val recentActivities: List<TeamActivity> = emptyList(),
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val consumptionPredictions: List<ConsumptionPrediction> = emptyList(),
    val error: String? = null
)

// TeamStatistics is imported from TeamFeatures.kt

// TeamTask is replaced by FeedingTask from TeamFeatures.kt

// TeamActivity is imported from TeamFeatures.kt

// ShoppingItem and ConsumptionPrediction are imported from TeamFeatures.kt

class TeamViewModel(context: Context) : ViewModel() {
    
    private val teamRepository = TeamRepository(context)
    private val dogRepository = DogRepository(context)
    
    // UI State
    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()
    
    // Teams des Benutzers
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()
    
    // Eigene Hunde des Benutzers, die geteilt werden können
    private val _ownDogs = MutableStateFlow<List<Dog>>(emptyList())
    val ownDogs: StateFlow<List<Dog>> = _ownDogs.asStateFlow()
    
    // Für Benutzersuche
    private val _searchResults = MutableStateFlow<List<com.example.snacktrack.data.model.User>>(emptyList())
    val searchResults: StateFlow<List<com.example.snacktrack.data.model.User>> = _searchResults.asStateFlow()
    
    // Status und Fehlermeldungen
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    init {
        loadTeams()
        loadOwnDogs()
    }
    
    fun loadTeamData(teamId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Mock data for now
                val statistics = TeamStatistics(
                    teamId = teamId,
                    taskCompletionRate = 0.75f,
                    averageResponseTime = 30,
                    totalActivities = 12
                )
                
                val tasks = listOf(
                    FeedingTask(
                        id = "1",
                        teamId = teamId,
                        dogId = "dog1",
                        taskType = TaskType.OTHER,
                        assignedToUserId = "Max",
                        scheduledDate = java.time.LocalDate.now().plusDays(1),
                        notes = "Futter kaufen"
                    ),
                    FeedingTask(
                        id = "2",
                        teamId = teamId,
                        dogId = "dog1",
                        taskType = TaskType.VET_APPOINTMENT,
                        assignedToUserId = "Lisa",
                        scheduledDate = java.time.LocalDate.now().plusDays(3)
                    )
                )
                
                val activities = listOf(
                    TeamActivity(
                        id = "1",
                        teamId = teamId,
                        userId = "max123",
                        dogId = "buddy",
                        activityType = ActivityType.FEEDING,
                        timestamp = LocalDateTime.now().minusHours(2),
                        description = "Hat Buddy gefüttert"
                    ),
                    TeamActivity(
                        id = "2",
                        teamId = teamId,
                        userId = "lisa456",
                        dogId = "buddy",
                        activityType = ActivityType.MEDICATION_GIVEN,
                        timestamp = LocalDateTime.now().minusHours(5),
                        description = "Hat Medikament gegeben"
                    )
                )
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statistics = statistics,
                        upcomingTasks = tasks,
                        recentActivities = activities
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }
    
    fun completeTask(taskId: String, notes: String? = null) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    upcomingTasks = state.upcomingTasks.map { task ->
                        if (task.id == taskId) {
                            task.copy(
                                status = TaskStatus.COMPLETED,
                                completedAt = LocalDateTime.now(),
                                notes = notes ?: task.notes
                            )
                        } else task
                    }
                )
            }
        }
    }
    
    fun markItemAsPurchased(itemId: String) {
        _uiState.update { state ->
            state.copy(
                shoppingItems = state.shoppingItems.map { item ->
                    if (item.id == itemId) item.copy(isPurchased = true) else item
                }
            )
        }
    }
    
    fun addPredictionToShoppingList(prediction: ConsumptionPrediction) {
        val newItem = ShoppingItem(
            id = System.currentTimeMillis().toString(),
            productName = prediction.foodName,
            brand = prediction.brand,
            quantity = prediction.recommendedOrderQuantity,
            isPurchased = false,
            linkedFoodId = prediction.foodId
        )
        
        _uiState.update { state ->
            state.copy(shoppingItems = state.shoppingItems + newItem)
        }
    }
    
    fun loadMoreActivities() {
        // Mock implementation
        viewModelScope.launch {
            val moreActivities = listOf(
                TeamActivity(
                    id = System.currentTimeMillis().toString(),
                    teamId = "", // Would need actual teamId
                    userId = "tom789",
                    activityType = ActivityType.WALK_COMPLETED,
                    timestamp = LocalDateTime.now().minusDays(1),
                    description = "Hat Spielzeit gemacht"
                )
            )
            
            _uiState.update { state ->
                state.copy(recentActivities = state.recentActivities + moreActivities)
            }
        }
    }
    
    /**
     * Lädt alle Teams des Benutzers
     */
    fun loadTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Die korrekte Methode im TeamRepository heißt "loadTeams"
                val result = teamRepository.loadTeams()
                result.fold(
                    onSuccess = { teamsList: List<com.example.snacktrack.data.model.Team> ->
                        _teams.value = teamsList
                        _isLoading.value = false
                    },
                    onFailure = { error: Throwable ->
                        _errorMessage.value = "Fehler beim Laden der Teams: ${error.message}"
                        _isLoading.value = false
                    }
                )
                
                // Zusätzlich auf den StateFlow abonnieren für zukünftige Updates
                viewModelScope.launch {
                    teamRepository.teamsStateFlow.collect { teamsList: List<com.example.snacktrack.data.model.Team> ->
                        _teams.value = teamsList
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fehler beim Laden der Teams: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Lädt alle eigenen Hunde des Benutzers
     */
    fun loadOwnDogs() {
        viewModelScope.launch {
            _isLoading.value = true
            dogRepository.getOwnDogs().collect { dogsList ->
                _ownDogs.value = dogsList
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Erstellt ein neues Team
     */
    fun createTeam(name: String) {
        if (name.isBlank()) {
            _errorMessage.value = "Der Teamname darf nicht leer sein."
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            teamRepository.createTeam(name)
                .onSuccess { team ->
                    _successMessage.value = "Team '${team.name}' erfolgreich erstellt."
                    loadTeams()
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler beim Erstellen des Teams: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Sucht nach Benutzern anhand der E-Mail-Adresse
     */
    fun searchUsers(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _errorMessage.value = "Bitte geben Sie eine gültige E-Mail-Adresse ein."
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            teamRepository.searchUsers(email)
                .onSuccess { users ->
                    _searchResults.value = users
                    if (users.isEmpty()) {
                        _errorMessage.value = "Keine Benutzer mit dieser E-Mail gefunden."
                    }
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler bei der Benutzersuche: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Fügt einen Benutzer zu einem Team hinzu
     */
    fun addTeamMember(teamId: String, email: String, role: BasicTeamRole) {
        viewModelScope.launch {
            _isLoading.value = true
            teamRepository.addTeamMember(teamId, email, role)
                .onSuccess {
                    _successMessage.value = "Einladung an $email gesendet."
                    loadTeams()
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler beim Hinzufügen des Mitglieds: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Aktualisiert die Rolle eines Teammitglieds
     */
    fun updateTeamMemberRole(teamId: String, membershipId: String, role: BasicTeamRole) {
        viewModelScope.launch {
            _isLoading.value = true
            teamRepository.updateTeamMemberRole(teamId, membershipId, role)
                .onSuccess {
                    _successMessage.value = "Rolle erfolgreich aktualisiert."
                    loadTeams()
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler beim Aktualisieren der Rolle: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Entfernt ein Mitglied aus einem Team
     */
    fun removeTeamMember(teamId: String, membershipId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            teamRepository.removeTeamMember(teamId, membershipId)
                .onSuccess {
                    _successMessage.value = "Mitglied erfolgreich entfernt."
                    loadTeams()
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler beim Entfernen des Mitglieds: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Teilt einen Hund mit einem Team
     */
    fun shareDogWithTeam(dogId: String, teamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Die korrekte Methode im TeamRepository aufrufen, die einen Eintrag in der dog_sharing-Collection erstellt
                teamRepository.shareDogWithTeam(dogId, teamId)
                    .onSuccess {
                        _successMessage.value = "Hund erfolgreich mit dem Team geteilt."
                        loadOwnDogs() // Lade die Hunde neu, um Updates zu zeigen
                    }
                    .onFailure { e ->
                        _errorMessage.value = "Fehler beim Teilen des Hundes: ${e.message}"
                        Log.e("TeamViewModel", "Fehler beim Teilen des Hundes", e)
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Unerwarteter Fehler beim Teilen des Hundes: ${e.message}"
                Log.e("TeamViewModel", "Unerwarteter Fehler beim Teilen des Hundes", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Hebt die Freigabe eines Hundes für ein Team auf
     */
    fun unshareDogsFromTeam(dogId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Hund zuerst laden
            val dog = _ownDogs.value.find { it.id == dogId }
            if (dog != null) {
                // teamId auf null setzen
                val updatedDog = dog.copy(teamId = null)
                dogRepository.saveDog(updatedDog)
                    .onSuccess {
                        _successMessage.value = "Freigabe des Hundes aufgehoben."
                        loadOwnDogs()
                    }
                    .onFailure { e ->
                        _errorMessage.value = "Fehler beim Aufheben der Freigabe: ${e.message}"
                        _isLoading.value = false
                    }
            } else {
                _errorMessage.value = "Hund nicht gefunden."
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Löscht ein Team
     */
    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            teamRepository.deleteTeam(teamId)
                .onSuccess {
                    _successMessage.value = "Team erfolgreich gelöscht."
                    loadTeams()
                    
                    // Auch die Teamzugehörigkeit aller Hunde entfernen
                    val dogsToUpdate = _ownDogs.value.filter { it.teamId == teamId }
                    dogsToUpdate.forEach { dog ->
                        val updatedDog = dog.copy(teamId = null)
                        dogRepository.saveDog(updatedDog)
                    }
                    if (dogsToUpdate.isNotEmpty()) {
                        loadOwnDogs()
                    }
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler beim Löschen des Teams: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Löscht Erfolgs- und Fehlermeldungen
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
