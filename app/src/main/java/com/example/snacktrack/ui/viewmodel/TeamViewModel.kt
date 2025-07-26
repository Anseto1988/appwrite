package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.Team as ModelTeam
import com.example.snacktrack.data.repository.Team as RepositoryTeam
import com.example.snacktrack.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamViewModel(
    private val context: Context
) : ViewModel() {
    
    private val teamRepository = TeamRepository(context)
    
    private val _teams = MutableStateFlow<List<ModelTeam>>(emptyList())
    val teams: StateFlow<List<ModelTeam>> = _teams.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadTeams()
    }
    
    fun loadTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            teamRepository.getTeams().collect { teamList ->
                _teams.value = teamList.map { repoTeam ->
                    ModelTeam(
                        id = repoTeam.id,
                        name = repoTeam.name,
                        ownerId = repoTeam.ownerId,
                        members = repoTeam.members.map { member ->
                            com.example.snacktrack.data.model.TeamMember(
                                userId = member.userId,
                                email = member.email,
                                name = member.name,
                                role = com.example.snacktrack.data.model.BasicTeamRole.valueOf(member.role.name)
                            )
                        },
                        sharedDogs = repoTeam.sharedDogs,
                        description = repoTeam.description,
                        createdAt = repoTeam.createdAt
                    )
                }
                _isLoading.value = false
            }
        }
    }
    
    fun createTeam(name: String, description: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            teamRepository.createTeam(name, description)
                .onSuccess {
                    loadTeams() // Reload teams after creation
                }
                .onFailure { e ->
                    _error.value = "Fehler beim Erstellen des Teams: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            teamRepository.deleteTeam(teamId)
                .onSuccess {
                    loadTeams() // Reload teams after deletion
                }
                .onFailure { e ->
                    _error.value = "Fehler beim Löschen des Teams: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun addMemberToTeam(teamId: String, userEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            teamRepository.addTeamMember(teamId, userEmail)
                .onSuccess {
                    loadTeams() // Reload teams after adding member
                }
                .onFailure { e ->
                    _error.value = "Fehler beim Hinzufügen des Mitglieds: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun removeTeamMember(teamId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            teamRepository.removeTeamMember(teamId, userId)
                .onSuccess {
                    loadTeams() // Reload teams after removing member
                }
                .onFailure { e ->
                    _error.value = "Fehler beim Entfernen des Mitglieds: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun updateTeamMemberRole(teamId: String, userId: String, role: com.example.snacktrack.data.model.TeamRole) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Convert model role to repository role
            val repoRole = com.example.snacktrack.data.repository.TeamRole.valueOf(role.name)
            teamRepository.addTeamMember(teamId, userId, repoRole)
                .onSuccess {
                    loadTeams() // Reload teams after updating role
                }
                .onFailure { e ->
                    _error.value = "Fehler beim Aktualisieren der Rolle: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun searchUsers(query: String) {
        // This should be in InvitationViewModel instead
    }
    
    fun shareDogWithTeam(dogId: String, teamId: String) {
        // This should be in InvitationViewModel instead
    }
}