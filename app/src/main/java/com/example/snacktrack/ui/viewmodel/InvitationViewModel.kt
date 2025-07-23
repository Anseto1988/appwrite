package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.TeamInvitation
import com.example.snacktrack.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel für die Verwaltung von Team-Einladungen
 * Verwendet manuelles Factory-Pattern anstatt Hilt, um kapt-Probleme zu vermeiden
 */
class InvitationViewModel(
    private val teamRepository: TeamRepository
) : ViewModel() {

    // UI-Status
    private val _uiState = MutableStateFlow<InvitationUiState>(InvitationUiState.Loading)
    val uiState: StateFlow<InvitationUiState> = _uiState.asStateFlow()

    // Einladungen
    private val _invitations = MutableStateFlow<List<TeamInvitation>>(emptyList())
    val invitations: StateFlow<List<TeamInvitation>> = _invitations.asStateFlow()

    // Lade Einladungen beim Start des ViewModels
    init {
        loadInvitations()
    }

    // Lade ausstehende Einladungen
    fun loadInvitations() {
        _uiState.value = InvitationUiState.Loading
        viewModelScope.launch {
            val result = teamRepository.loadPendingInvitations()
            result.fold(
                onSuccess = { invitationList ->
                    _invitations.value = invitationList
                    _uiState.value = if (invitationList.isEmpty()) {
                        InvitationUiState.Empty
                    } else {
                        InvitationUiState.Success(invitationList)
                    }
                },
                onFailure = { error ->
                    _uiState.value = InvitationUiState.Error(error.message ?: "Fehler beim Laden der Einladungen")
                }
            )
        }
    }

    // Einladung akzeptieren
    fun acceptInvitation(invitation: TeamInvitation) {
        _uiState.value = InvitationUiState.Loading
        viewModelScope.launch {
            val result = teamRepository.acceptInvitation(invitation)
            result.fold(
                onSuccess = {
                    loadInvitations() // Liste aktualisieren
                },
                onFailure = { error ->
                    _uiState.value = InvitationUiState.Error(error.message ?: "Fehler beim Akzeptieren der Einladung")
                }
            )
        }
    }

    // Einladung ablehnen
    fun declineInvitation(invitation: TeamInvitation) {
        _uiState.value = InvitationUiState.Loading
        viewModelScope.launch {
            val result = teamRepository.declineInvitation(invitation)
            result.fold(
                onSuccess = {
                    loadInvitations() // Liste aktualisieren
                },
                onFailure = { error ->
                    _uiState.value = InvitationUiState.Error(error.message ?: "Fehler beim Ablehnen der Einladung")
                }
            )
        }
    }
    
    /**
     * Factory-Klasse für die Erstellung von InvitationViewModel ohne Hilt
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InvitationViewModel::class.java)) {
                val teamRepository = TeamRepository(context)
                return InvitationViewModel(teamRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// UI-Status für die Einladungsanzeige
sealed class InvitationUiState {
    object Loading : InvitationUiState()
    object Empty : InvitationUiState()
    data class Success(val invitations: List<TeamInvitation>) : InvitationUiState()
    data class Error(val message: String) : InvitationUiState()
}