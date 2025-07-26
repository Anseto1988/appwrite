package com.example.snacktrack.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.repository.AuthRepository
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.ui.components.CommonTopAppBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToDogs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFoodDatabase: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository(context) }
    val dogRepository = remember { DogRepository(context) }
    val appwriteService = remember { AppwriteService.getInstance(context) }
    
    var userName by remember { mutableStateOf("") }
    var dogCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        // Lade Benutzerdaten
        authRepository.getCurrentUser().collect { user ->
            userName = user?.name ?: "Benutzer"
        }
    }
    
    LaunchedEffect(Unit) {
        // Lade Anzahl der Hunde
        dogRepository.getDogs().collect { dogs ->
            dogCount = dogs.size
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "SnackTrack",
                showBackButton = false, // Kein Zurück auf dem Home Screen
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Einstellungen")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            authRepository.logout()
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Abmelden")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Begrüßung
                item {
                    Text(
                        text = "Willkommen zurück, $userName!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Quick Stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Hunde",
                            value = dogCount.toString(),
                            icon = Icons.Default.Pets,
                            onClick = onNavigateToDogs
                        )
                        
                        QuickStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Heute gefüttert",
                            value = "0", // TODO: Implementieren
                            icon = Icons.Default.Restaurant,
                            onClick = { /* TODO */ }
                        )
                    }
                }
                
                // Hauptaktionen
                item {
                    Text(
                        text = "Schnellzugriff",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            ActionCard(
                                title = "Hunde verwalten",
                                icon = Icons.Default.Pets,
                                onClick = onNavigateToDogs
                            )
                        }
                        
                        item {
                            ActionCard(
                                title = "Futterdatenbank",
                                icon = Icons.Default.Storage,
                                onClick = onNavigateToFoodDatabase
                            )
                        }
                        
                        item {
                            ActionCard(
                                title = "Neuer Hund",
                                icon = Icons.Default.Add,
                                onClick = {
                                    navController.navigate("add_dog")
                                }
                            )
                        }
                        
                        item {
                            ActionCard(
                                title = "Export/Import",
                                icon = Icons.Default.ImportExport,
                                onClick = {
                                    navController.navigate("export_import")
                                }
                            )
                        }
                    }
                }
                
                // Wenn keine Hunde vorhanden
                if (dogCount == 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Pets,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Noch keine Hunde hinzugefügt",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Fügen Sie Ihren ersten Hund hinzu, um loszulegen!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { navController.navigate("add_dog") }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Hund hinzufügen")
                                }
                            }
                        }
                    }
                }
                
                // Letzte Aktivitäten
                item {
                    Text(
                        text = "Letzte Aktivitäten",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // TODO: Echte Aktivitäten laden
                items(3) { index ->
                    ActivityCard(
                        activity = "Beispielaktivität ${index + 1}",
                        time = "Vor ${index + 1} Stunden"
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.size(120.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActivityCard(
    activity: String,
    time: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}