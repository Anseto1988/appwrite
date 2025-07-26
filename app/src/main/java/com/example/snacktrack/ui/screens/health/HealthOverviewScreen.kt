package com.example.snacktrack.ui.screens.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.snacktrack.data.model.HealthStatus
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.repository.HealthRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.navigation.Screen
import com.example.snacktrack.data.model.Vaccination
import com.example.snacktrack.data.service.AppwriteService
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthOverviewScreen(
    dogId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val dogRepository = remember { DogRepository(context) }
    val appwriteService = remember { AppwriteService.getInstance(context) }
    val healthRepository = remember { HealthRepository(context, appwriteService) }
    val scope = rememberCoroutineScope()
    
    var dog by remember { mutableStateOf<com.example.snacktrack.data.model.Dog?>(null) }
    var healthStatus by remember { mutableStateOf<HealthStatus?>(null) }
    var upcomingVaccinations by remember { mutableStateOf<List<Vaccination>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Lade Gesundheitsdaten
    LaunchedEffect(dogId) {
        isLoading = true
        error = null
        
        try {
            // Lade Hund
            val dogsFlow = dogRepository.getDogs()
            scope.launch {
                dogsFlow.collect { dogs ->
                    dog = dogs.find { it.id == dogId }
                }
            }
            
            // TODO: Gesundheitsstatus implementieren
            // val healthResult = healthRepository.getHealthStatus(dogId)
            // healthResult.fold(
            //     onSuccess = { status ->
            //         healthStatus = status
            //     },
            //     onFailure = { /* Kein Fehler anzeigen, wenn noch kein Status existiert */ }
            // )
            
            // Lade anstehende Impfungen
            val vaccinationsResult = healthRepository.getUpcomingVaccinations(dogId)
            vaccinationsResult.fold(
                onSuccess = { vaccinations ->
                    upcomingVaccinations = vaccinations
                },
                onFailure = { /* Kein Fehler anzeigen */ }
            )
            
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Gesundheit",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null || dog == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
                            "Fehler beim Laden der Gesundheitsdaten",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        error?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Gesundheitsstatus
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (healthStatus?.overallHealth) {
                                "Ausgezeichnet" -> MaterialTheme.colorScheme.primaryContainer
                                "Gut" -> MaterialTheme.colorScheme.secondaryContainer
                                "Befriedigend" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Gesundheitsstatus",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                healthStatus?.overallHealth ?: "Noch nicht bewertet",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            healthStatus?.lastCheckup?.let { lastCheckup ->
                                Text(
                                    "Letzter Check: ${lastCheckup.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Gewichtsstatus
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            navController.navigate(Screen.WeightHistory.createRoute(dogId))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.MonitorWeight,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "Gewicht",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    dog?.let { d ->
                                        Text(
                                            "${d.weight} kg",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        d.targetWeight?.let { target ->
                                            val diff = d.weight - target
                                            Text(
                                                when {
                                                    diff > 0.5 -> "Über Zielgewicht (+%.1f kg)".format(diff)
                                                    diff < -0.5 -> "Unter Zielgewicht (%.1f kg)".format(diff)
                                                    else -> "Im Zielbereich"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = when {
                                                    kotlin.math.abs(diff) > 2 -> MaterialTheme.colorScheme.error
                                                    kotlin.math.abs(diff) > 0.5 -> MaterialTheme.colorScheme.tertiary
                                                    else -> MaterialTheme.colorScheme.primary
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                    
                    // Impfungen
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Vaccines,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Impfungen",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (upcomingVaccinations.isNotEmpty()) {
                                    Badge {
                                        Text("${upcomingVaccinations.size}")
                                    }
                                }
                            }
                            
                            if (upcomingVaccinations.isEmpty()) {
                                Text(
                                    "Keine anstehenden Impfungen",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                upcomingVaccinations.forEach { vaccination ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                vaccination.vaccineName,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                "Fällig: ${vaccination.dueDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (vaccination.dueDate.isBefore(LocalDate.now()))
                                                    MaterialTheme.colorScheme.error
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (vaccination.dueDate.isBefore(LocalDate.now())) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Button(
                                onClick = {
                                    // TODO: Navigation zu Impfungsdetails
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Impfungen verwalten")
                            }
                        }
                    }
                    
                    // Medikamente
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
                                    Icons.Default.Medication,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Medikamente",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                "Keine aktiven Medikamente",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            OutlinedButton(
                                onClick = {
                                    // TODO: Medikament hinzufügen
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Medikament hinzufügen")
                            }
                        }
                    }
                    
                    // Allergien & Unverträglichkeiten
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
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Allergien & Unverträglichkeiten",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            if (dog?.allergens.isNullOrEmpty()) {
                                Text(
                                    "Keine bekannten Allergien",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                dog?.allergens?.forEach { allergen ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Circle,
                                            contentDescription = null,
                                            modifier = Modifier.size(8.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            allergen,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Notfallkontakte
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
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Notfallkontakte",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // TODO: Tierarzt-Kontakt aus Benutzerprofil laden
                            Text(
                                "Tierarzt: Dr. Mustermann",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Tel: 0123 456789",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            Text(
                                "Notfall-Tierklinik",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Tel: 0123 999999",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}