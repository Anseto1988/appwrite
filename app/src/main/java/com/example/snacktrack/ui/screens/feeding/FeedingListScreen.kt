package com.example.snacktrack.ui.screens.feeding

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
import com.example.snacktrack.data.model.Feeding
import com.example.snacktrack.data.repository.FeedingRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.navigation.Screen
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingListScreen(
    dogId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val feedingRepository = remember { FeedingRepository(context) }
    val scope = rememberCoroutineScope()
    
    var feedings by remember { mutableStateOf<List<Feeding>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    // Lade Fütterungen
    LaunchedEffect(dogId, selectedDate) {
        isLoading = true
        error = null
        
        scope.launch {
            feedingRepository.getFeedingsByDate(dogId, selectedDate).collect { feedingList ->
                feedings = feedingList
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Fütterungen",
                onBackClick = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddFeeding.createRoute(dogId))
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Fütterung hinzufügen")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Datumsauswahl
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = {
                    // TODO: Datumspicker öffnen
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Datum",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Fehler beim Laden der Fütterungen",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                error ?: "Unbekannter Fehler",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        error = null
                                        feedingRepository.getFeedingsByDate(dogId, selectedDate).collect { feedingList ->
                                            feedings = feedingList
                                            isLoading = false
                                        }
                                    }
                                }
                            ) {
                                Text("Erneut versuchen")
                            }
                        }
                    }
                }
                feedings.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.FoodBank,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Keine Fütterungen für diesen Tag",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = {
                                    navController.navigate(Screen.AddFeeding.createRoute(dogId))
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fütterung hinzufügen")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tagesübersicht
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Tagesübersicht",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "${feedings.sumOf { it.calories }}",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "kcal",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "${feedings.sumOf { it.amount }}g",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Menge",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "${feedings.size}",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Mahlzeiten",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Fütterungsliste
                        items(feedings) { feeding ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    // TODO: Navigation zur Fütterungsdetails
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            feeding.foodName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            "Typ: ${feeding.type.displayName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        feeding.time?.let { time ->
                                            Text(
                                                time.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            "${feeding.amount}g",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${feeding.calories} kcal",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}