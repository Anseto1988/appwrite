package com.example.snacktrack.ui.screens.dogs

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
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDogScreen(
    dogId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val dogRepository = remember { DogRepository(context) }
    val scope = rememberCoroutineScope()
    
    var dog by remember { mutableStateOf<Dog?>(null) }
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDateString by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var selectedSex by remember { mutableStateOf(Sex.MALE) }
    var selectedActivityLevel by remember { mutableStateOf(ActivityLevel.NORMAL) }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Lade Hundedaten
    LaunchedEffect(dogId) {
        isLoading = true
        try {
            dogRepository.getDogs().collect { dogs ->
                dog = dogs.find { it.id == dogId }
                dog?.let { d ->
                    name = d.name
                    breed = d.breed
                    birthDateString = d.birthDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: ""
                    weight = d.weight.toString()
                    targetWeight = d.targetWeight?.toString() ?: ""
                    selectedSex = d.sex
                    selectedActivityLevel = d.activityLevel
                }
                isLoading = false
            }
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Hund bearbeiten",
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
                            "Fehler beim Laden der Hundedaten",
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
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
                        isError = name.isEmpty() && isSaving
                    )
                    
                    // Rasse
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Rasse") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                    
                    // Geburtsdatum
                    OutlinedTextField(
                        value = birthDateString,
                        onValueChange = { birthDateString = it },
                        label = { Text("Geburtsdatum (TT.MM.JJJJ)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                    )
                    
                    // Gewicht
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Gewicht (kg) *") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                        isError = weight.isEmpty() && isSaving
                    )
                    
                    // Zielgewicht
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text("Zielgewicht (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) }
                    )
                    
                    // Geschlecht
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Geschlecht",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Sex.entries.forEach { sex ->
                                    FilterChip(
                                        selected = selectedSex == sex,
                                        onClick = { selectedSex = sex },
                                        label = { Text(sex.displayName) }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Aktivitätslevel
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Aktivitätslevel",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ActivityLevel.entries.forEach { level ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedActivityLevel == level,
                                        onClick = { selectedActivityLevel = level }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(level.displayName)
                                        Text(
                                            level.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Speichern Button
                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                error = null
                                
                                // Validierung
                                if (name.isBlank() || weight.isBlank()) {
                                    error = "Bitte füllen Sie alle Pflichtfelder aus"
                                    isSaving = false
                                    return@launch
                                }
                                
                                val weightValue = weight.toDoubleOrNull()
                                if (weightValue == null || weightValue <= 0) {
                                    error = "Bitte geben Sie ein gültiges Gewicht ein"
                                    isSaving = false
                                    return@launch
                                }
                                
                                val targetWeightValue = if (targetWeight.isNotBlank()) {
                                    targetWeight.toDoubleOrNull()
                                } else null
                                
                                // Parse Geburtsdatum
                                val birthDate = if (birthDateString.isNotBlank()) {
                                    try {
                                        LocalDate.parse(birthDateString, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                    } catch (e: Exception) {
                                        null
                                    }
                                } else null
                                
                                // Update den Hund
                                val updatedDog = dog!!.copy(
                                    name = name.trim(),
                                    breed = breed.trim().takeIf { it.isNotBlank() } ?: "",
                                    birthDate = birthDate,
                                    weight = weightValue,
                                    targetWeight = targetWeightValue,
                                    sex = selectedSex,
                                    activityLevel = selectedActivityLevel
                                )
                                
                                val result = dogRepository.updateDog(updatedDog)
                                if (result.isSuccess) {
                                    navController.navigateUp()
                                } else {
                                    error = "Fehler beim Speichern: ${result.exceptionOrNull()?.message}"
                                }
                                
                                isSaving = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Änderungen speichern")
                        }
                    }
                    
                    // Fehleranzeige
                    error?.let { err ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    err,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}