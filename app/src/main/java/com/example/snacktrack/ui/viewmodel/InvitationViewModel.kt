package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.Team
import com.example.snacktrack.data.model.TeamInvitation
import com.example.snacktrack.data.model.TeamRole
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class InvitationUiState {
    object Loading : InvitationUiState()
    object Empty : InvitationUiState()
    object Success : InvitationUiState()
    data class Error(val message: String) : InvitationUiState()
}

class InvitationViewModel(
    private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<InvitationUiState>(InvitationUiState.Empty)
    val uiState: StateFlow<InvitationUiState> = _uiState.asStateFlow()
    
    private val _invitations = MutableStateFlow<List<TeamInvitation>>(emptyList())
    val invitations: StateFlow<List<TeamInvitation>> = _invitations.asStateFlow()
    
    private val _ownDogs = MutableStateFlow<List<Dog>>(emptyList())
    val ownDogs: StateFlow<List<Dog>> = _ownDogs.asStateFlow()
    
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    init {
        loadInvitations()
    }
    
    fun loadInvitations() {
        viewModelScope.launch {
            _uiState.value = InvitationUiState.Loading
            // TODO: Load invitations from repository
            _invitations.value = emptyList()
            _uiState.value = if (_invitations.value.isEmpty()) {
                InvitationUiState.Empty
            } else {
                InvitationUiState.Success
            }
        }
    }
    
    fun acceptInvitation(invitationId: String) {
        viewModelScope.launch {
            _uiState.value = InvitationUiState.Loading
            // TODO: Accept invitation via repository
            _successMessage.value = "Einladung akzeptiert"
            loadInvitations()
        }
    }
    
    fun declineInvitation(invitationId: String) {
        viewModelScope.launch {
            _uiState.value = InvitationUiState.Loading
            // TODO: Decline invitation via repository
            _successMessage.value = "Einladung abgelehnt"
            loadInvitations()
        }
    }
    
    fun createTeam(name: String, description: String?) {
        viewModelScope.launch {
            // TODO: Create team via repository
            _successMessage.value = "Team erstellt"
        }
    }
    
    fun addTeamMember(teamId: String, userEmail: String) {
        viewModelScope.launch {
            // TODO: Add team member via repository
            _successMessage.value = "Mitglied hinzugefügt"
        }
    }
    
    fun removeTeamMember(teamId: String, userId: String) {
        viewModelScope.launch {
            // TODO: Remove team member via repository
            _successMessage.value = "Mitglied entfernt"
        }
    }
    
    fun updateTeamMemberRole(teamId: String, userId: String, role: TeamRole) {
        viewModelScope.launch {
            // TODO: Update member role via repository
            _successMessage.value = "Rolle aktualisiert"
        }
    }
    
    fun searchUsers(query: String) {
        viewModelScope.launch {
            if (query.length >= 3) {
                // TODO: Search users via repository
                _searchResults.value = emptyList()
            } else {
                _searchResults.value = emptyList()
            }
        }
    }
    
    fun shareDogWithTeam(dogId: String, teamId: String) {
        viewModelScope.launch {
            // TODO: Share dog with team via repository
            _successMessage.value = "Hund geteilt"
        }
    }
    
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InvitationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return InvitationViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}