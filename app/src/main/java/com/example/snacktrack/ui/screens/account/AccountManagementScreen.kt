package com.example.snacktrack.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Hilt-Import entfernt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.Team
import com.example.snacktrack.data.model.TeamMember
import com.example.snacktrack.data.model.BasicTeamRole
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.InvitationViewModel
import com.example.snacktrack.ui.viewmodel.TeamViewModel
import kotlinx.coroutines.delay

@Composable
fun AccountManagementScreen(
    navController: NavController
) {
    val context = LocalContext.current
    // Verwende die Factory für InvitationViewModel statt Hilt
    val invitationViewModel: InvitationViewModel = viewModel { InvitationViewModel.Factory(context).create(InvitationViewModel::class.java) }
    val teamViewModel: TeamViewModel = viewModel { TeamViewModel(context) }
    
    val teams by teamViewModel.teams.collectAsState()
    val ownDogs by invitationViewModel.ownDogs.collectAsState()
    val searchResults by invitationViewModel.searchResults.collectAsState()
    val isLoading by teamViewModel.isLoading.collectAsState()
    val errorMessage by invitationViewModel.errorMessage.collectAsState()
    val successMessage by invitationViewModel.successMessage.collectAsState()
    
    // State für Dialoge
    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var showInviteUserDialog by remember { mutableStateOf(false) }
    
    // Einladungen beim Start laden
    LaunchedEffect(Unit) {
        invitationViewModel.loadInvitations()
    }
    var showShareDogDialog by remember { mutableStateOf(false) }
    var showDeleteTeamDialog by remember { mutableStateOf(false) }
    var selectedTeam by remember { mutableStateOf<Team?>(null) }
    var selectedDog by remember { mutableStateOf<Dog?>(null) }
    
    // Nachrichten automatisch ausblenden
    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null || errorMessage != null) {
            delay(3000)
            invitationViewModel.clearMessages()
        }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Kontoverwaltung",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // LazyColumn braucht eine feste Höhenbeschränkung
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Diese Zeile sorgt für eine feste Höhenbeschränkung
            ) {
                // Team-Einladungen anzeigen
                item {
                    Text(
                        text = "Einladungen",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TeamInvitationsSection(
                        invitationViewModel = invitationViewModel
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                }
                item {
                    // Abschnitt für Teams
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Meine Teams",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Button(
                            onClick = { showCreateTeamDialog = true },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Team erstellen"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Team erstellen")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Liste der Teams
                if (teams.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Keine Teams vorhanden. Erstellen Sie ein Team, um Ihre Hunde zu teilen.")
                            }
                        }
                    }
                } else {
                    items(teams) { team ->
                        TeamCard(
                            team = team,
                            onInviteClick = {
                                selectedTeam = team
                                showInviteUserDialog = true
                            },
                            onShareDogClick = {
                                selectedTeam = team
                                showShareDogDialog = true
                            },
                            onDeleteClick = {
                                selectedTeam = team
                                showDeleteTeamDialog = true
                            },
                            onRemoveMemberClick = { membershipId ->
                                teamViewModel.removeTeamMember(team.id, membershipId)
                            },
                            onUpdateRoleClick = { membershipId, role ->
                                teamViewModel.updateTeamMemberRole(team.id, membershipId, role)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            // Ladeanzeige
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            
            // Snackbars für Fehler- und Erfolgsmeldungen
            errorMessage?.let {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(it)
                    }
                }
            }
            
            successMessage?.let {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(it)
                    }
                }
            }
        }
    }
    
    // Dialog zum Erstellen eines Teams
    if (showCreateTeamDialog) {
        CreateTeamDialog(
            onDismiss = { showCreateTeamDialog = false },
            onCreateTeam = { name ->
                teamViewModel.createTeam(name)
                showCreateTeamDialog = false
            }
        )
    }
    
    // Dialog zum Einladen eines Benutzers
    if (showInviteUserDialog && selectedTeam != null) {
        InviteUserDialog(
            onDismiss = { showInviteUserDialog = false },
            onInviteUser = { email, role ->
                invitationViewModel.addTeamMember(selectedTeam!!.id, email)
                showInviteUserDialog = false
            },
            onSearchUser = { email ->
                invitationViewModel.searchUsers(email)
            },
            searchResults = searchResults
        )
    }
    
    // Dialog zum Teilen eines Hundes
    if (showShareDogDialog && selectedTeam != null) {
        ShareDogDialog(
            dogs = ownDogs.filter { it.teamId != selectedTeam!!.id },
            onDismiss = { showShareDogDialog = false },
            onShareDog = { dogId ->
                invitationViewModel.shareDogWithTeam(dogId, selectedTeam!!.id)
                showShareDogDialog = false
            }
        )
    }
    
    // Dialog zum Löschen eines Teams
    if (showDeleteTeamDialog && selectedTeam != null) {
        AlertDialog(
            onDismissRequest = { showDeleteTeamDialog = false },
            title = { Text("Team löschen") },
            text = { Text("Möchten Sie das Team '${selectedTeam!!.name}' wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                Button(
                    onClick = {
                        teamViewModel.deleteTeam(selectedTeam!!.id)
                        showDeleteTeamDialog = false
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteTeamDialog = false }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun TeamCard(
    team: Team,
    onInviteClick: () -> Unit,
    onShareDogClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRemoveMemberClick: (String) -> Unit,
    onUpdateRoleClick: (String, BasicTeamRole) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Team-Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onShareDogClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Hund teilen"
                        )
                    }
                    
                    IconButton(onClick = onInviteClick) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Benutzer einladen"
                        )
                    }
                    
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Team löschen"
                        )
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Team-Mitglieder
            Text(
                text = "Mitglieder",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            team.members.forEach { member ->
                TeamMemberItem(
                    member = member,
                    isOwner = member.userId == team.ownerId,
                    onRemoveClick = { onRemoveMemberClick(member.userId) },
                    onRoleChange = { role -> onUpdateRoleClick(member.userId, role) }
                )
            }
        }
    }
}

