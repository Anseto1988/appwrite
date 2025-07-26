package com.example.snacktrack.ui.screens.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.Team
import com.example.snacktrack.data.model.TeamMember
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.TeamViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val teamViewModel = remember { TeamViewModel(context) }
    val teams by teamViewModel.teams.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTeam by remember { mutableStateOf<Team?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Team-Verwaltung",
                onBackClick = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Team erstellen")
            }
        }
    ) { paddingValues ->
        if (teams.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Noch keine Teams vorhanden",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { showCreateDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Erstes Team erstellen")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teams) { team ->
                    TeamCard(
                        team = team,
                        onManageClick = { 
                            selectedTeam = team
                            showAddMemberDialog = true
                        },
                        onDeleteClick = {
                            selectedTeam = team
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // Dialog zum Erstellen eines Teams
    if (showCreateDialog) {
        CreateTeamDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { teamName ->
                scope.launch {
                    teamViewModel.createTeam(teamName)
                    showCreateDialog = false
                }
            }
        )
    }
    
    // Dialog zum Hinzufügen von Mitgliedern
    if (showAddMemberDialog && selectedTeam != null) {
        AddMemberDialog(
            team = selectedTeam!!,
            onDismiss = { 
                showAddMemberDialog = false
                selectedTeam = null
            },
            onAddMember = { email ->
                scope.launch {
                    selectedTeam?.let { team ->
                        teamViewModel.addMemberToTeam(team.id, email)
                    }
                    showAddMemberDialog = false
                    selectedTeam = null
                }
            }
        )
    }
    
    // Dialog zum Löschen eines Teams
    if (showDeleteDialog && selectedTeam != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                selectedTeam = null
            },
            title = { Text("Team löschen?") },
            text = { 
                Text("Möchten Sie das Team '${selectedTeam?.name}' wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            selectedTeam?.let { team ->
                                teamViewModel.deleteTeam(team.id)
                            }
                            showDeleteDialog = false
                            selectedTeam = null
                        }
                    }
                ) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        selectedTeam = null
                    }
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
    onManageClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        team.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${team.members.size} Mitglieder",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onManageClick) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Mitglied hinzufügen")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Team löschen",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            if (team.members.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                
                team.members.forEach { member ->
                    MemberItem(member = member, isOwner = member.userId == team.ownerId)
                }
            }
            
            // Geteilte Hunde anzeigen
            team.sharedDogs?.let { dogs ->
                if (dogs.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Geteilte Hunde: ${dogs.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemberItem(
    member: TeamMember,
    isOwner: Boolean
) {
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
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    member.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (isOwner) {
            Badge {
                Text("Besitzer")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neues Team erstellen") },
        text = {
            Column {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { 
                        teamName = it
                        nameError = null
                    },
                    label = { Text("Team-Name") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        teamName.isBlank() -> nameError = "Bitte Team-Namen eingeben"
                        teamName.length < 3 -> nameError = "Name muss mindestens 3 Zeichen lang sein"
                        else -> onCreate(teamName.trim())
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
    team: Team,
    onDismiss: () -> Unit,
    onAddMember: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mitglied zu '${team.name}' hinzufügen") },
        text = {
            Column {
                Text(
                    "Geben Sie die E-Mail-Adresse des Benutzers ein, den Sie zum Team hinzufügen möchten.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = null
                    },
                    label = { Text("E-Mail-Adresse") },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        email.isBlank() -> emailError = "Bitte E-Mail-Adresse eingeben"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                            emailError = "Ungültige E-Mail-Adresse"
                        team.members.any { it.email == email } ->
                            emailError = "Benutzer ist bereits Mitglied"
                        else -> onAddMember(email.trim())
                    }
                }
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}