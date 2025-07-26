package com.example.snacktrack.ui.screens.dogs

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.navigation.Screen
import com.example.snacktrack.ui.viewmodel.DogViewModel
import com.example.snacktrack.ui.viewmodel.TeamViewModel
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun DogDetailScreen(
    dogId: String,
    navController: NavController,
    dogViewModel: DogViewModel
) {
    val context = LocalContext.current
    val teamViewModel = remember { TeamViewModel(context) }
    
    // Zustandsvariablen für die Sitzungsvalidierung
    var sessionValid by remember { mutableStateOf(true) }
    var isCheckingSession by remember { mutableStateOf(true) }
    
    // Zustandsvariablen für das Laden der Hundedaten
    var isDogsLoading by remember { mutableStateOf(true) }
    
    // Zuerst prüfen wir, ob die Sitzung gültig ist
    LaunchedEffect(Unit) {
        try {
            val userId = dogViewModel.getCurrentUserId()
            sessionValid = userId.isNotEmpty()
            isCheckingSession = false
            Log.d("DogDetailScreen", "Session validiert: $sessionValid, UserID: $userId")
        } catch (e: Exception) {
            Log.e("DogDetailScreen", "Fehler bei der Sitzungsvalidierung: ${e.message}")
            sessionValid = false
            isCheckingSession = false
        }
    }
    
    // Wenn die Sitzung ungültig ist, zeigen wir ein entsprechendes UI und Optionen
    if (!sessionValid && !isCheckingSession) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Sitzung abgelaufen",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Deine Anmeldesitzung ist abgelaufen. Bitte melde dich erneut an, um fortzufahren.",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                // DEBUG TEST BUTTON - Direkt zur Edit-Seite navigieren
                Button(
                    onClick = {
                        val route = Screen.EditDog.createRoute(dogId)
                        Log.d("Navigation", "TEST-BUTTON: Navigiere zu Edit-Screen mit Route: $route")
                        navController.navigate(route)
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("TEST: Hund bearbeiten")
                }
                
                Button(
                    onClick = { 
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Zur Anmeldung")
                }
            }
        }
        return
    }
    
    val dogs by dogViewModel.dogs.collectAsState()
    val teams by teamViewModel.teams.collectAsState()
    val dog = dogs.find { it.id == dogId }
    
    // Prüfe, ob Hundedaten noch geladen werden
    LaunchedEffect(dogs.size) {
        // Wenn Hunde geladen wurden (Liste ist nicht leer), stoppe das Laden
        if (dogs.isNotEmpty()) {
            isDogsLoading = false
            Log.d("DogDetailScreen", "🐕 ${dogs.size} Hunde geladen, suche nach ID: $dogId")
            Log.d("DogDetailScreen", "🐕 Verfügbare Hunde: ${dogs.map { "${it.name}(${it.id})" }}")
        }
    }
    
    // Timeout für das Laden von Hundedaten (falls Repository leer zurückgibt)
    LaunchedEffect(Unit) {
        delay(3000) // 3 Sekunden warten
        if (isDogsLoading && dogs.isEmpty()) {
            Log.w("DogDetailScreen", "⏰ Timeout beim Laden der Hundedaten - beende Ladevorgang")
            isDogsLoading = false
        }
    }
    
    // Wenn wir die Session noch überprüfen oder Hunde noch laden, zeigen wir einen Ladezustand
    if (isCheckingSession || isDogsLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isCheckingSession) "Sitzung wird geprüft..." else "Lade Hundedaten...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }
    
    // Jetzt prüfen wir, ob der Hund nach dem Laden gefunden wurde
    if (dog == null) {
        LaunchedEffect(Unit) {
            Log.e("DogDetailScreen", "❌ Hund mit ID $dogId nicht gefunden nach dem Laden - navigiere zurück")
            Log.e("DogDetailScreen", "❌ Verfügbare Hunde: ${dogs.map { "${it.name}(${it.id})" }}")
            navController.popBackStack()
        }
        return
    }
    
    // Ab hier ist dog garantiert nicht null
    val nonNullDog = dog
    
    // Finden des Teams, falls der Hund geteilt wird
    val team = nonNullDog.teamId?.let { teamId ->
        teams.find { it.id == teamId }
    }
    
    // Die Benutzer-ID abrufen
    var currentUserId by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        currentUserId = dogViewModel.getCurrentUserId()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Log.d("DogDetailScreen", "🎯 Zeige Details für Hund: ${nonNullDog.name} (ID: ${nonNullDog.id})")
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = nonNullDog.name,
                showBackButton = true,
                showDogDetailButton = true,
                currentDogOwnerId = nonNullDog.ownerId,
                onBackClick = { 
                    Log.d("Navigation", "🔙 Zurück-Button geklickt")
                    navController.popBackStack() 
                },
                onDogDetailClick = {
                    val route = Screen.EditDog.createRoute(nonNullDog.id)
                    Log.d("Navigation", "🚀 EDIT-BUTTON CALLBACK: Edit-Button wurde geklickt für Hund: ${nonNullDog.name} (ID: ${nonNullDog.id})")
                    Log.d("Navigation", "🚀 ROUTE: Navigiere zu: $route")
                    try {
                        navController.navigate(route)
                        Log.d("Navigation", "✅ Navigation erfolgreich gestartet")
                    } catch (e: Exception) {
                        Log.e("Navigation", "❌ Navigation fehlgeschlagen: ${e.message}", e)
                    }
                },
                onAdminClick = { 
                    Log.d("Navigation", "🔧 Admin-Button geklickt")
                    navController.navigate(Screen.FoodSubmissionAdmin.route) 
                },
                onAccountClick = { 
                    Log.d("Navigation", "👤 Account-Button geklickt")
                    navController.navigate(Screen.AccountManagement.route) 
                },
                onLogoutClick = { 
                    Log.d("Navigation", "🚪 Logout-Button geklickt")
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                actions = {
                    // Menü für weitere Aktionen wie Löschen wenn Benutzer der Besitzer ist
                    if (currentUserId == nonNullDog.ownerId) {
                        var showMenu by remember { mutableStateOf(false) }
                        
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Aktionen")
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Löschen") },
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Löschen"
                                        ) 
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // Lösch-Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hund löschen") },
                text = { Text("Möchtest du ${nonNullDog.name} wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                dogViewModel.deleteDog(nonNullDog.id)
                                navController.popBackStack() // Zurück zur Hundesliste navigieren
                            }
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Löschen")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Abbrechen")
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hundebild
            if (!nonNullDog.imageId.isNullOrEmpty()) {
                // Verwende ByteArray-basierte Bildanzeige für bessere Authentifizierung
                val imageDataFlow = dogViewModel.getImageDataFlow(nonNullDog.imageId)
                val imageData by imageDataFlow.collectAsState()
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (val data = imageData) {
                        null -> {
                            // Während Bilddaten geladen werden
                            CircularProgressIndicator()
                        }
                        else -> {
                            if (data.isEmpty()) {
                                // Fallback, wenn keine Bilddaten geladen werden konnten
                                Text("Bild konnte nicht geladen werden")
                            } else {
                                // Bild mit ByteArray anzeigen
                                AsyncImage(
                                    model = data,
                                    contentDescription = "Foto von ${nonNullDog.name}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onError = { error ->
                                        Log.e("DogImage", "Detail-Screen: Fehler beim Laden des Bildes für ${nonNullDog.name}: ${error.result.throwable.message}")
                                    },
                                    onSuccess = {
                                        Log.d("DogImage", "Detail-Screen: Bild für ${nonNullDog.name} erfolgreich geladen")
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Grundinformationen
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Grundinformationen",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow("Name", nonNullDog.name)
                    DetailRow("Rasse", nonNullDog.breed)
                    DetailRow("Geschlecht", nonNullDog.sex.displayName)
                    nonNullDog.birthDate?.let {
                        DetailRow("Geburtsdatum", it.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    }
                }
            }

            // Gewicht und Aktivität
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Gewicht & Aktivität",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow("Aktuelles Gewicht", "${nonNullDog.weight} kg")
                    nonNullDog.targetWeight?.let {
                        DetailRow("Zielgewicht", "$it kg")
                    }
                    DetailRow("Aktivitätslevel", nonNullDog.activityLevel.displayName)
                    DetailRow("Täglicher Kalorienbedarf", "${nonNullDog.calculateDailyCalorieNeed()} kcal")
                }
            }
            
            // Team-Informationen anzeigen, falls der Hund geteilt wird
            if (nonNullDog.teamId != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PeopleAlt,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Team-Informationen",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (team != null) {
                            DetailRow("Geteilt mit Team", team.name)
                            DetailRow("Team-Besitzer", if (team.ownerId == nonNullDog.ownerId) "Sie" else {
                                team.members.find { it.userId == team.ownerId }?.name ?: "Unbekannt"
                            })
                            
                            // Wenn der Benutzer der Besitzer des Hundes ist, zeigen wir die Option zum Aufheben der Freigabe an
                            if (nonNullDog.ownerId == currentUserId) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            // TODO: Implement removeDogFromTeam method in TeamViewModel
                                            // teamViewModel.removeDogFromTeam("team_id", nonNullDog.id)
                                            // Hunde neu laden, damit die Änderung angezeigt wird
                                            dogViewModel.loadDogs()
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Freigabe aufheben")
                                }
                            }
                        } else {
                            Text("Team-Informationen konnten nicht geladen werden.")
                        }
                    }
                }
            } else if (nonNullDog.ownerId == currentUserId) {
                // Option zum Teilen anzeigen, wenn der Benutzer der Besitzer ist und der Hund noch nicht geteilt wird
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Teilen",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text("Dieser Hund wird derzeit nicht geteilt.")
                        
                        Button(
                            onClick = {
                                navController.navigate(Screen.AccountManagement.route)
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hund teilen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium
        )
        Text(text = value)
    }
}