@Composable
fun TeamMemberItem(
    member: TeamMember,
    isOwner: Boolean,
    onRemoveClick: () -> Unit,
    onRoleChange: (BasicTeamRole) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(text = member.name)
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Row {
            // Dropdown für Rollenauswahl (nur wenn man nicht selbst der Besitzer ist)
            if (!isOwner) {
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(member.role.displayName)
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        BasicTeamRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.displayName) },
                                onClick = {
                                    onRoleChange(role)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = onRemoveClick,
                    enabled = !isOwner // Besitzer kann nicht entfernt werden
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Entfernen",
                        tint = if (isOwner) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else MaterialTheme.colorScheme.error
                    )
                }
            } else {
                // Wenn es der Besitzer ist, zeigen wir nur die Rolle an
                Text(
                    text = member.role.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    }
}

@Composable
fun CreateTeamDialog(
    onDismiss: () -> Unit,
    onCreateTeam: (String) -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Team erstellen") },
        text = {
            Column {
                Text("Geben Sie einen Namen für das neue Team ein:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Teamname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateTeam(teamName) },
                enabled = teamName.isNotBlank()
            ) {
                Text("Erstellen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun InviteUserDialog(
    onDismiss: () -> Unit,
    onInviteUser: (String, BasicTeamRole) -> Unit,
    onSearchUser: (String) -> Unit,
    searchResults: List<com.example.snacktrack.data.model.User>
) {
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(BasicTeamRole.VIEWER) }
    var showRoleDropdown by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Benutzer einladen") },
        text = {
            Column {
                Text("Geben Sie die E-Mail-Adresse des Benutzers ein:")
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-Mail") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onSearchUser(email) },
                        enabled = email.contains("@")
                    ) {
                        Text("Suchen")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Suchergebnisse
                if (searchResults.isNotEmpty()) {
                    Text(
                        text = "Gefundene Benutzer:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.height(120.dp)
                    ) {
                        items(searchResults) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(user.name)
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        email = user.email
                                    }
                                ) {
                                    Text("Auswählen")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Rollenauswahl
                Text("Rolle auswählen:")
                Spacer(modifier = Modifier.height(8.dp))
                
                Box {
                    Button(
                        onClick = { showRoleDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedRole.displayName)
                    }
                    
                    DropdownMenu(
                        expanded = showRoleDropdown,
                        onDismissRequest = { showRoleDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        BasicTeamRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.displayName) },
                                onClick = {
                                    selectedRole = role
                                    showRoleDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onInviteUser(email, selectedRole) },
                enabled = email.contains("@")
            ) {
                Text("Einladen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun ShareDogDialog(
    dogs: List<Dog>,
    onDismiss: () -> Unit,
    onShareDog: (String) -> Unit
) {
    var selectedDogId by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hund teilen") },
        text = {
            Column {
                if (dogs.isEmpty()) {
                    Text("Sie haben keine Hunde, die Sie teilen können.")
                } else {
                    Text("Wählen Sie einen Hund aus, den Sie teilen möchten:")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(dogs) { dog ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDogId == dog.id,
                                    onClick = { selectedDogId = dog.id }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(dog.name)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onShareDog(selectedDogId) },
                enabled = selectedDogId.isNotEmpty() && dogs.isNotEmpty()
            ) {
                Text("Teilen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
