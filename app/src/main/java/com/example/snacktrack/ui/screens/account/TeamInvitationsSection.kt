package com.example.snacktrack.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.snacktrack.R
import com.example.snacktrack.data.model.TeamInvitation
import com.example.snacktrack.ui.viewmodel.InvitationUiState
import com.example.snacktrack.ui.viewmodel.InvitationViewModel

@Composable
fun TeamInvitationsSection(
    invitationViewModel: InvitationViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by invitationViewModel.uiState.collectAsState()
    val invitations by invitationViewModel.invitations.collectAsState()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Team-Einladungen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (uiState) {
                is InvitationUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is InvitationUiState.Empty -> {
                    Text(
                        text = "Keine ausstehenden Team-Einladungen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                is InvitationUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp) // Eine feste maximale Höhe für die LazyColumn
                    ) {
                        items(invitations) { invitation ->
                            InvitationItem(
                                invitation = invitation,
                                onAccept = { invitationViewModel.acceptInvitation(invitation) },
                                onDecline = { invitationViewModel.declineInvitation(invitation) }
                            )
                        }
                    }
                }
                
                is InvitationUiState.Error -> {
                    val errorState = uiState as InvitationUiState.Error
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { invitationViewModel.loadInvitations() }
                        ) {
                            Text("Erneut versuchen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvitationItem(
    invitation: TeamInvitation,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = invitation.teamName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Du wurdest zu diesem Team eingeladen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDecline,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Ablehnen")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onAccept
                ) {
                    Text("Annehmen")
                }
            }
        }
    }
}